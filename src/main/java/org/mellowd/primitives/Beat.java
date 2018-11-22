//Beat
//====

package org.mellowd.primitives;

import org.mellowd.intermediate.functions.operations.Slurrable;

//A `Beat` represent a classical definition of a note duration.
//Each beat has the number of quarter notes it is equivalent to (possibly a fraction),
//for converting from `PPQN` (endTimeStamp per quarter note) to a duration in endTimeStamp that the
//beat should be held for.
public class Beat implements Slurrable<Beat> {
    //All beats must start out as one of the following durations which can then
    //later on be manipulated via dots to extend the duration or wrapping inside a tuplet.
    public static Beat WHOLE()          { return new Beat(4d);   }
    public static Beat HALF()           { return new Beat(2d);   }
    public static Beat QUARTER()        { return new Beat(1d);   }
    public static Beat EIGHTH()         { return new Beat(1/2d); }
    public static Beat SIXTEENTH()      { return new Beat(1/4d); }
    public static Beat THIRTYSECOND()   { return new Beat(1/8d); }
    
    public static final Beat ZERO = new Beat(0d);

    public static int compare(Beat left, Beat right) {
        return (int) (left.numQuarters - right.numQuarters);
    }

    private final double numQuarters;
    private boolean slurred;

    public Beat(double numQuarters) {
        this.numQuarters = numQuarters;
    }

    public Beat(double numQuarters, boolean slurred) {
        this.numQuarters = numQuarters;
        this.slurred = slurred;
    }

    //Each dot following a beat extends the duration by the original duration
    //times <sup>1</sup>&frasl;<sub>2<sup>n</sup></sub>. Where `n` is the index
    //of the dot. This means that a quarter note with 3 dots has the equivalent duration
    //of
    //<pre>
    //  1 + 1&middot;<sup>1</sup>&frasl;<sub>2<sup>1</sup></sub> + 1&middot;<sup>1</sup>&frasl;<sub>2<sup>2</sup></sub> + 1&middot;<sup>1</sup>&frasl;<sub>2<sup>3</sup></sub>
    //  = 1 + 1&middot;<sup>1</sup>&frasl;<sub>2</sub> + 1&middot;<sup>1</sup>&frasl;<sub>4</sub> + 1&middot;<sup>1</sup>&frasl;<sub>8</sub>
    //  = 1 <sup>6</sup>&frasl;<sub>8</sub>
    //  = 1 <sup>3</sup>&frasl;<sub>4</sub> quarter notes
    //</pre>
    public Beat dot(int amount) {
        double numQuarters = this.numQuarters;
        double prevAdded = this.numQuarters;
        for (int i = 0; i < amount; i++) {
            numQuarters += prevAdded/2;
        }
        return new Beat(numQuarters);
    }

    //A tuplet is a sequence of equivalent notes played in the time it takes
    //to play fewer of those notes. A 5:3 quarter note tuplet plays 5 quarter
    //notes in the time it takes to play 3. A common tuplet is a triplet.

    //A triplet squeezes 3 of the wrapped beat into the same time frame
    //that 2 of that beat would fit. For example an eighth note triplet
    //will result in the performance of 3 eighth notes in 2 eight notes,
    //equivalent of a quarter note. This is the same as scaling down the
    //`numQuarters` by <sup>2</sup>&frasl;<sub>3</sub> or <sup>div</sup>&fracsl;<sub>num</sub>

    //For example: an eight note is <sup>1</sup>&frasl;<sub>2</sub> quarter notes.
    //<pre>
    //<sup>1</sup>&frasl;<sub>2</sub> + <sup>1</sup>&frasl;<sub>2</sub> = 1 quarter note
    //</pre>

    //Now if we want an eight note triple we need to fit 3 eight notes into a quarter note
    //<pre>
    //<sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> + <sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> + <sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> = <sup>6</sup>&frasl;<sub>6</sub> = 1 quarter note
    //</pre>
    public Beat tuplet(int num) {
        if (num <= 1)
            throw new IllegalArgumentException("Cannot create a tuplet of "+num);
        return tuplet(num, num - 1);
    }

    public Beat tuplet(int num, int div) {
        if (num <= 0 || div <= 0)
            throw new IllegalArgumentException("Cannot create a tuplet of "+num+":"+div);
        return new Beat(numQuarters * (div / (double) num));
    }

    public Beat add(Beat other) {
        return new Beat(this.numQuarters + other.numQuarters);
    }

    public Beat times(int amt) {
        return new Beat(this.numQuarters * amt);
    }

    public double getNumQuarters() {
        return this.numQuarters;
    }

    public boolean isSlurred() {
        return slurred;
    }

    @Override
    public Beat toggleSlur() {
        return new Beat(this.numQuarters, !this.slurred);
    }

    @Override
    public String toString() {
        String str;
        if (this.numQuarters == 4d) {
            str = "w";
        } else if (this.numQuarters == 2d) {
            str = "h";
        } else if (this.numQuarters == 1d) {
            str = "q";
        } else if (this.numQuarters == 1/2d) {
            str = "e";
        } else if (this.numQuarters == 1/4d) {
            str = "s";
        } else if (this.numQuarters == 1/8d) {
            return "t";
        } else {
            str = String.format("r{%.2f}", this.numQuarters);
        }
        return isSlurred() ? str + "_" : str;
    }
}
