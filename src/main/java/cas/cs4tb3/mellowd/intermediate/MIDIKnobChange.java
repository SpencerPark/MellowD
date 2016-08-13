package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.Knob;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.midi.MIDIControl;

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
