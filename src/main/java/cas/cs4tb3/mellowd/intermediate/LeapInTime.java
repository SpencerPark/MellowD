package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

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
