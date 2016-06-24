package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;

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

    public Chord getChord() {
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

    //Overloading groovy operators
    public Pitch getAt(int index) {
        return chord.getPitchAt(index);
    }

    public ArticulatedChord plus(int transposeAmt) {
        return new ArticulatedChord(this.chord.transpose(transposeAmt), this.articulation);
    }

    public ArticulatedChord minus(int transposeAmt) {
        return new ArticulatedChord(this.chord.transpose(-transposeAmt), this.articulation);
    }

    public ArticulatedChord leftShift(int octaveShift) {
        return shiftOctave(-octaveShift);
    }

    public ArticulatedChord rightShift(int octaveShift) {
        return shiftOctave(octaveShift);
    }

    //End groovy overloading

    @Override
    public ArticulatedChord shiftOctave(int octaveShift) {
        return new ArticulatedChord(this.chord.shiftOctave(octaveShift), this.articulation);
    }

    @Override
    public Sound createSound(Beat beat) {
        return Sound.newSound(this.chord, beat, this.articulation);
    }
}
