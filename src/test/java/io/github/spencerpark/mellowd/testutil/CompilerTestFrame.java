package io.github.spencerpark.mellowd.testutil;

import io.github.spencerpark.mellowd.compiler.Compiler;
import io.github.spencerpark.mellowd.compiler.DirectorySourceFinder;
import io.github.spencerpark.mellowd.compiler.ResourceSourceFinder;
import io.github.spencerpark.mellowd.compiler.SourceFinder;
import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.parser.MellowD;
import io.github.spencerpark.mellowd.parser.MellowDCompiler;
import org.antlr.v4.runtime.ANTLRInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class CompilerTestFrame extends ParserTestFrame {

    private SourceFinder srcFinder;
    private TimingEnvironment timingEnvironment;

    protected MellowDCompiler compiler;

    public CompilerTestFrame(String testPrefix, File sourceRoot) {
        super(testPrefix);

        if (sourceRoot != null)
            srcFinder = new DirectorySourceFinder(sourceRoot, Compiler.FILE_EXTENSION);
        else
            srcFinder = new ResourceSourceFinder(Compiler.FILE_EXTENSION);

        this.timingEnvironment = new TimingEnvironment(4, 4, 120);
    }

    public CompilerTestFrame(String testPrefix) {
        this(testPrefix, null);
    }

    public void init(String input) {
        init(new ANTLRInputStream(input));
    }

    public void init(InputStream input) throws IOException {
        init(new ANTLRInputStream(input));
    }

    public void init(Reader input) throws IOException {
        init(new ANTLRInputStream(input));
    }

    public void init(ANTLRInputStream input) {
        super.init(input);

        MellowD mellowD = new MellowD(this.srcFinder, this.timingEnvironment);
        this.compiler = new MellowDCompiler(mellowD);
    }
}
