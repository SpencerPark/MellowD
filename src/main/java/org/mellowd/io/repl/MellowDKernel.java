package org.mellowd.io.repl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.mellowd.compiler.*;
import org.mellowd.io.Compiler;
import org.mellowd.io.*;

import javax.sound.midi.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MellowDKernel implements Closeable {
    private final MellowD mellowD;
    private MellowDBlock selectedBlock;
    private MellowDCompiler compiler;
    private Path workingDirectory;
    private Synthesizer synth;

    public MellowDKernel(MellowD mellowD, String workingDirectory) throws MidiUnavailableException {
        this.mellowD = mellowD;
        this.compiler = new MellowDCompiler(mellowD);
        this.workingDirectory = Paths.get(workingDirectory);
        this.synth = MidiSystem.getSynthesizer();
    }

    private String formatPath(Path path) {
        return this.workingDirectory.relativize(path).toString();
    }

    public void addSourceDirectory(String rawPath) {
        Path path = Paths.get(rawPath);
        File dir;
        if (path.isAbsolute()) {
            dir = path.toFile();
        } else {
            path = workingDirectory.resolve(rawPath);
            dir = path.toFile();
        }

        if (!dir.isDirectory())
            throw new IllegalArgumentException(String.format("Cannot register source directory '%s': not a directory", path.toAbsolutePath().toString()));

        this.mellowD.addSrcFinder(new DirectorySourceFinder(dir, Compiler.FILE_EXTENSION));
    }

    public void loadSoundFont(String rawPath) {
        Path path = Paths.get(rawPath);
        File soundFontFile;
        if (path.isAbsolute()) {
            soundFontFile = path.toFile();
        } else {
            path = Paths.get(workingDirectory.toAbsolutePath().toString(), rawPath);
            soundFontFile = path.toFile();
        }

        if (!soundFontFile.isFile()) {
            System.err.printf("Sound font path '%s' cannot be resolved to a file", path.toAbsolutePath().toString());
            return;
        }

        System.out.printf("Loading sound font '%s'...\n", formatPath(path));

        Soundbank soundbank;
        try {
            soundbank = MidiSystem.getSoundbank(soundFontFile);
        } catch (InvalidMidiDataException e) {
            System.err.printf("Invalid sound font '%s'. Problem: %s\n",
                    soundFontFile.getName(), e.getLocalizedMessage());
            return;
        } catch (IOException e) {
            System.err.printf("Error loading sound font '%s'. Problem: %s\n",
                    soundFontFile.getName(), e.getLocalizedMessage());
            return;
        }

        if (!synth.isSoundbankSupported(soundbank)) {
            System.err.printf("Sound font '%s' is not supported by the midi system's synthesizer.\n",
                    soundFontFile.getName());
            return;
        }

        boolean allLoaded = synth.loadAllInstruments(soundbank);

        System.out.printf("Loaded %s instruments from sound font %s\n",
                    allLoaded ? "all" : "some", formatPath(path));
    }

    public Sequence eval(String code) throws ParseException, CompilationException {
        CharStream input = new ANTLRInputStream(code);
        MellowDLexer lexer = new MellowDLexer(input);

        TokenStream tokens = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokens);

        ParseErrorListener errorListener = new ParseErrorListener();
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        MellowDParser.SongContext parseTree = parser.song();
        if (errorListener.encounteredError())
            throw new ParseException(errorListener.getErrors());

        MellowDCompiler walker = new MellowDCompiler(mellowD);

        if (!parseTree.importStatement().isEmpty()) {
            //Compile the dependencies
            parseTree.importStatement().forEach(walker::visitImportStatement);
        }

        walker.visitSong(parseTree);

        try {
            return mellowD.execute();
        } catch (Exception e) {
            throw new ExecutionException("Error executing code: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
