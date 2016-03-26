//Rhythm
//======

package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.ArticulatedSound;
import cas.cs4tb3.mellowd.Beat;
import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.PlayableSound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//The `Rhythm` is responsible for the timing considerations in playable sounds. It specifies
//the duration in the form of a [Beat](../Beat.html). In a Mellow D source file a rhythm is
//a comma separated list of beats wrapped in `<` and `>`. Each beat is referred to by the first
//letter in it's name. Additionally beats can be slurred together. This results in the durations
//overlapping and the notes connecting more smoothly.
public class Rhythm {
    //These two lists are maintained by the rhythm class making sure that both remain the same
    //length. Each beat has a slurred value associated with it, true marking the beat as slurred
    //and false being the default, unslurred, state.
    private List<Beat> beats;
    private List<Boolean> slurred;

    //Creating a rhythm is done by specifying zero or more beats that make up the rhythm.
    public Rhythm(Beat... beats) {
        //A copy of the input data is stored in the `beats` list.
        this.beats = new ArrayList<>();
        Collections.addAll(this.beats, beats);
        //The `slurred` list is filled in with the default value keeping it the same size as the
        //`beats` list.
        this.slurred = new ArrayList<>();
        for (int i = 0; i < this.beats.size(); i++) {
            slurred.add(false);
        }
    }

    //This utility method calculates the total duration of this rhythm if performed in the given
    //[timing environment](../TimingEnvironment.html). The returned duration is in ticks.
    public long getTotalTickDuration(TimingEnvironment environment) {
        //To calculate this duration simply sum up the result of calculating
        //the ticks in each beat in this rhythm.
        long duration = 0;
        for (Beat beat : beats) {
            duration += environment.ticksInBeat(beat);
        }
        return duration;
    }

    //Rhythm also defines some methods for manipulating and retrieving the data
    //stored in this class.

    //`numBeats` returns the number of beats in this Rhythm which is just the size
    //of the `beats` list.
    public int numBeats() {
        return beats.size();
    }

    //`getBeat` returns the beat at a given position in the rhythm. This will throw
    //an index out of bounds exception if the `index` is &lt; 0 or &ge; numBeats().
    public Beat getBeat(int index){
        return beats.get(index);
    }

    //`isSlurred` checks if the beat at the given index is slurred. This returns true
    //if the beat is slurred and false otherwise.
    public boolean isSlurred(int index) {
        return this.slurred.get(index);
    }

    //`slur` sets the slur flag to true for all beats in the index range `[start, end)`.
    //If `end` is larger than the number of beats in the rhythm this method will stop at
    //the largest beat index available.
    public void slur(int start, int end) {
        for (int i = start; i < this.beats.size() && i < end; i++) {
            this.slurred.set(i, true);
        }
    }

    //`append` adds a beat to the rhythm. It is equivalent to calling `append(beat, false)`
    //as the added beat will not be slurred.
    public void append(Beat beat) {
        this.beats.add(beat);
        this.slurred.add(false);
    }

    //`appendSlurred` adds a beat to the rhythm. It is equivalent to calling `append(beat, true)`
    //as the added beat will not be slurred.
    public void appendSlurred(Beat beat) {
        this.beats.add(beat);
        this.slurred.add(true);
    }

    //`append` adds a beat to the rhythm setting the slurred flag to `slur`.
    public void append(Beat beat, boolean slur) {
        this.beats.add(beat);
        this.slurred.add(slur);
    }

    //`append` preforms a concatenation between this rhythm and the given rhythm. The order
    //of the elements remain the same with each beat in the new rhythm being added to the end
    //of this rhythm. All slurred beats will remain slurred.
    public void append(Rhythm rhythm) {
        this.beats.addAll(rhythm.beats);
        this.slurred.addAll(rhythm.slurred);
    }

    //`append` preforms a concatenation between this rhythm and the given rhythm. If the `slurred`
    //flag is set to true then all appended beats will have their slurred flag set to true. If the
    //flag is false then the beats in the given rhythm will retain their slurred state.
    public void append(Rhythm rhythm, boolean slurred) {
        this.beats.addAll(rhythm.beats);
        if (!slurred) {
            this.slurred.addAll(rhythm.slurred);
        } else {
            for (int i = 0; i < rhythm.slurred.size(); i++) {
                this.slurred.add(true);
            }
        }
    }

    //`createPhrase` is the method that preforms the `*` operation to turn a [Melody](Melody.html)
    //and a rhythm into a [Phrase](Phrase.html).
    public Phrase createPhrase(Melody melody) {
        //First we need to take some space to save the created phrase. The length of the phrase is the length
        //of the longer of the melody or rhythm.
        PlayableSound[] sounds = new PlayableSound[Math.max(this.beats.size(), melody.getNotes().size())];
        List<ArticulatedSound> notes = melody.getNotes();

        //Fill in the array by creating a new sound from the sound and beat. Iteration
        //over the melody and rhythm are modulo so that the shorter collection wraps
        //around to the front to make the sizes of the collections always match up.
        for (int i = 0; i < sounds.length; i++) {
            sounds[i] = new PlayableSound(notes.get(i % notes.size()),
                    this.beats.get(i % this.beats.size()),
                    this.slurred.get(i % this.slurred.size()));
        }

        //Create and return a phrase backed by the sounds just created.
        return new Phrase(sounds);
    }
}
