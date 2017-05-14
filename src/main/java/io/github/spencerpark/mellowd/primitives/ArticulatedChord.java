package io.github.spencerpark.mellowd.primitives;

import io.github.spencerpark.mellowd.intermediate.Sound;

public class ArticulatedChord implements Articulated {
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
}
