//Melody
//======

package org.mellowd.primitives;

import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.intermediate.functions.operations.OctaveShiftable;

import java.util.*;

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
public class Melody implements Indexable<Articulated, Melody>, OctaveShiftable<Melody> {

    public static int compare(Melody left, Melody right) {
        for (int i = 0; i < left.size(); i++) {
            if (right.size() <= i) //Equal up to here but left is longer
                return 1; //Treat right[i] as null

            Articulated leftAtI = left.sounds.get(i);
            Articulated rightAtI = right.sounds.get(i);
            boolean leftIsPitch = leftAtI instanceof ArticulatedPitch;
            boolean rightIsPitch = rightAtI instanceof ArticulatedPitch;

            int cmp;
            if (leftIsPitch && rightIsPitch)
                cmp = Pitch.compare((Pitch) leftAtI.getElement(), (Pitch) rightAtI.getElement());
            else if (!leftIsPitch && !rightIsPitch)
                cmp = Chord.compare((Chord) leftAtI.getElement(), (Chord) rightAtI.getElement());
            else
                cmp = leftIsPitch ? -1 : 1;

            if (cmp != 0)
                return cmp;
        }

        //Equal up to the end of left
        if (right.size() > left.size())
            return -1; //Right has an extra element

        //Totally equal
        return 0;
    }

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

    //`append` is overloaded to support single or multiple concatenation. When
    //concatenated with another melody the ordering of the additions remains
    //the same, just appended to the end of the melody.
    public void append(Melody melody) {
        this.sounds.addAll(melody.sounds);
    }

    public void append(Articulated sound) {
        this.sounds.add(sound);
    }

    public void append(Pitch pitch) {
        this.sounds.add(new ArticulatedPitch(pitch));
    }

    public void append(Chord chord) {
        this.sounds.add(new ArticulatedChord(chord));
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
    public Articulated getAtIndex(int index) {
        if (this.sounds.isEmpty())
            return null;

        return this.sounds.get(Indexable.calcIndex(index, size()));
    }

    @Override
    public Melody getAtRange(int lower, int upper) {
        List<Articulated> newElements = new ArrayList<>(Indexable.sizeOfRange(lower, upper));

        int size = size();
        Indexable.forEachInRange(lower, upper, i -> newElements.add(sounds.get(Indexable.calcIndex(i, size))));

        return new Melody(newElements);
    }

    @Override
    public String toString() {
        StringJoiner str = new StringJoiner(", ", "[", "]");
        this.sounds.forEach(sound -> str.add(sound.toString()));
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Melody melody = (Melody) o;
        return Objects.equals(sounds, melody.sounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sounds);
    }
}
