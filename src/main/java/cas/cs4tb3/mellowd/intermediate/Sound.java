package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.Beat;
import cas.cs4tb3.mellowd.Pitch;
import cas.cs4tb3.mellowd.primitives.Chord;

import java.util.Collections;
import java.util.List;

/**
 * A plain sound. This sound simple turns the notes on and then off after
 * the duration of the beat has passed.
 */
public class Sound implements Playable {
    protected final List<Pitch> pitches;
    protected final Beat duration;

    public Sound(Chord chord, Beat duration) {
        this.pitches = chord.getPitches();
        this.duration = duration;
    }

    public Sound(Pitch pitch, Beat duration) {
        this.pitches = Collections.singletonList(pitch);
        this.duration = duration;
    }

    public Beat getDuration() {
        return duration;
    }

    @Override
    public void play(MIDIChannel channel) {
        pitches.forEach(channel::noteOn);
        channel.stepIntoFuture(duration);
        pitches.forEach(channel::noteOff);
    }
}
