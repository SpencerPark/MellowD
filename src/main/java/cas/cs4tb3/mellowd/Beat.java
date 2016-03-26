//Beat
//====

package cas.cs4tb3.mellowd;

//A `Beat` represent a classical definition of a note duration.
//Each beat has the number of quarter notes it is equivalent to (possibly a fraction),
//for converting from `PPQN` (ticks per quarter note) to a duration in ticks that the
//beat should be held for.
public class Beat {
    //All beats must start out as one of the following durations which can then
    //later on be manipulated via dots to extend the duration or wrapping inside a tuple.
    public static final Beat WHOLE          = new Beat(4d);
    public static final Beat HALF           = new Beat(2d);
    public static final Beat QUARTER        = new Beat(1d);
    public static final Beat EIGHTH         = new Beat(1/2d);
    public static final Beat SIXTEENTH      = new Beat(1/4d);
    public static final Beat THIRTYSECOND   = new Beat(1/8d);

    private final double numQuarters;

    private Beat(double numQuarters) {
        this.numQuarters = numQuarters;
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

    //A triplet squeezes 3 of the wrapped beat into the same time frame
    //that 2 of that beat would fit. For example an eighth note triplet
    //will result in the performance of 3 eighth notes in 2 eight notes,
    //equivalent of a quarter note. This is the same as scaling down the
    //`numQuarters` by <sup>2</sup>&frasl;<sub>3</sub>.

    //For example: an eight note is <sup>1</sup>&frasl;<sub>2</sub> quarter notes.
    //<pre>
    //<sup>1</sup>&frasl;<sub>2</sub> + <sup>1</sup>&frasl;<sub>2</sub> = 1 quarter note
    //</pre>

    //Now if we want an eight note triple we need to fit 3 eight notes into a quarter note
    //<pre>
    //<sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> + <sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> + <sup>1</sup>&frasl;<sub>2</sub> &middot; <sup>2</sup>&frasl;<sub>3</sub> = <sup>6</sup>&frasl;<sub>6</sub> = 1 quarter note
    //</pre>
    public Beat triplet() {
        return new Beat(numQuarters * 2/3);
    }

    public double getNumQuarters() {
        return this.numQuarters;
    }
}
