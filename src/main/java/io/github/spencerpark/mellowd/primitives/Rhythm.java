//Rhythm
//======

package io.github.spencerpark.mellowd.primitives;

import io.github.spencerpark.mellowd.intermediate.functions.operations.Indexable;
import io.github.spencerpark.mellowd.intermediate.functions.operations.Slurrable;

import java.util.*;

//The `Rhythm` is responsible for the timing considerations in playable sounds. It specifies
//the duration in the form of a [Beat](../Beat.html). In a Mellow D source file a rhythm is
//a comma separated list of beats wrapped in `<` and `>`. Each beat is referred to by the first
//letter in it's name. Additionally beats can be slurred together. This results in the durations
//overlapping and the notes connecting more smoothly.
public class Rhythm implements Slurrable, Indexable<Beat, Rhythm> {

    public static int compare(Rhythm left, Rhythm right) {
        for (int i = 0; i < left.size(); i++) {
            if (right.size() <= i) //Equal up to here but left is longer
                return 1; //Treat right[i] as null

            int cmp = Beat.compare(left.beats.get(i), right.beats.get(i));
            if (cmp != 0)
                return cmp;
        }

        //Equal up to the end of left
        if (right.size() > left.size())
            return -1; //Right has an extra beat

        //Totally equal
        return 0;
    }

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
    public Rhythm getAtRange(int lower, int upper) {
        Rhythm r = new Rhythm();

        int size = size();
        Indexable.forEachInRange(lower, upper, i -> r.append(this.beats.get(Indexable.calcIndex(i, size))));

        return r;
    }

    @Override
    public String toString() {
        StringJoiner str = new StringJoiner(", ", "<", ">");
        this.beats.forEach(beat -> str.add(beat.toString()));
        return str.toString();
    }
}
