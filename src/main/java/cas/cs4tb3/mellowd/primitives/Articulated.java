package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;

public interface Articulated extends ConcatableComponent.TypeMelody {

    Articulated shiftOctave(int octaveShift);

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);

    @Override
    default void appendTo(Melody root) {
        root.add(this);
    }
}
