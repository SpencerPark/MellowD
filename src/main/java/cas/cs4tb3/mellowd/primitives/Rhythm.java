//Rhythm
//======

package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Slurrable;

import java.util.*;

//The `Rhythm` is responsible for the timing considerations in playable sounds. It specifies
//the duration in the form of a [Beat](../Beat.html). In a Mellow D source file a rhythm is
//a comma separated list of beats wrapped in `<` and `>`. Each beat is referred to by the first
//letter in it's name. Additionally beats can be slurred together. This results in the durations
//overlapping and the notes connecting more smoothly.
public class Rhythm implements Slurrable, Indexable<Beat> {
    private final List<Beat> beats;

    //Creating a rhythm is done by specifying zero or more beats that make up the rhythm.
    public Rhythm() {
        this.beats = new ArrayList<>();
    }

    public boolean isSlurred(int index) {
        return getAtIndex(Indexable.calcIndex(index, size())).isSlurred();
    }

    //`append` adds a beat to the rhythm. It is equivalent to calling `append(beat, false)`
    //as the added beat will not be slurred.
    public void append(Beat beat) {
        this.beats.add(beat);
    }

    public void append(Rhythm other) {
        this.beats.addAll(other.beats);
    }

    public void setSlurred(int index, boolean slurred) {
        getAtIndex(Indexable.calcIndex(index, size())).setSlurred(slurred);
    }

    @Override
    public void setSlurred(boolean slur) {
        slurAll();
    }

    public void slurAll() {
        this.beats.forEach(Beat::flipSlur);
    }

    public int size() {
        return this.beats.size();
    }

    public Beat getDuration() {
        double numQuarters = 0;

        for (Beat beat : this.beats)
            numQuarters += beat.getNumQuarters();

        return new Beat(numQuarters);
    }

    @Override
    public Beat getAtIndex(int index) {
        return this.beats.get(Indexable.calcIndex(index, size()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");

        for (Beat b : this.beats)
            sb.append(b).append(", ");

        if (sb.length() > 1)
            sb.setLength(sb.length() - 2);

        sb.append('>');

        return sb.toString();
    }
}
