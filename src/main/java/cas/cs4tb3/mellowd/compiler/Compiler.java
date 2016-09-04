//Compiler
//========

package cas.cs4tb3.mellowd.compiler;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.parser.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

//The `Compiler` class is the main entry point for the program.
public class Compiler {

    //Empty sequences will have this event appended to create a playable empty sequence.
    private static final class ImmutableEndOfTrack extends MetaMessage {
        private static final byte EOT_EVENT_CODE = 0x2F;

        private ImmutableEndOfTrack() {
            super(new byte[3]);
            data[0] = (byte) META;
            data[1] = EOT_EVENT_CODE;
            data[2] = 0;
        }

        public void setMessage(int type, byte[] data, int length) throws InvalidMidiDataException {
            throw new InvalidMidiDataException("cannot modify end of track message");
        }
    }

    public static final MidiMessage EOT_MESSAGE = new ImmutableEndOfTrack();
    public static final String FILE_EXTENSION = ".mlod";

    public static void main(String[] args) throws Exception {
        //The ArgumentParser is from the [argparse4j](https://argparse4j.github.io/). It is
        //a library for parsing command line arguments.

        //Create a new parser for the mellowd command. The defaultHelp create a `-h` option
        //that displays a help menu for the command.
        ArgumentParser argParser = ArgumentParsers.newArgumentParser("mellowd")
                .defaultHelp(true)
                .version("1.0.0");
        //The `-p` option is a flag so the action will be storeTrue so that the value
        //of this argument will be false if not present and true if it is present. If
        //`-p` is set the program will immediately playback the compiled song.
        argParser.addArgument("-p", "--play")
                .action(Arguments.storeTrue())
                .help("Playback the specified file instead of exporting it.");
        //The `-o` option specifies the output directory. By default this is just the current
        //directory. The type of the argument is a file so the parser tries to parse a path.
        argParser.addArgument("-o", "--outdir")
                .nargs("?")
                .setDefault(Paths.get("").toAbsolutePath().toFile())
                .type(Arguments.fileType())
                .help("Set the output directory for the compilation.");
        //The `-ts` option specifies the time signature. This is <sup>4</sup>&frasl;<sub>4</sub>
        //by default. To specify a new time signature both the numerator and denominator must
        //be given. Otherwise neither need to be given.
        argParser.addArgument("-ts", "--timesig")
                .nargs(2)
                .type(byte.class)
                .setDefault(4, 4)
                .required(false)
                .metavar("numerator", "denominator")
                .help("Specify the numerator and denominator of the time signature (Ex: 4 4 for 4/4).");
        //The `-t` options specifies the tempo. A standard tempo is 120 bpm so that is the default. To
        //change the tempo the `-t` argument must be followed by a number that represents the tempo in
        //beats per minute.
        argParser.addArgument("-t", "--tempo")
                .nargs("?")
                .metavar("tempo")
                .type(int.class)
                .setDefault(120)
                .help("Specify the tempo of the compiled song in BPM.");
        //There are various flags that can specify what output formats to write the file as.
        MutuallyExclusiveGroup outputFormat = argParser.addMutuallyExclusiveGroup("output format")
                .description("Set the output format of the compiler. This defaults to MIDI (.mid)");
        outputFormat.addArgument("--mid", "--midi")
                .dest("IODelegate")
                .action(Arguments.storeConst())
                .setConst(MIDIIODelegate.getInstance())
                .help("Set the output type of the compiler to a MIDI file (.mid). Default: MIDI (.mid)");
        outputFormat.addArgument("--wav", "--wave")
                .dest("IODelegate")
                .action(Arguments.storeConst())
                .setConst(WavIODelegate.getInstance())
                .help("Set the output type of the compiler to a WAVE file (.wav). Default: MIDI (.mid)");
        //The last argument is a required argument. The file to compile. The given file must exist
        //and be readable.
        argParser.addArgument("file")
                .type(Arguments.fileType()
                        .verifyExists()
                        .verifyCanRead())
                .help("Specify the file to compile.");

        //Now that the parser is constructed we can parse the input arguments.
        Namespace arguments = null;
        try {
            arguments = argParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            //If the parse fails forward the error to the parser
            //for a nicely formatted error message and then exit because
            //we don't have the correct arguments to run the compiler.
            argParser.handleError(e);
            System.exit(1);
        }

        //Now that we have valid arguments we need to pull the information
        //out of the parser.
        File outDir = handleOutDir(arguments.get("outdir"));
        File toCompile = handleInFile(arguments.get("file"));
        List<Number> tempo = arguments.getList("timesig");

        //Now we can begin compiling
        Sequence compilationResult = null;
        try {
            //Compile the input file with the given timing arguments.
            compilationResult = compile(toCompile,
                    tempo.get(0).byteValue(),
                    tempo.get(1).byteValue(),
                    arguments.getInt("tempo"),
                    true);

            //Display the duration of the compiled song in seconds.
            System.out.printf("Song length: %d s\n",
                    TimeUnit.MICROSECONDS.toSeconds(compilationResult.getMicrosecondLength()));
            //If an IOException occurs then we had a problem with the input file. We will
            //display the error and exit.
        } catch (IOException e) {
            System.err.printf("Error reading input file (%s). Reason: %s\n",
                    toCompile.getAbsolutePath(), e.getLocalizedMessage());
            System.exit(1);
            //If a ParseException occurs then there was a problem with the data in the input
            //file. The input is well formed but the semantics are wrong. Display the error and exit.
        } catch (CompilationException e) {
            e.print(System.err);
            System.exit(1);
        } catch (ParseException e) {
            for (SyntaxErrorReport errorReport : e.getProblems()) {
                System.err.println(errorReport.getErrorType().toString()+": "+errorReport.getMessage());
            }
            System.exit(1);
        }

        try {
            //If the play flag is set than we will play the `compilationResult` with the systems
            //midi sequencer.
            if (arguments.getBoolean("play")) {
                //Tell the user the song is playing.
                System.out.printf("Playing %s\n", toCompile.getName().replace(FILE_EXTENSION, ""));

                //Create a music player from the sequencer and song.
                SequencePlayer player = new SequencePlayer(MidiSystem.getSequencer(), compilationResult);

                //Make a blocking call to start playing the song
                player.playSync();

                player.close();

                //When playSync returns this means the song is done. Tell the user that the play is
                //completed and exit.
                System.out.println("Play complete!");
                System.exit(0);
            //If the play flag is not set the we will write the result to a file of the same name
            //located in the `outdir`.
            } else {
                try {
                    //If the compilation result is empty then add the EOT event to
                    //make the output file playable.
                    if (compilationResult.getTickLength() == 0) {
                        compilationResult.getTracks()[0].add(new MidiEvent(EOT_MESSAGE, 1));
                    }

                    //If not specified the MIDI delegate should be used
                    SequenceIODelegate ioDelegate = arguments.get("IODelegate");
                    if (ioDelegate == null)
                        ioDelegate = MIDIIODelegate.getInstance();

                    //The `outFile` is a file in the `outDir` with the same name as the file to compile
                    //with the `.mid` file extension instead.
                    File outFile = new File(outDir, toCompile.getName().replace(FILE_EXTENSION, ioDelegate.getExtension()));
                    if (!outFile.exists()) outFile.createNewFile();

                    long writeStartTime = System.nanoTime();
                    ioDelegate.save(compilationResult, outFile);
                    if (ioDelegate != MIDIIODelegate.getInstance()) {
                        //If we are not writing to a MIDI file then we should inform the user
                        //that the expensive operation is converting to sound to the specified
                        //type.
                        long writeTime = System.nanoTime() - writeStartTime;
                        System.out.printf("Conversion to %s took %.4f s\n",
                                ioDelegate.getExtension(), writeTime / 1E9d);
                    }

                    //Display the compilation input and output locations for the user also
                    //letting them know that the compilation was successful.
                    System.out.printf("%s compiled to %s\n",
                            toCompile.getName().replace(FILE_EXTENSION, ".mlod"),
                            outFile.getPath());
                    //If an IOException occurred let the user know the issue and exit.
                } catch (IOException e) {
                    System.err.printf("Error writing compilation result. Reason: %s.\n", e.getLocalizedMessage());
                    System.exit(1);
                }
            }
            //If a MidiUnavailableException occurs let the user know the error and exit.
        } catch (MidiUnavailableException e) {
            System.err.printf("Midi system not available. %s.\n", e.getLocalizedMessage());
            System.exit(1);
            //If an InvalidMidiDataException occurs it is most likely our fault. This error
            //should be reported so ask the user to report it.
        } catch (InvalidMidiDataException e) {
            System.err.printf("Midi compilation error. Something went wrong, please submit your source to" +
                    " the bug tracker. Error: %s\n", e.getLocalizedMessage());
            System.exit(1);
        }
    }

    //`handleOutDir` tries its best to use the given `outDir` and if it can't
    //it reports the problem to the user and closes the program.
    private static File handleOutDir(File outDir) {
        //If the directory doesn't exits try and create it
        if (!outDir.exists()) {
            try {
                boolean created = outDir.mkdirs();
                //If `mkdirs` returns false the the directory doesn't exist because we are
                //only at this point if it didn't exists in the first place.
                if (!created) {
                    System.err.printf("Could not create outdir %s\n", outDir.getAbsolutePath());
                    System.exit(1);
                }
                //If a SecurityException occurs let the user know that there is nothing we can do
                //to create the directory for them and close the program.
            } catch (SecurityException e) {
                System.err.printf("Could not create outdir (%s). Stopped by the security manager: %s",
                        outDir.getAbsolutePath(), e.getLocalizedMessage());
                System.exit(1);
            }
            //If we make it down here than all went well and the directory was created so let
            //the user know we made a new directory on their machine.
            System.out.printf("Created directory %s\n", outDir.getAbsolutePath());
        //If the path exists but is not a directory then we can't put the compilation result
        //anywhere. Let the user know they gave us a path to an existing file and close the program.
        } else if (!outDir.isDirectory()) {
            System.err.printf("outdir (%s) is not a directory\n", outDir.getAbsolutePath());
            System.exit(1);
        }

        //Return the file.
        return outDir.getAbsoluteFile();
    }

    //`handleInFile` does its best to use the input file given
    private static File handleInFile(File inFile) {
        //If the file doesn't exist then there is nothing to read. Tell the user the
        //problem and exit.
        if (!inFile.exists()) {
            System.err.printf("Cannot find input file %s.\n", inFile.getAbsolutePath());
            System.exit(1);
        }

        //If the file doesn't end with `.mlod` then it isn't a Mellow D source and
        //the user most likely gave the wrong file. Let them know the file they gave
        //and tell them we can't use it.
        if (!inFile.getName().endsWith(FILE_EXTENSION)) {
            System.err.printf("In file (%s) is not a Mellow D source file (.mlod).\n",
                    inFile.getAbsolutePath());
            System.exit(1);
        }

        //Return the input file
        return inFile;
    }

    public static Sequence compile(File src, byte numerator, byte denominator, int tempo, boolean verbose) throws Exception {
        return compile(new ANTLRFileStream(src.getAbsolutePath()),
                new SourceFinder(src.getAbsoluteFile().getParentFile(), FILE_EXTENSION),
                numerator,
                denominator,
                tempo,
                verbose);
    }

    //`compile` is the method that actually runs the compiler.
    public static Sequence compile(CharStream inStream, SourceFinder srcFinder, byte numerator, byte denominator, int tempo, boolean verbose) throws Exception {
        //First we will display the inputs being used so they can double check everything
        //is as expected.
        if (verbose) {
            System.out.printf("Time Signature: %d / %d\n", numerator, denominator);
            System.out.printf("Tempo: %d bpm\n", tempo);
            System.out.printf("Compiling: %s\n", inStream.getSourceName());
        }

        //Now we can build a lexer with the `src` as the input.
        MellowDLexer lexer = new MellowDLexer(inStream);

        //The parser takes the tokens from the lexer as well as the timing environment constructed
        //from the input arguments and a track manager.
        TokenStream tokens = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokens);

        ParseErrorListener errorListener = new ParseErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        TimingEnvironment timingEnvironment = new TimingEnvironment(numerator, denominator, tempo);

        //Parse the input!
        long parseStart = System.nanoTime();
        MellowDParser.SongContext parseTree = parser.song();
        if (errorListener.encounteredError())
            throw new ParseException(errorListener.getErrors());

        if (verbose) {
            long parseTime = System.nanoTime() - parseStart;
            System.out.printf("Parsing took %.4f s\n",
                    parseTime / 1E9d);
        }

        MellowD compilationResult = new MellowD(srcFinder, timingEnvironment);
        MellowDCompiler walker = new MellowDCompiler(compilationResult);

        if (!parseTree.importStatement().isEmpty()) {
            //Compile the dependencies
            long dependencyCompStart = System.nanoTime();
            parseTree.importStatement().forEach(walker::visitImportStatement);
            if (verbose) {
                long dependencyCompTime = System.nanoTime() - dependencyCompStart;
                System.out.printf("Dependency compilation took %.4f s\n",
                        dependencyCompTime / 1E9d);
            }
        }

        //Compile the body
        long compileStart = System.nanoTime();
        walker.visitSong(parseTree);
        if (verbose) {
            long compileTime = System.nanoTime() - compileStart;
            System.out.printf("Compilation took %.4f s\n",
                    compileTime / 1E9d);
        }

        //Execute all of the compiled statements to build the output
        long executionStart = System.nanoTime();
        Sequence result = compilationResult.execute();
        if (verbose) {
            long executionTime = System.nanoTime() - executionStart;
            System.out.printf("Execution took %.4f s\n",
                    executionTime / 1E9d);
        }
        return result;
    }
}
