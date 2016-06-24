package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.primitives.Articulation;
import cas.cs4tb3.mellowd.primitives.Beat;
import cas.cs4tb3.mellowd.primitives.Pitch;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.primitives.Chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plain sound. This sound simple turns the notes on and then off after
 * the duration of the beat has passed.
 */
public class Sound implements Playable {
    public static Sound newSound(Chord chord, Beat beat, Articulation articulation) {
        switch (articulation) {
            default:
                return new Sound(chord, beat);
            case STACCATO:
                return new ArticulatedSound.Staccato(chord, beat);
            case STACCATISSIMO:
                return new ArticulatedSound.Staccatissimo(chord, beat);
            case MARCATO:
                return new ArticulatedSound.Marcato(chord, beat);
            case ACCENT:
                return new ArticulatedSound.Accent(chord, beat);
            case TENUTO:
                return new ArticulatedSound.Tenuto(chord, beat);
            case GLISCANDO:
                return new ArticulatedSound.Gliscando(chord, beat);
        }
    }

    public static Sound newSound(Pitch pitch, Beat beat, Articulation articulation) {
        switch (articulation) {
            default:
                return new Sound(pitch, beat);
            case STACCATO:
                return new ArticulatedSound.Staccato(pitch, beat);
            case STACCATISSIMO:
                return new ArticulatedSound.Staccatissimo(pitch, beat);
            case MARCATO:
                return new ArticulatedSound.Marcato(pitch, beat);
            case ACCENT:
                return new ArticulatedSound.Accent(pitch, beat);
            case TENUTO:
                return new ArticulatedSound.Tenuto(pitch, beat);
            case GLISCANDO:
                return new ArticulatedSound.Gliscando(pitch, beat);
        }
    }

    private static final Beat SLUR_EXTENSION = Beat.EIGHTH;
    private final List<Pitch> pitches;
    private final Beat duration;
    private transient Sound next;

    public Sound(Chord chord, Beat duration) {
        this.pitches = chord.getPitches();
        this.duration = duration;
    }

    public Sound(Pitch pitch, Beat duration) {
        this.pitches = Collections.singletonList(pitch);
        this.duration = duration;
    }

    private Sound(List<Pitch> pitches, Beat duration) {
        this.pitches = pitches;
        this.duration = duration;
    }

    public Sound shiftOctave(int octaveShift) {
        if (octaveShift == 0) return this;
        List<Pitch> pitches = new ArrayList<>(this.pitches.size());
        for (Pitch p : this.pitches) {
            pitches.add(p.shiftOctave(octaveShift));
        }
        return new Sound(pitches, this.duration);
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

    //TODO find a better way to leak this data to gliscand
    protected boolean isHigherInPitch(Sound other) {
        return !this.isChord() && !other.isChord() && this.pitches.get(0).getMidiNum() >= other.pitches.get(0).getMidiNum();
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

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return env.ticksInBeat(this.duration);
    }
}
