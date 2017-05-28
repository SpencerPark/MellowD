package org.mellowd.primitives;

import org.mellowd.intermediate.Sound;
import org.mellowd.intermediate.functions.operations.Articulatable;
import org.mellowd.intermediate.functions.operations.OctaveShiftable;

public interface Articulated extends OctaveShiftable<Articulated>, Articulatable {

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);

    Articulatable getElement();
}
