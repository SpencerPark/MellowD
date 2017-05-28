package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.midi.MIDIChannel;

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
