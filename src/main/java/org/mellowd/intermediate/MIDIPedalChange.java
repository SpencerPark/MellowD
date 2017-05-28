package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.MIDIControl;
import org.mellowd.midi.Pedal;

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
