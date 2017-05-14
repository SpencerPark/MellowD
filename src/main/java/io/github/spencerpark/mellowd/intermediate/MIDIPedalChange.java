package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.MIDIChannel;
import io.github.spencerpark.mellowd.midi.MIDIControl;
import io.github.spencerpark.mellowd.midi.Pedal;

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
