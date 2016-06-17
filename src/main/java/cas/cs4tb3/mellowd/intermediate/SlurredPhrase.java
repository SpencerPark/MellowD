package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.midi.MIDIControl;
import cas.cs4tb3.mellowd.midi.Pedal;

/**
 * Created on 2016-06-15.
 */
public class SlurredPhrase extends Phrase {
    public SlurredPhrase() {
    }

    public SlurredPhrase(Phrase toCopy) {
        super(toCopy);
    }

    @Override
    public void play(MIDIChannel channel) {
        Pedal sustain = channel.getController(MIDIControl.SUSTAIN);
        sustain.press();
        channel.setSlurred(true);

        super.play(channel);

        channel.setSlurred(false);
        sustain.release();
    }
}
