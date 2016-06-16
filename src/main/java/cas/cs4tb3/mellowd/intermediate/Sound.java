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
    private static final Beat SLUR_EXTENSION = Beat.EIGHTH;
    private final List<Pitch> pitches;
    private final Beat duration;
    private Sound next;

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

    public void setNext(Sound next) {
        this.next = next;
    }

    public boolean isChord() {
        return getNumNotes() > 1;
    }

    protected void notesOn(MIDIChannel channel, long delay, int volumeIncrease) {
        channel.doLater(delay, () -> pitches.forEach(p -> {
            if (channel.isSlurred() && channel.isNoteOn(p)) return;
            channel.noteOn(p, volumeIncrease);
        }));
    }

    protected void noteOn(MIDIChannel channel, long delay, int noteIndex, int volumeIncrease) {
        channel.doLater(delay, () -> {
            Pitch p = pitches.get(noteIndex);
            if (channel.isSlurred() && channel.isNoteOn(p)) return;
            channel.noteOn(p, volumeIncrease);
        });
    }

    protected void notesOff(MIDIChannel channel, long delay, int offVolume) {
        if (channel.isSlurred()) delay += channel.ticksInBeat(SLUR_EXTENSION);
        channel.doLater(delay, () -> pitches.forEach(p -> {
            if (next != null && channel.isSlurred() && next.pitches.contains(p)) return;
            channel.noteOff(p, offVolume);
        }));
    }

    protected void noteOff(MIDIChannel channel, long delay, int noteIndex, int offVolume) {
        if (channel.isSlurred()) delay += channel.ticksInBeat(SLUR_EXTENSION);
        channel.doLater(delay, () -> {
            Pitch p = pitches.get(noteIndex);
            if (next != null && channel.isSlurred() && next.pitches.contains(p)) return;
            channel.noteOff(p, offVolume);
        });
    }

    protected long getDuration(MIDIChannel channel) {
        return channel.ticksInBeat(this.duration);
    }

    protected void advanceDuration(MIDIChannel channel) {
        channel.stepIntoFuture(this.duration);
    }

    @Override
    public void play(MIDIChannel channel) {
        notesOn(channel, 0, 0);
        notesOff(channel, getDuration(channel), MIDIChannel.DEFAULT_OFF_VELOCITY);
        advanceDuration(channel);
    }
}
