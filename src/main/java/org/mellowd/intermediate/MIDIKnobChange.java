package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.midi.Knob;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.MIDIControl;

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
