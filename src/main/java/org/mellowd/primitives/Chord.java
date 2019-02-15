//Chord
//=====

package org.mellowd.primitives;

import org.mellowd.intermediate.functions.operations.Articulatable;
import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.intermediate.functions.operations.OctaveShiftable;
import org.mellowd.intermediate.functions.operations.Transposable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//A chord is a collection of pitches played simultaneously. There are various chords
//that are described by a sequence of intervals. In a Mellow D source file chords
//are defined between `(` and `)` tokens. The `,` separated pitches make up the chord.
//
//This class contains a variety of standard chords frequently used in compositions.
public class Chord implements Transposable<Chord>, Articulatable, Indexable<Pitch, Chord>, OctaveShiftable<Chord> {

    public static int compare(Chord left, Chord right) {
        for (int i = 0; i < left.size(); i++) {
            if (right.size() <= i) //Equal up to here but left is longer
                return 1; //Treat right[i] as a rest

            int cmp = Pitch.compare(left.pitches[i], right.pitches[i]);
            if (cmp != 0)
                return cmp;
        }

        //Equal up to the end of left
        if (right.size() > left.size())
            return -1; //Right has an extra non rest pitch

        //Totally equal
        return 0;
    }

    //The `CHORD_NAME_PATTERN` is a regular expression for matching chord names. There are a common
    //collection of chord patterns that are frequently used and the compiler should treat them as
    //all defined as variables. This is of course not possible so they are virtually defined and if
    //a chord identifier is expected the compiler will try to match it to this pattern before throwing
    //an undefined reference exception.

    //The pattern is `[A-G] ( '#' | '$' )? ( '+' | '-' [0-9]+ )? ( [a-z0-9]+ )?`
    //with named capturing groups to pull the data from the matcher.
    private static final Pattern CHORD_NAME_PATTERN = Pattern.compile(
            "^(?<note>[A-G])((?<sharp>#)|(?<flat>\\$))?(?<name>[a-z0-9]+)?(?<octaveShift>((?<shiftUp>\\+)|(?<shiftDown>-))(?<shiftAmt>[0-9]+))?$"
    );

    //At its core, a chord is simply a collection of pitches. These are those pitches.
    private Pitch[] pitches;

    //The varargs constructor is deigned for use by the various chord building methods.
    public Chord(Pitch... pitches) {
        this.pitches = pitches;
    }

    public Pitch getRoot() {
        return this.getAtIndex(0);
    }

    //This method provides access to the internal array of pitches. It is used for accessing
    //specific notes via the mellow d syntax `chordName:index`.
    @Override
    public Pitch getAtIndex(int index) {
        int shift = Indexable.calcIndexOverflow(index, size());
        return pitches[Indexable.calcIndex(index, size())].shiftOctave(shift);
    }

    @Override
    public Chord getAtRange(int lower, int upper) {
        Pitch[] newChord = new Pitch[Indexable.sizeOfRange(lower, upper)];

        int[] index = { 0 };
        Indexable.forEachInRange(lower, upper, i -> newChord[index[0]++] = getAtIndex(i));

        return new Chord(newChord);
    }

    //`getPitches` leaks a copy of the pitches that make up this chord.
    public List<Pitch> getPitches() {
        List<Pitch> pitches = new ArrayList<>(this.pitches.length);
        Collections.addAll(pitches, this.pitches);
        return pitches;
    }

    //It is important to know the size of the chord to stop an array out of bounds exception
    //when calling `getPitchAt`. An index greater than 0 and less than `size` will return a pitch
    //without ever throwing an array out of bounds exception.
    public int size() {
        return pitches.length;
    }

    @Override
    public Articulated articulate(Articulation articulation) {
        return new ArticulatedChord(this, articulation);
    }

    @Override
    public Chord transpose(int numSemiTones) {
        Pitch[] pitches = new Pitch[this.pitches.length];

        for (int i = 0; i < pitches.length; i++)
            pitches[i] = this.pitches[i].transpose(numSemiTones);

        return new Chord(pitches);
    }

    @Override
    public Chord shiftOctave(int octaveShift) {
        Pitch[] pitches = new Pitch[this.pitches.length];
        for (int i = 0; i < this.pitches.length; i++) {
            pitches[i] = this.pitches[i].shiftOctave(octaveShift);
        }
        return new Chord(pitches);
    }

    public void append(Pitch p) {
        this.pitches = Arrays.copyOf(this.pitches, this.pitches.length + 1);
        this.pitches[this.pitches.length - 1] = p;
    }

    public void append(Chord chord) {
        int i = size();
        pitches = Arrays.copyOf(pitches, size() + chord.size());
        for (Pitch p : chord.pitches) {
            pitches[i++] = p;
        }
    }

    public void append(Articulated articulated) {
        Articulatable element = articulated.getElement();
        if (element instanceof Pitch)
            this.append((Pitch) element);
        else if (element instanceof Chord)
            this.append((Chord) element);
        else
            throw new IllegalArgumentException("Articualted should be sealed wrt elements.");
    }

    //The `resolve` method is the method that provides the lookup by name support described above with
    //the `CHORD_NAME_PATTERN`.
    public static Chord resolve(String identifier) {
        //If the identifier doesn't match the pattern we can just get out of the method
        //immediately returning null to specify that the identifier cannot be resolved.
        Matcher m = CHORD_NAME_PATTERN.matcher(identifier);
        if (!m.matches()) {
            return null;
        }

        //First resolve the pitch. This is the root of the chord.
        Pitch pitch = Pitch.C;
        switch (m.group("note")) {
            case "A": pitch = Pitch.A;
                break;
            case "B": pitch = Pitch.B;
                break;
            case "C": pitch = Pitch.C;
                break;
            case "D": pitch = Pitch.D;
                break;
            case "E": pitch = Pitch.E;
                break;
            case "F": pitch = Pitch.F;
                break;
            case "G": pitch = Pitch.G;
                break;
        }
        if (m.group("sharp") != null)
            pitch = pitch.sharp();
        if (m.group("flat") != null)
            pitch = pitch.flat();
        if (m.group("octaveShift") != null) {
            if (m.group("shiftUp") != null) {
                pitch = pitch.shiftOctaveUp(Integer.parseInt(m.group("shiftAmt")));
            } else {
                pitch = pitch.shiftOctaveDown(Integer.parseInt(m.group("shiftAmt")));
            }
        }

        //Then resolve the chord name and invoke the appropriate builder
        if (m.group("name") == null)
            //By default a chord is a major chord, so if it is missing a name then it is assumed to
            //mean `maj`.
            return major(pitch);

        switch (m.group("name")) {
            case "maj":     return major(pitch);
            case "m":
            case "min":     return minor(pitch);
            case "aug":     return augmented(pitch);
            case "dim":     return diminished(pitch);
            case "dim7":    return diminished7th(pitch);
            case "maj7b5":  return majorSeventhFlatFive(pitch);
            case "min7":    return minorSeventh(pitch);
            case "minmaj7": return minorMajorSeventh(pitch);
            case "7":
            case "dom7":    return dominantSeventh(pitch);
            case "maj7":    return majorSeventh(pitch);
            case "aug7":    return augmentedSeventh(pitch);
            case "maj7s5":  return majorSeventhSharpFive(pitch);
            case "6":
            case "maj6":    return majorSixth(pitch);
            case "min6":    return minorSixth(pitch);
            default:        return null;
        }
    }

    public boolean contains(Pitch pitch) {
        return Arrays.stream(this.pitches).anyMatch(pitch::equals);
    }

    public boolean containsIgnoreOctave(Pitch pitch) {
        return Arrays.stream(this.pitches).anyMatch(pitch::equalsIgnoreOctave);
    }

    //For use in various collections we should override the `equals` and `hashcode` methods
    //with the standard implementation pattern.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chord chord = (Chord) o;

        return Arrays.equals(pitches, chord.pitches);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pitches);
    }

    //The `toString` implementation wraps the list of pitches in parentheses just
    //as it would appear in a source file.
    @Override
    public String toString() {
        StringJoiner str = new StringJoiner(", ", "(", ")");
        for (Pitch pitch : this.pitches)
            str.add(pitch.toString());
        return str.toString();
    }

    //Triads
    //------
    //
    //Three note chords. These consists of the root, a third and a fifth.
    //The third and fifth intervals are shifted slightly to vary the chord's sound.

    //**Major**: Cmaj or C (root, major 3rd, perfect 5th)
    public static Chord major(Pitch root) {
        return new Chord(root, root.majorThird(), root.perfectFifth());
    }

    //**Minor**: Cmin (root, minor 3rd, perfect 5th)
    public static Chord minor(Pitch root) {
        return new Chord(root, root.minorThird(), root.perfectFifth());
    }

    //**Augmented**: Caug (root, major 3rd, augmented 5th)
    public static Chord augmented(Pitch root) {
        return new Chord(root, root.majorThird(), root.augmentedFifth());
    }

    //**Diminished**: Cdim (root, minor 3rd, diminished 5th)
    public static Chord diminished(Pitch root) {
        return new Chord(root, root.minorThird(), root.diminishedFifth());
    }

    //Sixths
    //------
    //
    //Triads with an added fourth note that is a sixth interval
    //above the root.

    //**Major Sixth**: Cmaj6 (root, major 3rd, perfect 5th, major 6th)
    public static Chord majorSixth(Pitch root) {
        return new Chord(root, root.majorThird(), root.perfectFifth(), root.majorSixth());
    }

    //**Minor Sixth**: Cmin6 (root, minor 3rd, perfect 5th, major 6th)
    public static Chord minorSixth(Pitch root) {
        return new Chord(root, root.minorThird(), root.perfectFifth(), root.majorSixth());
    }

    //Sevenths
    //--------
    //
    //Triads with an added fourth note that is a seventh interval
    //above the root.

    //**Diminished Seventh**: Cdim7 (root, minor 3rd, diminished 5th, diminished 7th)
    public static Chord diminished7th(Pitch root) {
        return new Chord(root, root.minorThird(), root.diminishedFifth(), root.diminishedSeventh());
    }

    //**Major Seventh Flat Five**: Cmaj7b5 (root, minor 3rd, diminished 5th, minor 7th)
    public static Chord majorSeventhFlatFive(Pitch root) {
        return new Chord(root, root.minorThird(), root.diminishedFifth(), root.minorSeventh());
    }

    //**Minor Seventh**: Cmin7 (root, minor 3rd, perfect 5th, minor 7th)
    public static Chord minorSeventh(Pitch root) {
        return new Chord(root, root.minorThird(), root.perfectFifth(), root.minorSeventh());
    }

    //**Minor Major Seventh**: Cminmaj7 (root, minor 3rd, perfect 5th, major 7th)
    public static Chord minorMajorSeventh(Pitch root) {
        return new Chord(root, root.minorThird(), root.perfectFifth(), root.majorSeventh());
    }

    //**Dominant Seventh**: Cdom7 (root, major 3rd, perfect 5th, minor 7th)
    public static Chord dominantSeventh(Pitch root) {
        return new Chord(root, root.majorThird(), root.perfectFifth(), root.minorSeventh());
    }

    //**Major Seventh**: Cmaj7 (root, major 3rd, perfect 5th, major 7th)
    public static Chord majorSeventh(Pitch root) {
        return new Chord(root, root.majorThird(), root.perfectFifth(), root.majorSeventh());
    }

    //**Augmented Seventh**: Caug7 (root, major 3rd, augmented 5th, minor 7th)
    public static Chord augmentedSeventh(Pitch root) {
        return new Chord(root, root.majorThird(), root.augmentedFifth(), root.minorSeventh());
    }

    //**Augmented Major Seventh**: Cmaj7s5 (root, major 3rd, augmented 5th, major 7th)
    public static Chord majorSeventhSharpFive(Pitch root) {
        return new Chord(root, root.majorThird(), root.augmentedFifth(), root.majorSeventh());
    }
}
