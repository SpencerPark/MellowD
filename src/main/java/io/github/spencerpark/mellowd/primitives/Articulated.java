package io.github.spencerpark.mellowd.primitives;

import io.github.spencerpark.mellowd.intermediate.Sound;
import io.github.spencerpark.mellowd.intermediate.functions.operations.Articulatable;
import io.github.spencerpark.mellowd.intermediate.functions.operations.OctaveShiftable;

public interface Articulated extends OctaveShiftable<Articulated>, Articulatable {

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);

    Articulatable getElement();
}
