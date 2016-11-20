package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;
import cas.cs4tb3.mellowd.intermediate.functions.operations.OctaveShiftable;

public interface Articulated extends ConcatableComponent.TypeMelody, OctaveShiftable<Articulated> {

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);

    Articulatable getElement();

    @Override
    default void appendTo(Melody root) {
        root.add(this);
    }
}
