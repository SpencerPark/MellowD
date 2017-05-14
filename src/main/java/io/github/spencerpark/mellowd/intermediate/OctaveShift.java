package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.MIDIChannel;

public class OctaveShift implements Playable {
    private final int octaveShift;

    public OctaveShift(int octaveShift) {
        this.octaveShift = octaveShift;
    }

    public int getOctaveShift() {
        return octaveShift;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.setOctaveShift(this.octaveShift);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
