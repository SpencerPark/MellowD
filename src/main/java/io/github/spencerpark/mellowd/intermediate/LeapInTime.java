package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.MIDIChannel;

public class LeapInTime implements Playable {
    private final long ticks;

    public LeapInTime(long ticks) {
        this.ticks = ticks;
    }

    public long getTicks() {
        return ticks;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.stepIntoFuture(ticks);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return this.ticks;
    }
}
