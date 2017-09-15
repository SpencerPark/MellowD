package org.mellowd.compiler;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.PrintStream;

public class CompilationException extends RuntimeException {
    private int start;
    private int line;
    private int startPosInLine;
    private int stop;
    private String text;

    public CompilationException(int start, int line, int startPosInLine, int stop, String text, Throwable cause) {
        super(cause);
        this.start = start;
        this.line = line;
        this.startPosInLine = startPosInLine;
        this.stop = stop;
        this.text = text;
    }

    public CompilationException(Token problem, Throwable cause) {
        super(cause);
        init(problem);
    }

    private void init(Token problem) {
        this.start = problem.getStartIndex();
        this.line = problem.getLine();
        this.startPosInLine = problem.getCharPositionInLine();
        this.stop = problem.getStopIndex();
        this.text = problem.getText();
    }

    public CompilationException(ParserRuleContext problem, Throwable cause) {
        super(cause);
        init(problem);
    }

    private void init(ParserRuleContext problem) {
        this.start = problem.start.getStartIndex();
        this.line = problem.start.getLine();
        this.startPosInLine = problem.start.getCharPositionInLine();
        this.stop = problem.stop.getStopIndex();
        this.text = problem.start.getInputStream().getText(Interval.of(this.start, this.stop));
    }

    public CompilationException(ParseTree problem, Throwable cause) {
        super(cause);
        if (problem instanceof ParserRuleContext) {
            init((ParserRuleContext) problem);
        } else if (problem instanceof TerminalNode) {
            init(((TerminalNode) problem).getSymbol());
        } else {
            init(new CommonToken(-1, problem.getText()));
        }
    }

    public int getStart() {
        return start;
    }

    public int getLine() {
        return line;
    }

    public int getStartPosInLine() {
        return startPosInLine;
    }

    public int getStop() {
        return stop;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getMessage() {
        return getCause() != null ? getCause().getMessage() : "null";
    }

    public void print(PrintStream out) {
        out.printf("Compilation exception on line %d@%d-%d:'%s'. Problem: %s\n",
                getLine(),
                getStartPosInLine(),
                getStartPosInLine() + (getStop() - getStart()),
                getText(),
                getCause().getMessage());
    }
}
