//Melody
//======

package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.ArticulatedSound;

import java.util.ArrayList;
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
public class Melody {
    //This is the data that supports the melody.
    private List<ArticulatedSound> notes;

    public Melody(List<ArticulatedSound> notes) {
        this.notes = notes;
    }

    public List<ArticulatedSound> getNotes() {
        return notes;
    }

    //`add` is overloaded to support single or multiple concatenation. When
    //concatenated with another melody the ordering of the additions remains
    //the same, just appended to the end of the melody.
    public void add(Melody melody) {
        this.notes.addAll(melody.notes);
    }

    public void add(ArticulatedSound note) {
        this.notes.add(note);
    }

    //Melodies defined as variables need to be reevaluated in the current octave
    //for them to have any use.
    public Melody inOctave(int octave) {
        List<ArticulatedSound> sounds = new ArrayList<>(notes.size());
        for (ArticulatedSound sound : this.notes) {
            sounds.add(new ArticulatedSound(sound.getSound().inOctave(octave), sound.getArticulation()));
        }
        return new Melody(sounds);
    }
}
