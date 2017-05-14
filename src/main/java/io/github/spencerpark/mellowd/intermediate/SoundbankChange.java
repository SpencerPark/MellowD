package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.MIDIChannel;

public class SoundbankChange implements Playable {
    private final int soundbank;

    public SoundbankChange(int soundbank) {
        this.soundbank = soundbank;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.setSoundBank(soundbank);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
