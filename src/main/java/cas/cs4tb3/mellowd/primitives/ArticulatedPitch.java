package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;

public class ArticulatedPitch implements Articulated {
    private final Pitch pitch;
    private Articulation articulation;

    public ArticulatedPitch(Pitch pitch, Articulation articulation) {
        this.pitch = pitch;
        this.articulation = articulation;
    }

    public ArticulatedPitch(Pitch pitch) {
        this.pitch = pitch;
        this.articulation = Articulation.NONE;
    }

    public Pitch getPitch() {
        return pitch;
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

    public ArticulatedPitch plus(int transposeAmt) {
        return new ArticulatedPitch(this.pitch.transpose(transposeAmt), this.articulation);
    }

    public ArticulatedPitch minus(int transposeAmt) {
        return new ArticulatedPitch(this.pitch.transpose(-transposeAmt), this.articulation);
    }

    public ArticulatedPitch leftShift(int octaveShift) {
        return shiftOctave(-octaveShift);
    }

    public ArticulatedPitch rightShift(int octaveShift) {
        return shiftOctave(octaveShift);
    }

    public Object asType(Class<?> type) {
        if (type.equals(Pitch.class)) {
            return this.pitch;
        }

        return type.cast(this);
    }

    //End groovy overloading

    @Override
    public ArticulatedPitch shiftOctave(int octaveShift) {
        return new ArticulatedPitch(this.pitch.shiftOctave(octaveShift), this.articulation);
    }

    @Override
    public Sound createSound(Beat beat) {
        return Sound.newSound(this.pitch, beat, this.articulation);
    }

}
