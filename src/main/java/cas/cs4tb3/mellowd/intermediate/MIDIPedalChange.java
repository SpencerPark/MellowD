package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.midi.MIDIControl;
import cas.cs4tb3.mellowd.midi.Pedal;

public class MIDIPedalChange implements Playable {
    private final MIDIControl<Pedal> control;
    private final boolean press;

    public MIDIPedalChange(MIDIControl<Pedal> control, boolean press) {
        this.control = control;
        this.press = press;
    }

    @Override
    public void play(MIDIChannel channel) {
        if (press) channel.getController(control).press();
        else       channel.getController(control).release();
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
