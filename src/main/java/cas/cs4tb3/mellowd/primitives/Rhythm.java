//Rhythm
//======

package cas.cs4tb3.mellowd.primitives;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

//The `Rhythm` is responsible for the timing considerations in playable sounds. It specifies
//the duration in the form of a [Beat](../Beat.html). In a Mellow D source file a rhythm is
//a comma separated list of beats wrapped in `<` and `>`. Each beat is referred to by the first
//letter in it's name. Additionally beats can be slurred together. This results in the durations
//overlapping and the notes connecting more smoothly.
public class Rhythm implements ConcatableComponent.TypeRhythm {
    private final List<Beat> beats;
    private final BitSet slurred;

    //Creating a rhythm is done by specifying zero or more beats that make up the rhythm.
    public Rhythm() {
        this.beats = new ArrayList<>();
        this.slurred = new BitSet();
    }

    public Rhythm(Beat onlyBeat, boolean slurred) {
        this.beats = Collections.singletonList(onlyBeat);
        this.slurred = new BitSet();
        this.slurred.set(0, slurred);
    }

    //Rhythm also defines some methods for manipulating and retrieving the data
    //stored in this class.

    //`getBeat` returns the beat at a given position in the rhythm. This will throw
    //an index out of bounds exception if the `index` is &lt; 0 or &ge; numBeats().
    public Beat getBeat(int index){
        return beats.get(index);
    }

    public boolean isSlurred(int index) {
        return slurred.get(index);
    }

    //`append` adds a beat to the rhythm. It is equivalent to calling `append(beat, false)`
    //as the added beat will not be slurred.
    public void append(Beat beat) {
        this.beats.add(beat);
        this.slurred.set(this.beats.size()-1, beat.isSlurred());
    }

    public void append(Rhythm other) {
        int startingIndex = this.beats.size();
        this.beats.addAll(other.beats);
        for (int i = 0; i < other.size(); i++) {
            this.slurred.set(startingIndex + i, other.isSlurred(i));
        }
    }

    public void setSlurred(int index, boolean slurred) {
        this.slurred.set(index, slurred);
    }

    @Override
    public void setSlurred(boolean slur) {
        slurAll();
    }

    public void slurAll() {
        this.slurred.flip(0, this.beats.size());
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
    public void appendTo(Rhythm root) {
        root.append(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        for (int i = 0; i < this.size(); i++) {
            sb.append(this.beats.get(i));
            if (this.slurred.get(i))
                sb.append('_');
            sb.append(", ");
        }
        if (sb.length() > 1)
            sb.setLength(sb.length() - 2);
        sb.append('>');
        return sb.toString();
    }
}
