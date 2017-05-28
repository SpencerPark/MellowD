package org.mellowd.testutil;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.mellowd.TestErrorListener;
import org.mellowd.parser.MellowDLexer;
import org.mellowd.parser.MellowDParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ParserTestFrame {

    private final String testPrefix;

    protected MellowDParser parser;
    protected TestErrorListener errorListener;

    public ParserTestFrame(String testPrefix) {
        this.testPrefix = testPrefix;
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
        MellowDLexer lexer = new MellowDLexer(input);
        CommonTokenStream stream = new CommonTokenStream(lexer);

        this.parser = new MellowDParser(stream);

        this.parser.setBuildParseTree(true);
        this.parser.removeErrorListeners();
        this.errorListener = new TestErrorListener();
        this.parser.addErrorListener(this.errorListener);
    }

    public String takeTestHeader(String classifier) {
        return "[" + testPrefix + "-" + classifier + "]";
    }
}
