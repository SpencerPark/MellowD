//Error Response Test
//===================

package cas.cs4tb3.mellowd;

import cas.cs4tb3.mellowd.midi.GeneralMidiConstants;
import cas.cs4tb3.mellowd.parser.*;
import cas.cs4tb3.mellowd.parser.MellowDLexer;
import cas.cs4tb3.mellowd.parser.MellowDParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

//This class runs various tests with various erroneous inputs and verifying that
//the compiler catches the issue and responds appropriately.
@RunWith(JUnit4.class)
public class ErrorResponseTest {

    //The tests will require the instantiation of quite a few parsers so we will define
    //some helper methods for creating parsers from a file input or a string.
    private static MellowDParser parserFor(File input) throws IOException {
        ANTLRFileStream inStream = new ANTLRFileStream(input.getAbsolutePath());
        MellowDLexer lexer = new MellowDLexer(inStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new MellowDParser(tokenStream, new TimingEnvironment((byte) 4, (byte) 4, 120),
                new TrackManager(GeneralMidiConstants.REGULAR_CHANNELS, GeneralMidiConstants.DRUM_CHANNELS));
    }

    private static MellowDParser parserFor(String input) {
        ANTLRInputStream inStream = new ANTLRInputStream(input);
        MellowDLexer lexer = new MellowDLexer(inStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new MellowDParser(tokenStream, new TimingEnvironment((byte) 4, (byte) 4, 120),
                new TrackManager(GeneralMidiConstants.REGULAR_CHANNELS, GeneralMidiConstants.DRUM_CHANNELS));
    }

    //Verification of parse exceptions is a common task as well. For a parse exception to be expected
    //it must match the expected error text and position in the input.
    private static void assertExpectedError(String expectedText, int expectedStart, ParseException ex) {
        String failMsg = String.format("Expected error \"%s\":%d but was \"%s\":%d.",
                expectedText, expectedStart,
                ex.getText(), ex.getStart());
        assertTrue(failMsg,
                expectedStart == ex.getStart() && expectedText.equals(ex.getText()));
        System.out.printf("Successfully thrown exception: %s\n", ex.getMessage());
    }

    //Make sure the parser throws an error if a crescendo token
    //is not specified which would result in some sounds being skipped and
    //remaining in the buffer.
    @Test
    public void noTargetCrescendo() throws Exception {
        MellowDParser parser = parserFor("" +
                "block{" +
                "    pp << [a, b, c]*<q> " +
                "}"
        );
        try {
            parser.song();
        } catch (ParseException e) {
            assertExpectedError("<<", 13, e);
            return;
        }

        fail("Compiler did not throw an exception despite a crescendo with no target being specified.");
    }

    //Make sure the compiler notices the block's crescendo target is specified
    //even though it appears in the next fragment.
    @Test
    public void crescendoTargetNextFragment() throws Exception {
        MellowDParser parser = parserFor("" +
                "block{" +
                "    pp << [a, b, c]*<q> " +
                "}block{" +
                "    ff" +
                "}");
        try {
            parser.song();
        } catch (ParseException e) {
            fail("Compiler threw an exception even though the crescendo was closed in the following block.");
        }
    }

    @Test
    public void crescendoToLowerDynamic() throws Exception {
        MellowDParser parser = parserFor("" +
                "block{" +
                "    f << [a]*<q> pp" +
                "}");

        try {
            parser.song();
        } catch (ParseException e) {
            assertExpectedError("<<", 12, e);
            return;
        }

        fail("Expected an error due to a crescendo being specified by the volume decreasing but nothing" +
                "was thrown.");
    }

    //Due to the GM specifications we are restricted in the number of channels available. If
    //an input contains too many different blocks such that they can not be properly distributed
    //amongst these channels an exception should be thrown explaining the problem.
    @Test
    public void tooManyBlocks() throws Exception {
        MellowDParser parser = parserFor(new File(Thread.currentThread().getContextClassLoader()
                .getResource("errortest/largeInput.mlod").toURI().getPath()));

        boolean encounteredException = false;
        try {
            parser.song();
        } catch (IllegalStateException e) {
            encounteredException = true;
        }

        if (!encounteredException)
            fail("Expected largeInput.mlod to throw an illegal state exception about too many channels but" +
                    "it didn't.");

        //Test to see if the sharechannel option can be used to fix the overloaded issue
        MellowDParser parserFix = parserFor(new File(Thread.currentThread().getContextClassLoader()
                .getResource("errortest/largeInputShared.mlod").toURI().getPath()));

        try {
            parserFix.song();
        } catch (IllegalStateException e) {
            fail("Block2 sharing a channel with block1 did not fix the MIDI overload.");
        }
    }

    //Test that various incorrect variable declarations and references are caught
    //and handled appropriately.

    //Make sure that attempts to index a melody are caught
    @Test
    public void indexAMelody() throws Exception {
        MellowDParser parser = parserFor("" +
                "myChord -> [a, b, c]" +
                "block{" +
                "    [myChord:0]*<q>" +
                "}");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("myChord", 31, e);
            return;
        }

        fail("Variable myChord was a melody when it should have been a chord to index but the compiler" +
                "silently handled it.");
    }

    //Make sure that attempts to make a phrase from a rhythm identifier starred with
    //a rhythm are caught.
    @Test
    public void rhythmCrossRhythm() throws Exception {
        MellowDParser parser = parserFor("" +
                "myChord -> <q, q, q>" +
                "block{" +
                "    myChord*<q>" +
                "}");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("myChord", 30, e);
            return;
        }

        fail("Variable myChord was a rhythm and the compiler silently starred it with a rhythm.");
    }

    //Ensure attempts to use a melody identifier as a chord param are caught
    @Test
    public void chordMelodyConcatenation() throws Exception {
        MellowDParser parser = parserFor("" +
                "myChord -> [a, b, c]" +
                "block{" +
                "    (myChord, a)*<q>" +
                "}");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("myChord", 31, e);
            return;
        }

        fail("Variable myChord was a melody and the compiler silently accepted it as a chord parameter.");
    }

    //Ensure the correct erroneous identifier is marked
    @Test
    public void secondIdentIncorrect() throws Exception {
        MellowDParser parser = parserFor("" +
                "myMel -> [a, b, c]" +
                "myChord -> (c, e, g)" +
                "block{" +
                "    (myChord, myMel, a)*<q>" +
                "}");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("myMel", 58, e);
            return;
        }

        fail("Variable myMel was not marked as the problematic token.");
    }

    //Ensure that implicit chords can be overridden
    @Test
    public void baseOverrideChord() throws Exception {
        MellowDParser parser = parserFor("" +
                "C -> [a, b, c]" +
                "block{" +
                "    (C, a)*<q>" +
                "}");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("C", 25, e);
            return;
        }

        fail("Compiler used reassigned chord identifier as a chord despite being defined as a melody.");
    }

    //Ensure that percussion mappings can be overwritten
    @Test
    public void baseOverridePercussion() throws Exception {
        MellowDParser parser = parserFor("" +
                "hHat -> <q>" +
                "sample ->* [hHat, tri, lBongo]*<q>");

        try {
            parser.song();
        } catch (IncorrectTypeException e) {
            assertExpectedError("hHat", 23, e);
            return;
        }

        fail("Compiler ignored redefined percussion sound hHat.");

    }
}
