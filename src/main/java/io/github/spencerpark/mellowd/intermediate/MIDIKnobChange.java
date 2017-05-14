package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.Knob;
import io.github.spencerpark.mellowd.midi.MIDIChannel;
import io.github.spencerpark.mellowd.midi.MIDIControl;

public class MIDIKnobChange implements Playable {
    private final MIDIControl<Knob> control;
    private final int setting;

    public MIDIKnobChange(MIDIControl<Knob> control, int setting) {
        this.control = control;
        this.setting = setting;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.getController(control).twist(setting);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
