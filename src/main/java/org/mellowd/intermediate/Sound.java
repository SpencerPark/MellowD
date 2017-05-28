package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.primitives.Articulation;
import org.mellowd.primitives.Beat;
import org.mellowd.primitives.Pitch;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.primitives.Chord;

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

    public boolean isChord() {
        return getNumNotes() > 1;
    }

    protected long getDuration(MIDIChannel channel) {
        return channel.ticksInBeat(this.duration);
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.playNotes(pitches, 0, duration, MIDIChannel.DEFAULT_OFF_VELOCITY);
        channel.stepIntoFuture(this.duration);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return env.ticksInBeat(this.duration);
    }

    @Override
    public String toString() {
        return "Sound{" +
                "pitches=" + pitches +
                ", duration=" + duration +
                '}';
    }
}
