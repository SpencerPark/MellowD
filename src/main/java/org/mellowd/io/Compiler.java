//Compiler
//========

package org.mellowd.io;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.mellowd.compiler.*;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.plugin.PluginManager;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//The `Compiler` class is the main entry point for the program.
public class Compiler {
    private static final double NS_PER_SEC = 1E9d;

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

    public static final String VERSION;

    static {
        Properties metadata = new Properties();
        try {
            metadata.load(Compiler.class.getResourceAsStream("/metadata.properties"));
        } catch (IOException ignored) { }

        VERSION = metadata.getProperty("version", "UNKNOWN");
    }

    private static final Pattern CURRENT_DIRECTORY_PREFIX =
            Pattern.compile("^" + Pattern.quote(new File("").getAbsolutePath() + File.separator));

    private static String formatPath(File file) {
        String absPath = file.getAbsolutePath();
        Matcher m = CURRENT_DIRECTORY_PREFIX.matcher(absPath);
        return m.find() ? m.replaceFirst("") : absPath;
    }

    public static void main(String[] args) throws Exception {
        CompilerOptions options = ArgParser.parseOrPrint(args);
        if (options == null) System.exit(1);

        if (options.wantsVerbose())
            System.out.printf("Compiling with MellowD-v%s\n", VERSION);

        //Now that we have valid arguments we need to pull the information
        //out of the parser.
        File outDir = handleOutDir(options, new File(options.getOutputDirectory()).getAbsoluteFile());
        File toCompile = handleInFile(new File(options.getSource()).getAbsoluteFile());

        //Now we can begin compiling
        Sequence compilationResult = null;
        try {
            //Compile the input file with the given timing arguments.
            compilationResult = compile(toCompile, options);

            //Display the duration of the compiled song in seconds.
            if (options.wantsVerbose()) {
                System.out.printf("Song length: %d s\n",
                        TimeUnit.MICROSECONDS.toSeconds(compilationResult.getMicrosecondLength()));
            }
            //If an IOException occurs then we had a problem with the input file. We will
            //display the error and exit.
        } catch (IOException e) {
            System.err.printf("Error reading input file (%s). Reason: %s\n",
                    formatPath(toCompile), e.getLocalizedMessage());
            System.exit(1);
            //If a ParseException occurs then there was a problem with the data in the input
            //file. The input is well formed but the semantics are wrong. Display the error and exit.
        } catch (CompilationException e) {
            e.print(System.err);
            System.exit(1);
        } catch (ParseException e) {
            for (SyntaxErrorReport errorReport : e.getProblems()) {
                System.err.println(errorReport.getErrorType().toString() + ": " + errorReport.getMessage());
            }
            System.exit(1);
        }

        handleOutput(options, toCompile, outDir, compilationResult);
    }

    private static void handleOutput(CompilerOptions options, File source, File outDir, Sequence compilationResult) {
        String srcName = source.getName().replace(FILE_EXTENSION, "");
        try {
            if (options.shouldPlayLive()) {
                //Create a music player from the sequencer and song.
                SequencePlayer player = new SequencePlayer(MidiSystem.getSequencer(), createSoundFontLoader(options), compilationResult);

                if (options.wantsVerbose())
                    System.out.printf("Playing %s\n", srcName);

                //Start the player with a 1000ms count in to account for the loading lag
                player.playSync(1000L);

                if (options.wantsVerbose())
                    System.out.println("Play complete!");
            }

            try {
                //If the compilation result is empty then append the EOT event to
                //make the output file playable.
                if (compilationResult.getTickLength() == 0) {
                    compilationResult.getTracks()[0].add(new MidiEvent(EOT_MESSAGE, 1));
                }

                if (options.shouldOutputMIDI()) {
                    File outFile = new File(outDir, srcName + ".mid");
                    if (!outFile.exists() && !outFile.createNewFile()) {
                        System.err.printf("Cannot create output file %s\n", formatPath(outFile));
                    } else {
                        MIDIIODelegate.getInstance().save(compilationResult, outFile);

                        if (options.wantsVerbose())
                            System.out.printf("%s compiled to %s\n", srcName + FILE_EXTENSION, formatPath(outFile));
                    }
                }

                if (options.shouldOutputWAV()) {
                    File outFile = new File(outDir, srcName + ".wav");
                    if (!outFile.exists() && !outFile.createNewFile()) {
                        System.err.printf("Cannot create output file %s\n", formatPath(outFile));
                    } else {
                        long writeStartTime = System.nanoTime();
                        new WavIODelegate(createSoundFontLoader(options)).save(compilationResult, outFile);

                        if (options.wantsVerbose()) {
                            long writeTime = System.nanoTime() - writeStartTime;
                            System.out.printf("Conversion to WAV took %.4f s\n", writeTime / NS_PER_SEC);
                            System.out.printf("%s compiled to %s\n", srcName + FILE_EXTENSION, formatPath(outFile));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.printf("Error writing compilation result. Reason: %s.\n", e.getLocalizedMessage());
                System.exit(1);
            }

            System.exit(0);
            //If a MidiUnavailableException occurs let the user know the error and exit.
        } catch (MidiUnavailableException e) {
            System.err.printf("Midi system not available. %s.\n", e.getLocalizedMessage());
            System.exit(1);
            //If an InvalidMidiDataException occurs it is most likely our fault. This error
            //should be reported so ask the user to report it.
        } catch (InvalidMidiDataException e) {
            System.err.printf("Midi compilation error. Something went wrong, please submit your source to" +
                    " the issue tracker with the %s version tag. Error: %s\n", VERSION, e.getLocalizedMessage());
            System.exit(1);
        }
    }

    //`handleOutDir` tries its best to use the given `outDir` and if it can't
    //it reports the problem to the user and closes the program.
    private static File handleOutDir(CompilerOptions options, File outDir) {
        //If the directory doesn't exits try and create it
        if (!outDir.exists()) {
            try {
                boolean created = outDir.mkdirs();
                //If `mkdirs` returns false the the directory doesn't exist because we are
                //only at this point if it didn't exists in the first place.
                if (!created) {
                    System.err.printf("Could not create outdir %s\n", formatPath(outDir));
                    System.exit(1);
                }
                //If a SecurityException occurs let the user know that there is nothing we can do
                //to create the directory for them and close the program.
            } catch (SecurityException e) {
                System.err.printf("Could not create outdir (%s). Stopped by the security manager: %s",
                        formatPath(outDir), e.getLocalizedMessage());
                System.exit(1);
            }
            //If we make it down here than all went well and the directory was created so let
            //the user know we made a new directory on their machine.
            if (options.wantsVerbose())
                System.out.printf("Created directory %s\n", formatPath(outDir));
            //If the path exists but is not a directory then we can't put the compilation result
            //anywhere. Let the user know they gave us a path to an existing file and close the program.
        } else if (!outDir.isDirectory()) {
            System.err.printf("outdir (%s) is not a directory\n", formatPath(outDir));
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
            System.err.printf("Cannot find input file %s.\n", formatPath(inFile));
            System.exit(1);
        }

        //If the file doesn't end with `.mlod` then it isn't a Mellow D source and
        //the user most likely gave the wrong file. Let them know the file they gave
        //and tell them we can't use it.
        if (!inFile.getName().endsWith(FILE_EXTENSION)) {
            System.err.printf("In file (%s) is not a Mellow D source file (.mlod).\n",
                    formatPath(inFile));
            System.exit(1);
        }

        //Return the input file
        return inFile;
    }

    private static Function<Synthesizer, Synthesizer> createSoundFontLoader(CompilerOptions options) {
        return (synth) -> {
            if (options.getSoundFonts().isEmpty()) return synth;

            if (!synth.isOpen())
                System.err.printf("Synth must be open before applying sound fonts\n");

            for (String soundFontPath : options.getSoundFonts()) {
                File soundFontFile = new File(soundFontPath);
                if (options.wantsVerbose())
                    System.out.printf("Loading sound font %s...\n", formatPath(soundFontFile));

                Soundbank soundbank;
                try {
                    soundbank = MidiSystem.getSoundbank(soundFontFile);
                } catch (InvalidMidiDataException e) {
                    System.err.printf("Invalid sound font %s. Problem: %s\n",
                            soundFontFile.getName(), e.getLocalizedMessage());
                    System.exit(1);
                    return synth;
                } catch (IOException e) {
                    System.err.printf("Error loading sound font %s. Problem: %s\n",
                            soundFontFile.getName(), e.getLocalizedMessage());
                    System.exit(1);
                    return synth;
                }

                if (!synth.isSoundbankSupported(soundbank)) {
                    System.err.printf("Sound font %s is not supported by your midi system's synthesizer.\n",
                            soundFontFile.getName());
                    System.exit(1);
                    return synth;
                }

                boolean allLoaded = synth.loadAllInstruments(soundbank);

                if (options.wantsVerbose())
                    System.out.printf("Loaded %s instruments from sound font %s\n",
                            allLoaded ? "all" : "some", formatPath(soundFontFile));
            }

            return synth;
        };
    }

    public static Sequence compile(File src, CompilerOptions options) throws Exception {
        return compile(src, options, null);
    }

    public static Sequence compile(File src, CompilerOptions options, PluginManager plugins) throws Exception {
        return compile(new ANTLRFileStream(src.getAbsolutePath()),
                new DirectorySourceFinder(src.getAbsoluteFile().getParentFile(), FILE_EXTENSION),
                options,
                plugins);
    }

    public static Sequence compile(CharStream inStream, SourceFinder srcFinder, CompilerOptions options) throws Exception {
        return compile(inStream, srcFinder, options, null);
    }

    public static Sequence compile(CharStream inStream, SourceFinder srcFinder, CompilerOptions options, PluginManager plugins) throws Exception {
        List<SourceFinder> sourceFinders = new LinkedList<>();
        //First priority is the given finder
        if (srcFinder != null) sourceFinders.add(srcFinder);
        //Second is the user requested additional directories
        options.getSourceDirs().forEach(dir ->
                sourceFinders.add(new DirectorySourceFinder(new File(dir).getAbsoluteFile(), FILE_EXTENSION)));
        //Last priority is to look in the classpath
        sourceFinders.add(new ResourceSourceFinder(FILE_EXTENSION));
        srcFinder = new CompositeSourceFinder(sourceFinders.toArray(new SourceFinder[sourceFinders.size()]));

        //First we will display the inputs being used so they can double check everything
        //is as expected.
        if (options.wantsVerbose()) {
            System.out.printf("Time Signature: %d / %d\n", options.getTimeSignatureTop(), options.getTimeSignatureBottom());
            System.out.printf("Tempo: %d bpm\n", options.getTempo());
            System.out.printf("Compiling: %s\n", inStream.getSourceName());
        }

        //Now we can build a lexer with the `src` as the input.
        MellowDLexer lexer = new MellowDLexer(inStream);

        //The parser takes the tokens from the lexer as well as the timing environment constructed
        //from the input arguments and a track manager.
        TokenStream tokens = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokens);

        ParseErrorListener errorListener = new ParseErrorListener();
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        TimingEnvironment timingEnvironment = new TimingEnvironment(options.getTimeSignatureTop(), options.getTimeSignatureBottom(), options.getTempo());

        //Parse the input!
        long parseStart = System.nanoTime();
        MellowDParser.SongContext parseTree = parser.song();
        if (errorListener.encounteredError())
            throw new ParseException(errorListener.getErrors());

        if (options.wantsVerbose()) {
            long parseTime = System.nanoTime() - parseStart;
            System.out.printf("Parsing took %.4f s\n",
                    parseTime / NS_PER_SEC);
        }

        //If a plugin manager is not given, make a new one. If a new one is made it
        //should be cleaned up in the end.
        boolean freshPluginManager = false;
        if (plugins == null) {
            plugins = new PluginManager();
            freshPluginManager = true;
        }

        MellowD mellowD = new MellowD(srcFinder, timingEnvironment);

        plugins.applySome(mellowD, options.getPlugins());

        try {
            MellowDCompiler walker = new MellowDCompiler(mellowD);

            if (!parseTree.importStmt().isEmpty()) {
                // Compile the dependencies
                long dependencyCompStart = System.nanoTime();
                parseTree.importStmt().forEach(walker::visitImportStmt);
                if (options.wantsVerbose()) {
                    long dependencyCompTime = System.nanoTime() - dependencyCompStart;
                    System.out.printf("Dependency compilation took %.4f s\n",
                            dependencyCompTime / NS_PER_SEC);
                }
            }

            //Compile the body
            long compileStart = System.nanoTime();
            walker.visitSong(parseTree);
            if (options.wantsVerbose()) {
                long compileTime = System.nanoTime() - compileStart;
                System.out.printf("Compilation took %.4f s\n",
                        compileTime / NS_PER_SEC);
            }

            //Execute all of the compiled statements to build the output
            long executionStart = System.nanoTime();
            Sequence result = mellowD.execute().toSequence();
            if (options.wantsVerbose()) {
                long executionTime = System.nanoTime() - executionStart;
                System.out.printf("Execution took %.4f s\n",
                        executionTime / NS_PER_SEC);
            }
            return result;
        } finally {
            //No matter what we need to clean up the plugin manager
            if (freshPluginManager) plugins.unloadAll();
        }
    }
}
