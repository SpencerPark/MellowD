package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.primitives.Beat;
import cas.cs4tb3.mellowd.primitives.Melody;
import cas.cs4tb3.mellowd.primitives.Rhythm;

public class RepeatedPhrase extends Phrase {
    protected final int repetitions;

    public RepeatedPhrase(Melody melody, Rhythm rhythm, int repetitions) {
        super(melody, rhythm);
        this.repetitions = repetitions;
    }

    public int getRepetitions() {
        return repetitions;
    }

    @Override
    public Beat getDuration() {
        return new Beat(super.getDuration().getNumQuarters() * repetitions);
    }

    @Override
    public void play(MIDIChannel channel) {
        for (int i = 0; i < repetitions; i++)
            super.play(channel);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return super.calculateDuration(env) * repetitions;
    }
}
