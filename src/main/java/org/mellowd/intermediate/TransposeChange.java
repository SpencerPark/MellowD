package org.mellowd.intermediate;

import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.TimingEnvironment;

public class TransposeChange implements Playable {
    private final int semiToneShift;

    public TransposeChange(int semiToneShift) {
        this.semiToneShift = semiToneShift;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.setTranspose(semiToneShift);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
