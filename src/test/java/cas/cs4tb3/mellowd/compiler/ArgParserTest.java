package cas.cs4tb3.mellowd.compiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ArgParserTest {

    private static void testThrowsException(String failMessage, String... args) {
        try {
            ArgParser.parse(args);
        } catch (ArgParser.Help e) {
            return;
        }

        fail(failMessage);
    }

    @Test
    public void timeSigFlag() throws Exception {
        String[] args = new String[] {
                "--timesig", "3", "8"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect time signature numerator set in the options", 3, options.getTimeSignatureTop());
        assertEquals("Incorrect time signature denominator set in the options", 8, options.getTimeSignatureBottom());
    }

    @Test
    public void timeSigShortFlag() throws Exception {
        String[] args = new String[] {
                "-ts", "14", "16"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect time signature numerator set in the options", 14, options.getTimeSignatureTop());
        assertEquals("Incorrect time signature denominator set in the options", 16, options.getTimeSignatureBottom());
    }

    @Test
    public void timeSigFlagValuesMissing() throws Exception {
        testThrowsException("No exception thrown when time sig flag given but values missing",
                "--timesig"
        );
        testThrowsException("No exception thrown when time sig flag given but denominator missing",
                "--timesig", "3"
        );
    }

    @Test
    public void timeSigFlagValueNotInteger() throws Exception {
        testThrowsException("No exception thrown when time sig denominator is NaN",
                "--timesig", "4", "a"
        );
        testThrowsException("No exception thrown when time sig numerator is a decimal",
                "--timesig", "4.4", "5"
        );
    }

    @Test
    public void tempoFlag() throws Exception {
        String[] args = new String[] {
                "--tempo", "130"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect tempo set in the options", 130, options.getTempo());
    }

    @Test
    public void tempoShortFlag() throws Exception {
        String[] args = new String[] {
                "-t", "160"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect tempo set in options", 160, options.getTempo());
    }

    @Test
    public void tempoFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when tempo flag given but no tempo given",
                "--tempo"
        );
    }


    @Test
    public void tempoFlagNegative() throws Exception {
        testThrowsException("No exception thrown when tempo flag is negative",
                "--tempo", "-4"
        );
    }

    @Test
    public void tempoFlagDecimal() throws Exception {
        testThrowsException("No exception thrown when tempo flag is a decimal",
                "--tempo", "4.1"
        );
    }

    @Test
    public void outDirFlag() throws Exception {
        String[] args = new String[] {
                "--outdir", "build"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect output directory set in the options", "build", options.getOutputDirectory());
    }

    @Test
    public void outDirShortFlag() throws Exception {
        String[] args = new String[] {
                "-o", "build"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Incorrect output directory set in the options", "build", options.getOutputDirectory());
    }

    @Test
    public void outDirFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when output directory flag given but no output directory",
                "--outdir"
        );
    }

    @Test
    public void sourceFlag() throws Exception {
        String[] args = new String[] {
                "--src", "src1"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Source directory not added to the options", "src1", options.getSourceDirs().get(0));
    }

    @Test
    public void sourceShortFlag() throws Exception {
        String[] args = new String[] {
                "-s", "src1"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Source directory not added to the options", "src1", options.getSourceDirs().get(0));
    }

    @Test
    public void sourceFlagMultiple() throws Exception {
        String[] args = new String[] {
                "--src", "src1", "--src", "src2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Source directory not properly added to the options", "src1", options.getSourceDirs().get(0));
        assertEquals("Second source directory not properly added to ", "src2", options.getSourceDirs().get(1));
    }

    @Test
    public void sourceFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when source flag given but value is missing",
                "--src"
        );
    }

    @Test
    public void soundFontFlag() throws Exception {
        String[] args = new String[] {
                "--soundfont", "font.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Sound font not added to the options", "font.sf2", options.getSoundFonts().get(0));
    }

    @Test
    public void soundFontShortFlag() throws Exception {
        String[] args = new String[] {
                "-sf", "font.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Sound font not added to the options", "font.sf2", options.getSoundFonts().get(0));
    }

    @Test
    public void soundFontFlagMultiple() throws Exception {
        String[] args = new String[] {
                "--soundfont", "font.sf2", "--soundfont", "font1.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Sound font not properly added to the options", "font.sf2", options.getSoundFonts().get(0));
        assertEquals("Second sound font not properly added to ", "font1.sf2", options.getSoundFonts().get(1));
    }

    @Test
    public void soundFontFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when sound font flag given but value is missing",
                "--soundfont"
        );
    }

    @Test
    public void playFlag() throws Exception {
        String[] args = new String[] {
                "--play"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("Play flag not set when --play is given", options.shouldPlayLive());
        assertFalse("MIDI output flag is set when --play is given", options.shouldOutputMIDI());
        assertFalse("WAVE output flag is set when --play is given", options.shouldOutputWAV());
    }

    @Test
    public void playShortFlag() throws Exception {
        String[] args = new String[] {
                "-p"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("Play flag not set when -p is given", options.shouldPlayLive());
        assertFalse("MIDI output flag is set when -p is given", options.shouldOutputMIDI());
        assertFalse("WAVE output flag is set when -p is given", options.shouldOutputWAV());
    }

    @Test
    public void outputMIDIFlag() throws Exception {
        String[] args = new String[] {
                "--midi"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("MIDI output flag not set when --midi is given", options.shouldOutputMIDI());
        assertFalse("Play flag is set when --midi is given", options.shouldPlayLive());
        assertFalse("WAVE output flag is set when --midi is given", options.shouldOutputWAV());
    }

    @Test
    public void outputMIDIShortFlag() throws Exception {
        String[] args = new String[] {
                "-mid"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("MIDI output flag not set when -mid is given", options.shouldOutputMIDI());
        assertFalse("Play flag is set when -mid is given", options.shouldPlayLive());
        assertFalse("WAVE output flag is set when -mid is given", options.shouldOutputWAV());
    }

    @Test
    public void outputWAVEFlag() throws Exception {
        String[] args = new String[] {
                "--wave"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("WAVE output flag not set when --wave is given", options.shouldOutputWAV());
        assertFalse("Play flag is set when --wave is given", options.shouldPlayLive());
        assertFalse("MIDI output flag is set when --wave is given", options.shouldOutputMIDI());
    }

    @Test
    public void outputWAVEShortFlag() throws Exception {
        String[] args = new String[] {
                "-wav"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("WAVE output flag not set when -wav is given", options.shouldOutputWAV());
        assertFalse("Play flag is set when -wav is given", options.shouldPlayLive());
        assertFalse("MIDI output flag is set when -wav is given", options.shouldOutputMIDI());
    }

    @Test
    public void multiOutputFlags() throws Exception {
        String[] args = new String[] {
                "-wav", "-p", "--midi"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("WAVE output flag not set when all output flags were given", options.shouldOutputWAV());
        assertTrue("MIDI output flag not set when all output flags were given", options.shouldOutputMIDI());
        assertTrue("Play live output flag not set when all output flags were given", options.shouldPlayLive());
    }

    @Test
    public void silentFlag() throws Exception {
        String[] args = new String[] {
                "--silent"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue("Silent flag not set when --silent is given", options.wantsSilent());
    }

    @Test
    public void defaults() throws Exception {
        String[] args = new String[] { };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("Time signature is not 4/4 by default", 4, options.getTimeSignatureTop());
        assertEquals("Time signature is not 4/4 by default", 4, options.getTimeSignatureBottom());

        assertEquals("Tempo is not 120 by default", 120, options.getTempo());

        assertEquals("Output directory is not the working directory by default", "", options.getOutputDirectory());

        assertTrue("Source directories are not empty by default", options.getSourceDirs().isEmpty());

        assertTrue("Sound fonts are not empty by default", options.getSoundFonts().isEmpty());

        assertFalse("Play flag not disabled by default", options.shouldPlayLive());
        assertTrue("MIDI output flag not enabled by default", options.shouldOutputMIDI());
        assertFalse("WAVE output flag not disabled by default", options.shouldOutputWAV());

        assertFalse("Silent not disabled by default", options.wantsSilent());
    }
}