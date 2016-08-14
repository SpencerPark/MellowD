package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

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
