package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Articulatable;
import cas.cs4tb3.mellowd.intermediate.functions.operations.OctaveShiftable;

public interface Articulated extends OctaveShiftable<Articulated>, Articulatable {

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);

    Articulatable getElement();
}
