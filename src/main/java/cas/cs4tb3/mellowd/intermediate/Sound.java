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
    private final List<Pitch> pitches;
    private final Beat duration;

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

    public int getNumNotes() {
        return pitches.size();
    }

    public boolean isChord() {
        return getNumNotes() > 1;
    }

    protected void notesOn(MIDIChannel channel, long delay, int volumeIncrease) {
        channel.doLater(delay, () -> pitches.forEach(p -> channel.noteOn(p, volumeIncrease)));
    }

    protected void noteOn(MIDIChannel channel, long delay, int noteIndex, int volumeIncrease) {
        channel.doLater(delay, () -> channel.noteOn(pitches.get(noteIndex), volumeIncrease));
    }

    protected void notesOff(MIDIChannel channel, long delay, int offVolume) {
        channel.doLater(delay, () -> pitches.forEach(p -> channel.noteOff(p, offVolume)));
    }

    protected void noteOff(MIDIChannel channel, long delay, int noteIndex, int offVolume) {
        channel.doLater(delay, () -> channel.noteOff(pitches.get(noteIndex), offVolume));
    }

    protected long getDuration(MIDIChannel channel) {
        return channel.ticksInBeat(this.duration);
    }

    protected void advanceDuration(MIDIChannel channel) {
        channel.stepIntoFuture(this.duration);
    }

    @Override
    public void play(MIDIChannel channel) {
        pitches.forEach(channel::noteOn);
        channel.stepIntoFuture(duration);
        pitches.forEach(channel::noteOff);
    }
}
