package org.mellowd.io.live;

import org.mellowd.compiler.MellowDBlock;
import org.mellowd.intermediate.SchedulerDirectives;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.MIDITrack;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.primitives.Beat;

import javax.sound.midi.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;
import java.util.stream.Collectors;

public class CycleScheduler extends Thread {
    // TODO add option to make blocks that are not a multiple of a measure a compile error for
    // performances. This prevents strange out of sync issues.

    private enum ActiveState {
        INITIALIZING,
        READY,
        EXECUTING,
        SHUTTING_DOWN,
    }

    private static class ActiveBlock {
        final MellowDBlock block;
        final Statement[] code;
        final MIDITrack activeBuffer;
        final MIDITrack queuedReplacementBuffer;
        final ActiveState state;

        public ActiveBlock(MellowDBlock block, Statement[] code, MIDITrack activeBuffer, MIDITrack queuedReplacementBuffer, ActiveState state) {
            this.block = block;
            this.code = code;
            this.activeBuffer = activeBuffer;
            this.queuedReplacementBuffer = queuedReplacementBuffer;
            this.state = state;
        }

        public ActiveBlock withState(ActiveState state) {
            return new ActiveBlock(
                    this.block, this.code, this.activeBuffer, this.queuedReplacementBuffer,
                    state
            );
        }

        public ActiveBlock withEnqueuedReplacement(MIDITrack next) {
            return new ActiveBlock(
                    this.block, this.code,
                    this.activeBuffer != null ? this.activeBuffer : next,
                    this.activeBuffer != null ? next : null,
                    ActiveState.READY
            );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("ActiveBlock {\n");
            sb.append("\tblock = ").append(block).append('\n');
            sb.append("\tcode = ").append(Arrays.toString(code)).append('\n');
            sb.append("\tactiveBuffer = ").append(activeBuffer).append('\n');
            if (activeBuffer != null) sb.append("\tstart = ").append(activeBuffer.startTimeStamp()).append('\n');
            if (activeBuffer != null) sb.append("\tend = ").append(activeBuffer.endTimeStamp()).append('\n');
            sb.append("\tqueuedReplacementBuffer = ").append(queuedReplacementBuffer).append('\n');
            sb.append("\tstate = ").append(state).append('\n');
            sb.append("}\n");
            return sb.toString();
        }
    }

    private final TimingEnvironment timingEnvironment;
    private final Synthesizer synth;
    private final Receiver out;

    private final AtomicBoolean running;

    private final BiConsumer<MellowDBlock, Throwable> exceptionHandler;

    private final ExecutorService compilationExecutorService = Executors.newCachedThreadPool();
    private final AtomicReference<Map<String, ActiveBlock>> activeBlocks;


    private AtomicLong stateTime = new AtomicLong(0);
    Beat frameDurationInBeats;
    long frameDurationInTicks;
    long frameDurationInUs;

    Beat measureDurationInBeats;
    long measureDurationTicks;

    public CycleScheduler(Synthesizer synth, TimingEnvironment timingEnvironment, BiConsumer<MellowDBlock, Throwable> exceptionHandler) throws InvalidMidiDataException, MidiUnavailableException {
        super("MellowD-CycleScheduler");
        this.timingEnvironment = timingEnvironment;
        this.synth = synth;
        this.out = synth.getReceiver();

        this.running = new AtomicBoolean(false);
        this.exceptionHandler = exceptionHandler;
        this.activeBlocks = new AtomicReference<>(Collections.emptyMap());

        frameDurationInBeats = Beat.EIGHTH();
        frameDurationInTicks = timingEnvironment.ticksInBeat(frameDurationInBeats);
        frameDurationInUs = timingEnvironment.approxDurationOfBeatInUs(frameDurationInBeats);

        measureDurationInBeats = timingEnvironment.getBeatValue().times(timingEnvironment.getBeatsPerMeasure());
        measureDurationTicks = timingEnvironment.ticksInBeat(measureDurationInBeats);

        this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public synchronized void start() {
        this.running.set(true);
        super.start();
    }

    public void shutdown() {
        this.running.set(false);
    }

    private long getNextMeasureStart(long after, SchedulerDirectives directives) {
        if (after < 0) {
            after = this.stateTime.get();
        }
        after--;

        // TODO optimize for missing directives
        int quant = 1;
        int quantShift = 0;
        if (directives != null && directives.getQuantizeDirective() != null && !directives.getQuantizeDirective().isBlockAlignment()) {
            quant = directives.getQuantizeDirective().getBarSize();
            quantShift = directives.getQuantizeDirective().getBarOffset();
        }

        // We don't want to support using quant phase as a shift mechanism
        quantShift = quantShift % quant;

        // The new measureDuration
        long quantizedMeasureDurationTicks = quant * this.measureDurationTicks;

        long firstQuantizedMeasureStart = this.measureDurationTicks * quantShift;
        if (after <= firstQuantizedMeasureStart)
            return firstQuantizedMeasureStart;

        // Clear the shift offset so we can work from a new "0" which is the position of the firstQuantizedMeasureStart
        after -= firstQuantizedMeasureStart;

        //System.out.printf("quant: %d, measure: %d, quantmeasure: %d%n", quant, measureDurationTicks, quantizedMeasureDurationTicks);
        //return after + (this.measureDurationTicks - (after % measureDurationTicks));
        return firstQuantizedMeasureStart + (after + (quantizedMeasureDurationTicks - (after % quantizedMeasureDurationTicks)));
    }

    private void updateBlock(String name, Function<ActiveBlock, ActiveBlock> updater) {
        this.activeBlocks.updateAndGet(blocks -> {
            Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
            newBlocks.computeIfPresent(name, (blockName, oldBlock) -> updater.apply(oldBlock));
            return newBlocks;
        });
    }

    @Override
    public void run() {
        // TODO add drift cancellation if receiver transmits sync messages

        long clockStart = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
        // System.out.println(this.synth.getMicrosecondPosition());
        long synthOffset = Math.max(0, this.synth.getMicrosecondPosition()) + this.synth.getLatency();
        // System.out.println(synthOffset);
        long frameStartUs = clockStart;
        while (this.running.get()) {
            try {
                long start = stateTime.getAndAdd(frameDurationInTicks);
                long stop = start + frameDurationInTicks;
                //long nextStop = stop + frameDurationInTicks;
                //long nextMeasureStart = stop + (measureDurationTicks - (stop % measureDurationTicks));
                //System.out.printf("start: %d, stop: %d, nextMeasure: %d%n", start, stop, nextMeasureStart);

                ObjLongConsumer<MidiMessage> send = (msg, time) -> {
//                    if (msg.getStatus() == ShortMessage.NOTE_ON || msg.getStatus() == ShortMessage.NOTE_OFF)
//                        System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                    out.send(msg, time + synthOffset);
                };
                this.flushFrame(start, stop, stop + frameDurationInTicks, send);

                frameStartUs += frameDurationInUs;

                long now = System.nanoTime();
                TimeUnit.MICROSECONDS.sleep(frameStartUs - TimeUnit.NANOSECONDS.toMicros(now));
            } catch (InterruptedException ignored) {
                this.running.set(false);
            }
        }
    }

    private void startExecutingBlock(ActiveBlock active, long startStateTime) {
        ActiveBlock nextActive = this.activeBlocks.get().get(active.block.getName());
        if (nextActive != null && nextActive.state == ActiveState.EXECUTING)
            return;

        //System.out.printf("Scheduling %s at %d%n", active.block.getName(), startStateTime);
        //System.out.println(active);

        //System.out.println("Compiling " + active.block.getName() + " @ " + startStateTime);

        this.updateBlock(active.block.getName(), block -> block.withState(ActiveState.EXECUTING));

        MellowDBlock block = active.block;
        MIDITrack nextTrack = new MIDITrack(block.getName());

        // TODO save this future and cancel if a reeval before it gets scheduled
        // TODO the activeBuffer should update it's start time to the frame that it actually ends
        // up running on incase it runs too late.
        this.compilationExecutorService.submit(() -> {
            MIDIChannel channel = block.getMIDIChannel();
            channel.setTrack(nextTrack);

            // Jump to the start of the measure
            //System.out.printf("%s: %d%n", block.getName(), startStateTime);
            channel.stepIntoFuture(startStateTime - channel.getStateTime());

            try {
                for (Statement statement : active.code)
                    statement.execute(block, block);
            } catch (Throwable t) {
                this.exceptionHandler.accept(block, t);
            }

            channel.finalizeEOT(Beat.ZERO);

            // TODO if too slow updating wait to replace?
            this.updateBlock(block.getName(), b -> b.withEnqueuedReplacement(nextTrack));
        });
    }

    private void flushFrame(long start, long stop, long nextFrameStop, ObjLongConsumer<MidiMessage> send) {
        this.activeBlocks.get().values().forEach(active -> {
            // If there is an active buffer ready, flush this window
            if (active.activeBuffer != null) {
                active.activeBuffer.forEachInRange(start, stop, (msg, time) -> {
                    if (MIDITrack.isNotMeta(msg)) {
                        //System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                        send.accept(msg, this.timingEnvironment.ticksToUs(time));
                    }
                });
            }

            long activeEnd = active.activeBuffer == null ? -1 : active.activeBuffer.endTimeStamp();
            if (active.queuedReplacementBuffer != null) {
                // TODO this currently merges the tracks?
                // There is a replacement and this one finished mid frame
                active.queuedReplacementBuffer.forEachInRange(start/*activeEnd*/, stop, (msg, time) -> {
                    if (MIDITrack.isNotMeta(msg)) {
                        // System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                        send.accept(msg, this.timingEnvironment.ticksToUs(time));
                    }
                });
                if (activeEnd < stop) {
                    this.activeBlocks.updateAndGet(blocks -> {
                        //System.out.println("Updated " + active.block.getName());
                        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
                        newBlocks.put(active.block.getName(), new ActiveBlock(active.block, active.code, active.queuedReplacementBuffer, null, ActiveState.READY));
                        return newBlocks;
                    });
                }
            } else {
                if (active.state == ActiveState.SHUTTING_DOWN) {
                    this.activeBlocks.updateAndGet(blocks -> {
                        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
                        newBlocks.remove(active.block.getName());
                        return newBlocks;
                    });
                } else {
                    // Recompile the block
                    //if (activeEnd < stop) {
                    if (active.activeBuffer != null) {
                        if (active.activeBuffer.startTimeStamp() == activeEnd)
                            activeEnd += 1;
                    }
                    long nextMeasureStart = this.getNextMeasureStart(activeEnd, active.block.getSchedulerDirectives());
                    // The next frame contains the start so now is the time to start executing the next frame
                    if (nextMeasureStart < nextFrameStop || active.state == ActiveState.INITIALIZING) {
                        this.startExecutingBlock(active, nextMeasureStart);
                    }
                    //}
                }
            }
        });
    }

    /*
    this.activeBlocks.get().values().forEach(active -> {
            // If there is an active buffer ready, flush this window
            if (active.activeBuffer != null) {
                active.activeBuffer.forEachInRange(start, stop, (msg, time) -> {
                    if (MIDITrack.isNotMeta(msg)) {
                        //System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                        send.accept(msg, this.timingEnvironment.ticksToUs(time));
                    }
                });
            }

            long activeEnd = active.activeBuffer == null ? Integer.MAX_VALUE : active.activeBuffer.endTimeStamp();

            if (active.queuedReplacementBuffer != null) {
                // Flush the replacement buffer
                active.queuedReplacementBuffer.forEachInRange(start*//*activeEnd*//*, stop, (msg, time) -> {
        if (MIDITrack.isNotMeta(msg)) {
            //System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
            send.accept(msg, this.timingEnvironment.ticksToUs(time));
        }
    });
}

// If this window was the last one for the "activeBuffer" then we need to prepare for the next frame!
            if (activeEnd < stop) {
        if (active.queuedReplacementBuffer != null) {
        // If there is a replacement lined up, swap it in
        this.activeBlocks.updateAndGet(blocks -> {
        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
        newBlocks.put(active.block.getName(), new ActiveBlock(active.block, active.code, active.queuedReplacementBuffer, null, ActiveState.READY));
        return newBlocks;
        });
        }

        if (active.state == ActiveState.SHUTTING_DOWN) {
        // If the block is scheduled to shutdown then clear it from the active blocks
        // as it is now finished.
        this.activeBlocks.updateAndGet(blocks -> {
        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
        newBlocks.remove(active.block.getName());
        return newBlocks;
        });
        } else {
        // There is no replacement so the block needs to be looped!
        long nextMeasureStart = this.getNextMeasureStart(activeEnd);
        System.out.printf("Executing for %d with current stop=%d, activeEnd=%d%n", nextMeasureStart, stop, activeEnd);
        this.startExecutingBlock(active, nextMeasureStart);
        }
        }
        });
     */
    public void updateBlocks(Collection<MellowDBlock> blocks) {
        this.activeBlocks.updateAndGet(oldBlocks -> {
            Map<String, ActiveBlock> newBlocks = new HashMap<>(oldBlocks.size());

            blocks.forEach(newBlock -> {
                ActiveBlock oldActiveBlock = oldBlocks.get(newBlock.getName());
                Statement[] newCode = newBlock.getCode();
                newBlock.clearCode();
                if (oldActiveBlock != null) {
                    newBlocks.put(newBlock.getName(), new ActiveBlock(newBlock, newCode, oldActiveBlock.activeBuffer, null, ActiveState.INITIALIZING));
                } else {
                    ActiveBlock newActiveBlock = new ActiveBlock(newBlock, newCode, null, null, ActiveState.INITIALIZING);
                    newBlocks.put(newBlock.getName(), newActiveBlock);
                    //TODO ? this.startExecutingBlock(newActiveBlock, this.stateTime.get());
                }
            });

            oldBlocks.forEach((name, block) -> {
                if (!newBlocks.containsKey(name)) {
                    newBlocks.put(name, block);
                }
            });

            return newBlocks;
        });

        Set<String> updated = blocks.stream().map(MellowDBlock::getName).collect(Collectors.toSet());

        // TODO this resets all blocks to the next measure, the puller should start at some offset measure
//        this.activeBlocks.get().values().stream()
//                .filter(e -> updated.contains(e.block.getName()))
//                .forEach(e ->
//                        //this.startExecutingBlock(e, this.getNextMeasureStart(e.activeBuffer != null ? e.activeBuffer.endTimeStamp() : -1)));
//                        this.startExecutingBlock(e, this.getNextMeasureStart(e.activeBuffer != null ? e.activeBuffer.endTimeStamp() : -1, e.block.getSchedulerDirectives()))
//                );
    }


//    public void replaceActiveTracks(Collection<MIDITrack> tracks) {
//        this.activeTracks.updateAndGet(old -> {
//            Map<String, MIDITrack> newTracks = new LinkedHashMap<>();
//            old.forEach(ot -> newTracks.put(ot.getName(), ot));
//            tracks.forEach(nt -> newTracks.put(nt.getName(), nt));
//            return newTracks.values();
//        });
//    }
}
