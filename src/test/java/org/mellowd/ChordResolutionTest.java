//Chord Resolution Test
//=====================

package org.mellowd;

import org.mellowd.primitives.Chord;
import org.mellowd.primitives.Pitch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ChordResolutionTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> loadTests() throws IOException {
        return Arrays.asList(new Object[][]{
                {"Cmaj", Chord.major(Pitch.C)}, {"Cmin", Chord.minor(Pitch.C)},
                {"Caug", Chord.augmented(Pitch.C)}, {"Cdim", Chord.diminished(Pitch.C)},
                {"Cmaj7", Chord.majorSeventh(Pitch.C)}, {"Cmin7", Chord.minorSeventh(Pitch.C)},
                {"Caug7", Chord.augmentedSeventh(Pitch.C)}, {"Cdim7", Chord.diminished7th(Pitch.C)},
                {"Cmaj7b5", Chord.majorSeventhFlatFive(Pitch.C)}, {"Cminmaj7", Chord.minorMajorSeventh(Pitch.C)},
                {"Cdom7", Chord.dominantSeventh(Pitch.C)}, {"Cmaj7s5", Chord.majorSeventhSharpFive(Pitch.C)},
                {"A#", Chord.major(Pitch.A.sharp())}, {"B$", Chord.major(Pitch.B.flat())},
                {"D+1", Chord.major(Pitch.D.shiftOctaveUp(1))}, {"E-1", Chord.major(Pitch.E.shiftOctaveDown(1))}
        });
    }

    @Parameterized.Parameter
    public String chordDesc;

    @Parameterized.Parameter(1)
    public Chord chord;

    @Test
    public void resolve() throws Exception {
        Chord chord = Chord.resolve(chordDesc);
        assertEquals("Expected "+this.chord+" but resolved "+chordDesc+" to "+chord, this.chord, chord);
    }
}