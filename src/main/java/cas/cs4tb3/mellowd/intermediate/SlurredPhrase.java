package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.MIDIChannel;

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
        channel.setSlurred(true);
        super.play(channel);
        channel.setSlurred(false);
    }
}
