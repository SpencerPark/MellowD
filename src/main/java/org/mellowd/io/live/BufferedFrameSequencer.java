package org.mellowd.io.live;

import org.mellowd.io.Compiler;
import org.mellowd.midi.TimingEnvironment;

import javax.sound.midi.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiConsumer;
import java.util.function.ObjLongConsumer;

public class BufferedFrameSequencer {
    private final MidiEvent EOT;

    private final Sequencer sequencer;
    private final Sequence buffer;
    private final long frameDuration;
    private AtomicLong absPosition;

    private final AtomicReferenceArray<Track> frames;

    public BufferedFrameSequencer(Sequencer sequencer, TimingEnvironment timingEnvironment, long frameDuration, int numFrames) {
        assert numFrames > 1;

        this.sequencer = sequencer;
        this.buffer = timingEnvironment.createSequence();
        this.frameDuration = frameDuration;
        this.absPosition = new AtomicLong();

        this.frames = new AtomicReferenceArray<>(numFrames);

        EOT = new MidiEvent(Compiler.EOT_MESSAGE, numFrames * frameDuration);
        try {
            Track metaTrack = this.buffer.getTracks()[0];
            metaTrack.add(EOT);

            sequencer.setSequence(this.buffer);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        sequencer.setLoopStartPoint(0);
        sequencer.setLoopEndPoint(numFrames * frameDuration);
    }

    public long getFrameDuration() {
        return this.frameDuration;
    }

    public long getBufferLength() {
        return this.frameDuration * this.frames.length();
    }

    private int getRelativeFrameIndex(long absPosition) {
        return (int) ((absPosition / this.frameDuration) % this.frames.length());
    }

    private long getAbsFrameIndex(long absPosition) {
        return absPosition / this.frameDuration;
    }

    private long getAbsFirstFrameIndex(long absPosition) {
        long absFrameIdx = this.getAbsFrameIndex(absPosition);
        long relFrameIdx = absFrameIdx % this.frames.length();
        return absFrameIdx - relFrameIdx;
    }

    public long getAbsSequenceStartTicks(long absPosition) {
        return this.getAbsFirstFrameIndex(absPosition) * this.frameDuration;
    }

    /**
     * @param frameConsumer schedules a message in this frame (unchecked) with an absolute time
     *                      since the start of the sequence
     */
    public long writeNextFrame(BiConsumer<Long, ObjLongConsumer<MidiMessage>> frameConsumer) {
        long position = this.absPosition.getAndAdd(this.frameDuration);
        int frameIdx = this.getRelativeFrameIndex(position);

        Track newTrack = this.buffer.createTrack();
        Track oldTrack = this.frames.getAndSet(frameIdx, newTrack);
        if (oldTrack != null)
            this.buffer.deleteTrack(oldTrack);

        frameConsumer.accept(position, (message, time) ->
            newTrack.add(new MidiEvent(message, time - position)));

        try {
            this.sequencer.setSequence(this.buffer);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
        return position;
    }
}
