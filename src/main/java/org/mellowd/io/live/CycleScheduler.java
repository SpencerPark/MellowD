package org.mellowd.io.live;

import org.mellowd.compiler.MellowDBlock;
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
        final MIDITrack activeTrack;
        final MIDITrack nextTrack;
        final ActiveState state;

        public ActiveBlock(MellowDBlock block, Statement[] code, MIDITrack activeTrack, MIDITrack nextTrack, ActiveState state) {
            this.block = block;
            this.code = code;
            this.activeTrack = activeTrack;
            this.nextTrack = nextTrack;
            this.state = state;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("ActiveBlock {\n");
            sb.append("\tblock = ").append(block).append('\n');
            sb.append("\tcode = ").append(Arrays.toString(code)).append('\n');
            sb.append("\tactiveTrack = ").append(activeTrack).append('\n');
            if (activeTrack != null) sb.append("\tstart = ").append(activeTrack.startTimeStamp()).append('\n');
            if (activeTrack != null) sb.append("\tend = ").append(activeTrack.endTimeStamp()).append('\n');
            sb.append("\tnextTrack = ").append(nextTrack).append('\n');
            sb.append("\tstate = ").append(state).append('\n');
            sb.append("}\n");
            return sb.toString();
        }
    }

    //private final Sequencer sequencer;
    private final TimingEnvironment timingEnvironment;
    private final Synthesizer synth;
    private final Receiver out;

    //private final AtomicReference<Collection<MIDITrack>> activeTracks;
    private final AtomicBoolean running;
    //private final BufferedFrameSequencer bufferedSequence;

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
        //this.sequencer = sequencer;
        this.timingEnvironment = timingEnvironment;
        this.synth = synth;
        this.out = synth.getReceiver();

        //this.activeTracks = new AtomicReference<>(Collections.emptyList());
        this.running = new AtomicBoolean(false);
        this.exceptionHandler = exceptionHandler;
        this.activeBlocks = new AtomicReference<>(Collections.emptyMap());

        frameDurationInBeats = Beat.EIGHTH();
        frameDurationInTicks = timingEnvironment.ticksInBeat(frameDurationInBeats);
        frameDurationInUs = timingEnvironment.approxDurationOfBeatInUs(frameDurationInBeats);

        measureDurationInBeats = timingEnvironment.getBeatValue().times(timingEnvironment.getBeatsPerMeasure());
        measureDurationTicks = timingEnvironment.ticksInBeat(measureDurationInBeats);

        this.setPriority(Thread.MAX_PRIORITY);

        //Beat frameDurationBeat = timingEnvironment.getBeatValue().times(timingEnvironment.getBeatsPerMeasure());
        //long frameDurationTicks = timingEnvironment.ticksInBeat(frameDurationBeat);
        //this.bufferedSequence = new BufferedFrameSequencer(sequencer, timingEnvironment, frameDurationTicks, 2);
    }

    @Override
    public synchronized void start() {
        this.running.set(true);
        //this.sequencer.start();
        super.start();
    }

    public void shutdown() {
        this.running.set(false);
    }

//    @Override
//    public void run() {
//        while (this.running.get()) {
//            // Schedule the next frame
//            long scheduledFrameStartTicks = this.bufferedSequence.writeNextFrame((start, addMessage) -> {
//                final long stop = start + this.bufferedSequence.getFrameDuration();
//
//                System.out.println();
//                System.out.println("Scheduling: " + start + " Current: " + this.sequencer.getTickPosition());
//                this.activeTracks.get().forEach(activeTrack ->
//                        activeTrack.forEachInRange(start, stop, (msg, time) -> {
//                            addMessage.accept(msg, time);
//                            System.out.print(time + " ");
//                        }));
//            });
//
//            // Sleep until the next frame starts, we have 1 frame to schedule enough messages
//            long posTicks = this.sequencer.getTickPosition();
//            long seqStartTicks = this.bufferedSequence.getAbsSequenceStartTicks(posTicks);
//            long absPosTicks = seqStartTicks + posTicks;
//            long waitTicks = Math.max(0, scheduledFrameStartTicks - absPosTicks);
//            long waitUs = this.timingEnvironment.ticksToUs(waitTicks);
//            try {
//                TimeUnit.MICROSECONDS.sleep(waitUs);
//            } catch (InterruptedException ignored) {
//                this.running.set(false);
//            }
//        }
//    }

    private long getNextMeasureStart(long after) {
        if (after < 0)
            after = this.stateTime.get();
        after--;
        return after + (measureDurationTicks - (after % measureDurationTicks));
    }

    @Override
    public void run() {
        //Beat frameDurationInBeats = timingEnvironment.getBeatValue().times(timingEnvironment.getBeatsPerMeasure());

        // TODO add drift cancellation if receiver transmits sync messages

        long clockStart = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
        //System.out.println(this.synth.getMicrosecondPosition());
        long synthOffset = Math.max(0, this.synth.getMicrosecondPosition()) + this.synth.getLatency();
        //System.out.println(synthOffset);
        long frameStartUs = clockStart;
        while (this.running.get()) {
            try {
                long start = stateTime.getAndAdd(frameDurationInTicks);
                long stop = start + frameDurationInTicks;
                //long nextStop = stop + frameDurationInTicks;
                //long nextMeasureStart = stop + (measureDurationTicks - (stop % measureDurationTicks));
                //System.out.printf("start: %d, stop: %d, nextMeasure: %d%n", start, stop, nextMeasureStart);

                ObjLongConsumer<MidiMessage> send = (msg, time) ->
                        out.send(msg, time + synthOffset);
                this.flushFrame(start, stop, send);

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

        //System.out.println("Compiling " + active.block.getName() + " @ " + startStateTime);

        this.activeBlocks.updateAndGet(blocks -> {
            Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
            newBlocks.computeIfPresent(active.block.getName(), (name, oldBlock) ->
                    new ActiveBlock(oldBlock.block, oldBlock.code, oldBlock.activeTrack, oldBlock.nextTrack, ActiveState.EXECUTING));
            return newBlocks;
        });

        MellowDBlock block = active.block;
        MIDITrack nextTrack = new MIDITrack(block.getName());

        // TODO save this future and cancel if a reeval before it gets scheduled
        // TODO the activeTrack should update it's start time to the frame that it actually ends
        // up running on incase it runs too late.
        this.compilationExecutorService.submit(() -> {
            MIDIChannel channel = block.getMIDIChannel();
            channel.setTrack(nextTrack);

            // Jump to the start of the measure
            channel.stepIntoFuture(startStateTime - channel.getStateTime());

            try {
                for (Statement statement : active.code)
                    statement.execute(block, block);
            } catch (Throwable t) {
                this.exceptionHandler.accept(block, t);
            }

            channel.finalizeEOT(Beat.ZERO);

            // TODO if too slow updating wait to replace?
            this.activeBlocks.updateAndGet(blocks -> {
                Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
                newBlocks.computeIfPresent(block.getName(), (name, oldBlock) ->
                        new ActiveBlock(block, active.code, active.activeTrack == null ? nextTrack : active.activeTrack, active.activeTrack == null ? null : nextTrack, ActiveState.READY));
                return newBlocks;
            });
        });
    }

    private void flushFrame(long start, long stop, ObjLongConsumer<MidiMessage> send) {
        this.activeBlocks.get().values().forEach(active -> {
            if (active.activeTrack != null) {
                active.activeTrack.forEachInRange(start, stop, (msg, time) -> {
                    if (MIDITrack.isNotMeta(msg)) {
                        //System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                        send.accept(msg, this.timingEnvironment.ticksToUs(time));
                    }
                });
            }

            long activeEnd = active.activeTrack == null ? 0 : active.activeTrack.endTimeStamp();
            if (active.nextTrack != null) {
                // TODO this currently merges the tracks?
                // There is a replacement and this one finished mid frame
                active.nextTrack.forEachInRange(start/*activeEnd*/, stop, (msg, time) -> {
                    if (MIDITrack.isNotMeta(msg)) {
                        //System.out.println(time + " @ " + DatatypeConverter.printHexBinary(msg.getMessage()));
                        send.accept(msg, this.timingEnvironment.ticksToUs(time));
                    }
                });
                if (activeEnd < stop && active.nextTrack != null) {
                    this.activeBlocks.updateAndGet(blocks -> {
                        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
                        newBlocks.put(active.block.getName(), new ActiveBlock(active.block, active.code, active.nextTrack, null, ActiveState.READY));
                        return newBlocks;
                    });
                }
            }

            if (active.nextTrack == null) {
                if (active.state == ActiveState.SHUTTING_DOWN) {
                    this.activeBlocks.updateAndGet(blocks -> {
                        Map<String, ActiveBlock> newBlocks = new HashMap<>(blocks);
                        newBlocks.remove(active.block.getName());
                        return newBlocks;
                    });
                } else {
                    // Recompile the block
                    //this.startExecutingBlock(active, nextMeasureStart);
                    long nextMeasureStartPrime = this.getNextMeasureStart(active.activeTrack != null ? active.activeTrack.endTimeStamp() : -1);
                    this.startExecutingBlock(active, nextMeasureStartPrime);
                }
            }
        });
    }

    public void updateBlocks(Collection<MellowDBlock> blocks) {
        this.activeBlocks.updateAndGet(oldBlocks -> {
            Map<String, ActiveBlock> newBlocks = new HashMap<>(oldBlocks.size());

            blocks.forEach(newBlock -> {
                ActiveBlock oldActiveBlock = oldBlocks.get(newBlock.getName());
                Statement[] newCode = newBlock.getCode();
                newBlock.clearCode();
                if (oldActiveBlock != null) {
                    newBlocks.put(newBlock.getName(), new ActiveBlock(newBlock, newCode, oldActiveBlock.activeTrack, null, ActiveState.INITIALIZING));
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
        this.activeBlocks.get().values().stream()
                .filter(e -> updated.contains(e.block.getName()))
                .forEach(e ->
                        //this.startExecutingBlock(e, this.getNextMeasureStart(e.activeTrack != null ? e.activeTrack.endTimeStamp() : -1)));
                        this.startExecutingBlock(e, this.getNextMeasureStart(e.activeTrack != null ? e.activeTrack.endTimeStamp() : -1)));
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
