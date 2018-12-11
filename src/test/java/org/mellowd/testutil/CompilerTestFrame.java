package org.mellowd.testutil;

import org.antlr.v4.runtime.CharStream;
import org.mellowd.compiler.MellowD;
import org.mellowd.compiler.MellowDCompiler;
import org.mellowd.io.Compiler;
import org.mellowd.io.DirectorySourceFinder;
import org.mellowd.io.ResourceSourceFinder;
import org.mellowd.io.SourceFinder;
import org.mellowd.midi.TimingEnvironment;

import java.io.File;

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

    @Override
    public void init(CharStream input) {
        super.init(input);

        MellowD mellowD = new MellowD(this.srcFinder, this.timingEnvironment);
        this.compiler = new MellowDCompiler(mellowD);
    }
}
