//Pitch
//=====

package org.mellowd.primitives;

import org.mellowd.intermediate.functions.operations.Articulatable;
import org.mellowd.intermediate.functions.operations.OctaveShiftable;
import org.mellowd.intermediate.functions.operations.Transposable;

import java.util.Objects;

//Pitch is a key building block in the Mellow D compiler. It specifies the value of the played
//note. MIDI restricts the note value to 7 bits, hence we have 0-127 to work with. 0 corresponds
//to the musical note c with each consecutive number adding a semi-tone to the note value.
public final class Pitch implements Transposable<Pitch>, OctaveShiftable<Pitch>, Articulatable {
    //The `NOTE_NAMES` array maps a midi num in the lowest octave to a note name. Here
    //it is clear to see the mapping from number to note for a total of 12 semi-tones in
    //an octave.
    private static final String[] NOTE_NAMES = new String[]{
            "c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"
    };

    //A pitch is just a MIDI number (a int) wrapped with a bunch of utility methods. We can
    //represent every possible MIDI pitch with 128 instances. Most songs would easily hit this number of
    //pitch instantiations so we can create them all at once and share them.
    private static final Pitch[] ALL_PITCHES = new Pitch[128];

    static {
        for (int i = 0; i < 128; i++) ALL_PITCHES[i] = new Pitch(i);
    }

    public static final int DEFAULT_OCTAVE = 4;

    //To get started a static reference by name to a whole tone pitch is retrieved. From here
    //the caller would chain calls to the various wrapper methods to obtain the desired pitch.
    public static final Pitch C = ALL_PITCHES[0 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch D = ALL_PITCHES[2 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch E = ALL_PITCHES[4 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch F = ALL_PITCHES[5 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch G = ALL_PITCHES[7 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch A = ALL_PITCHES[9 + (DEFAULT_OCTAVE * 12)];
    public static final Pitch B = ALL_PITCHES[11 + (DEFAULT_OCTAVE * 12)];

    //The `REST` is a special instance that will be check for reference equality to insert
    //silence rather than sound. It is described in a Mellow D source file with a `*`.
    public static final Pitch REST = new Pitch(0);

    //This is the equivalent of a constructor. It retrieves the correct pitch instance for the given
    //MIDI note value. If the given value is negative or exceeds 127 it is pulled into the range
    //on the end that it exceeded.
    public static Pitch getPitch(int midiNum) {
        if (midiNum < 0)
            midiNum = 12 + (midiNum % 12); // 12 + -1 = B
        else if (midiNum > 127)
            midiNum = 127 + ((midiNum % 12) - 12); // 120 is highest c
        return ALL_PITCHES[midiNum];
    }

    public static int compare(Pitch left, Pitch right) {
        if (left == REST)
            return right == REST ? 0 : -1;
        else if (right == REST)
            return 1;

        return left.getMidiNum() - right.getMidiNum();
    }

    private final int midiNum;

    private Pitch(int midiNum) {
        this.midiNum = midiNum;
    }

    @Override
    public Articulated articulate(Articulation articulation) {
        return new ArticulatedPitch(this, articulation);
    }

    //Sharp, or `#` in a Mellow D source file, increases the note value by 1 semi-tone.
    public Pitch sharp() {
        if (this == REST) return REST;
        return getPitch(midiNum + 1);
    }

    //Flat, or `$` in a Mellow D source file, decreases the note value by 1 semi-tone.
    public Pitch flat() {
        if (this == REST) return REST;
        return getPitch(midiNum - 1);
    }

    //Each octave shift is described in a Mellow D source as `+` or `-` a `[0-9]+`.
    //As each octave contains 12 semi-tones, adding 12 to the MIDI note value increase the octave
    //by 1.

    //The shift up and shift down methods shift the octave in the appropriate direction ignoring
    //the sign of the `amt`.
    public Pitch shiftOctaveUp(int amt) {
        if (this == REST) return REST;
        return getPitch(midiNum + (12 * Math.abs(amt)));
    }

    public Pitch shiftOctaveDown(int amt) {
        if (this == REST) return REST;
        return getPitch(midiNum - (12 * Math.abs(amt)));
    }

    //The plain shift octave uses the sign of the `amt` to determine the direction for the shift.
    @Override
    public Pitch shiftOctave(int amt) {
        if (this == REST) return REST;
        return getPitch(midiNum + (12 * amt));
    }

    //Using java's integer division logic we can divide by 12 (the semi-tones per octave) and
    //round down all at once to get the octave a note is in.
    public int getOctave() {
        return midiNum / 12;
    }

    //In order to get the note value we can take the remainder of the MIDI note number when divided
    //by 12 (`midiNum % 12`). This gives the note number in octave 0. The we can just preform an
    //octave shift up to the desired octave (`octave * 12`).
    public Pitch inOctave(int octave) {
        if (this == REST) return REST;
        return getPitch((octave * 12) + (midiNum % 12));
    }

    public int distanceBetween(Pitch other) {
        if (this == REST || other == REST)
            return 0;

        return Math.abs(other.midiNum - this.midiNum);
    }

    //Intervals
    //---------
    //There are various common intervals that the Mellow D compiler heavily makes use of in
    //constructing [chords](primitives/Chord.html).

    //*majorThird*: 4 semi-tones above the root
    public Pitch majorThird() {
        if (this == REST) return REST;
        return getPitch(midiNum + 4);
    }

    //*minorThird*: 3 semi-tones above the root
    public Pitch minorThird() {
        if (this == REST) return REST;
        return getPitch(midiNum + 3);
    }

    //*perfectFifth*: 7 semi-tones above the root
    public Pitch perfectFifth() {
        if (this == REST) return REST;
        return getPitch(midiNum + 7);
    }

    //*augmentedFifth*: 8 semi-tones above the root
    public Pitch augmentedFifth() {
        if (this == REST) return REST;
        return getPitch(midiNum + 8);
    }

    //*diminishedFifth*: 6 semi-tones above the root
    public Pitch diminishedFifth() {
        if (this == REST) return REST;
        return getPitch(midiNum + 6);
    }

    //*diminishedFifth*: 9 semi-tones above the root
    public Pitch diminishedSeventh() {
        if (this == REST) return REST;
        return getPitch(midiNum + 9);
    }

    //*majorSixth*: 9 semi-tones above the root
    public Pitch majorSixth() {
        if (this == REST) return REST;
        return getPitch(midiNum + 9);
    }

    //*minorSeventh*: 10 semi-tones above the root
    public Pitch minorSeventh() {
        if (this == REST) return REST;
        return getPitch(midiNum + 10);
    }

    //*majorSeventh*: 11 semi-tones above the root
    public Pitch majorSeventh() {
        if (this == REST) return REST;
        return getPitch(midiNum + 11);
    }

    //`getMidiNum` resolves the pitch to its MIDI note number
    public int getMidiNum() {
        return midiNum;
    }

    //Pitch's `toString` implementation concatenates the MIDI note value with ':', the
    //name of the note and the octave for a nice pitch description.
    @Override
    public String toString() {
        if (this == REST) return "REST";
        return midiNum + ":" + NOTE_NAMES[midiNum % 12] + Integer.toString(midiNum / 12);
    }

    @Override
    public Pitch transpose(int numSemiTones) {
        if (this == REST) return REST;
        return Pitch.getPitch(this.midiNum + numSemiTones);
    }

    public boolean equalsIgnoreOctave(Pitch other) {
        return this == other
                || (REST != this && REST != other && this.midiNum % 12 == other.midiNum % 12);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pitch pitch = (Pitch) o;
        return midiNum == pitch.midiNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(midiNum);
    }
}
