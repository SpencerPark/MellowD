package org.mellowd.io.live;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.mellowd.compiler.*;
import org.mellowd.io.Compiler;
import org.mellowd.io.DirectorySourceFinder;
import org.mellowd.io.repl.ExecutionException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class MellowDSession {
    private final MellowD mellowD;
    private MellowDCompiler compiler;
    private Path workingDirectory;
    private final CycleScheduler scheduler;

    public MellowDSession(MellowD mellowD, Synthesizer synth, String workingDirectory) throws MidiUnavailableException, InvalidMidiDataException {
        this.mellowD = mellowD;
        this.compiler = new MellowDCompiler(mellowD);
        this.workingDirectory = Paths.get(workingDirectory);

        if (!synth.isOpen()) synth.open();

        this.scheduler = new CycleScheduler(synth, mellowD.getTimingEnvironment(), (block, err) -> {
            System.out.println("Error executing " + block.getName() + ": " + err.getMessage());
            err.printStackTrace(System.out);
        });

        this.scheduler.start();

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

    public synchronized void eval(String code) throws ParseException, CompilationException {
        CharStream input = CharStreams.fromString(code);
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

        if (!parseTree.importStmt().isEmpty()) {
            // Compile the dependencies
            parseTree.importStmt().forEach(this.compiler::visitImportStmt);
        }

        this.compiler.visitSong(parseTree);

        try {
            List<MellowDBlock> blocks = new LinkedList<>();
            this.mellowD.listBlocks().forEach(b -> {
                if (b.getCode().length > 0)
                    blocks.add(b);
            });

            this.scheduler.updateBlocks(blocks);
        } catch (Exception e) {
            throw new ExecutionException("Error executing code: " + e.getLocalizedMessage(), e);
        }
    }
}
