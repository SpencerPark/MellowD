package org.mellowd.primitives;

import org.mellowd.intermediate.Sound;

import java.util.Objects;

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

    @Override
    public Articulation getArticulation() {
        return articulation;
    }

    @Override
    public void setArticulation(Articulation articulation) {
        this.articulation = articulation;
    }

    @Override
    public Pitch getElement() {
        return pitch;
    }

    @Override
    public ArticulatedPitch shiftOctave(int octaveShift) {
        return new ArticulatedPitch(this.pitch.shiftOctave(octaveShift), this.articulation);
    }

    @Override
    public Sound createSound(Beat beat) {
        return Sound.newSound(this.pitch, beat, this.articulation);
    }

    @Override
    public Articulated articulate(Articulation articulation) {
        return new ArticulatedPitch(this.pitch, articulation);
    }

    @Override
    public String toString() {
        return this.pitch.toString() + this.articulation.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticulatedPitch that = (ArticulatedPitch) o;
        return Objects.equals(pitch, that.pitch) &&
                articulation == that.articulation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pitch, articulation);
    }
}
