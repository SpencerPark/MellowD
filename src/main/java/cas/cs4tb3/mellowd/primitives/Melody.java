//Melody
//======

package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.intermediate.functions.operations.OctaveShiftable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//A melody is a sequence of pitches played one after the other. Melodies are
//described in Mellow D source files as pitch descriptions between `[` and `]`.
//Each of these notes can be articulated with an [Articulation](../Articulation.html)
//tweaking the performance. Melodies can also contain chords as the collection of notes
//in the chord is played simultaneously it can be compiled on a single beat. Articulated
//chords are the equivalent of articulating each pitch in the chord with the chords
//articulation as this is typically the way that chords are played. In the future glissando
//may function more like a trill but never the less, this class will function the same.
//
//The Melody class is simply a list wrapper. The order of the sounds is important.
public class Melody implements ConcatableComponent.TypeMelody, Indexable<Articulatable>, OctaveShiftable<Melody> {
    //This is the data that supports the melody.
    private final List<Articulated> sounds;

    public Melody() {
        this.sounds = new ArrayList<>();
    }

    public Melody(Articulated... comps) {
        this.sounds = new ArrayList<>();
        Collections.addAll(this.sounds, comps);
    }

    public Melody(List<Articulated> sounds) {
        this.sounds = sounds;
    }

    //`add` is overloaded to support single or multiple concatenation. When
    //concatenated with another melody the ordering of the additions remains
    //the same, just appended to the end of the melody.
    public void add(Melody melody) {
        this.sounds.addAll(melody.sounds);
    }

    public void add(Articulated sound) {
        this.sounds.add(sound);
    }

    @Override
    public Melody shiftOctave(int octaveShift) {
        List<Articulated> sounds = new ArrayList<>(this.sounds.size());
        for (Articulated s : this.sounds) {
            sounds.add(s.shiftOctave(octaveShift));
        }
        return new Melody(sounds);
    }

    public int size() {
        return this.sounds.size();
    }

    public Articulated getElementAtIndex(int index) {
        return this.sounds.get(Indexable.calcIndex(index, size()));
    }

    @Override
    public Articulatable getAtIndex(int index) {
        if (this.sounds.isEmpty())
            return null;

        return this.sounds.get(Indexable.calcIndex(index, size())).getElement();
    }

    @Override
    public void appendTo(Melody root) {
        root.add(this);
    }
}
