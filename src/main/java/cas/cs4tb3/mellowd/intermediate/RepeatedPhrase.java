package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.MIDIChannel;

public class RepeatedPhrase extends Phrase {
    protected final int repetitions;

    public RepeatedPhrase(int repetitions) {
        this.repetitions = repetitions;
    }

    public RepeatedPhrase(int repetitions, Phrase toWrap) {
        super(toWrap);
        this.repetitions = repetitions;
    }

    public int getRepetitions() {
        return repetitions;
    }

    @Override
    public void play(MIDIChannel channel) {
        for (int i = 0; i < repetitions; i++)
            super.play(channel);
    }
}
