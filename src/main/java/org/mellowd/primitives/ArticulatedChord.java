package org.mellowd.primitives;

import org.mellowd.intermediate.Sound;
import org.mellowd.intermediate.functions.operations.Indexable;

import java.util.Objects;

public class ArticulatedChord implements Articulated, Indexable<Pitch, Chord> {
    private final Chord chord;
    private Articulation articulation;

    public ArticulatedChord(Chord chord, Articulation articulation) {
        this.chord = chord;
        this.articulation = articulation;
    }

    public ArticulatedChord(Chord chord) {
        this.chord = chord;
        this.articulation = Articulation.NONE;
    }

    @Override
    public Chord getElement() {
        return chord;
    }

    @Override
    public Articulation getArticulation() {
        return articulation;
    }

    @Override
    public void setArticulation(Articulation articulation) {
        this.articulation = articulation;
    }

    @Override
    public ArticulatedChord shiftOctave(int octaveShift) {
        return new ArticulatedChord(this.chord.shiftOctave(octaveShift), this.articulation);
    }

    @Override
    public Sound createSound(Beat beat) {
        return Sound.newSound(this.chord, beat, this.articulation);
    }

    @Override
    public Articulated articulate(Articulation articulation) {
        return new ArticulatedChord(this.chord, articulation);
    }

    @Override
    public Pitch getAtIndex(int index) {
        return this.chord.getAtIndex(index);
    }

    @Override
    public Chord getAtRange(int lower, int upper) {
        return this.chord.getAtRange(lower, upper);
    }

    @Override
    public String toString() {
        return this.chord.toString() + this.articulation.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticulatedChord that = (ArticulatedChord) o;
        return Objects.equals(chord, that.chord) &&
                articulation == that.articulation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chord, articulation);
    }
}
