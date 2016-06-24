package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.Sound;

/**
 * Created on 2016-06-21.
 */
public interface Articulated {

    Articulated shiftOctave(int octaveShift);

    Sound createSound(Beat beat);

    Articulation getArticulation();

    void setArticulation(Articulation articulation);
}
