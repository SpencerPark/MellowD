//Parse Exception
//===============

package cas.cs4tb3.mellowd.parser;

import org.antlr.v4.runtime.Token;

//A parse exception is the base exception for errors during parsing. It has information about the
//location of the error for describing the error. It also stores these values to improve IDE support
//in the future. Programmatic compilation can catch the exception and highlight the error directly.
public class ParseException extends RuntimeException {
    private int start;
    private int line;
    private int posInLine;
    private String text;
    private String message;

    public ParseException(Token token) {
        this(token.getStartIndex(), token.getCharPositionInLine(), token.getLine(), token.getText());
    }

    public ParseException(Token token, String message) {
        this(token.getStartIndex(), token.getCharPositionInLine(), token.getLine(), token.getText(), message);
    }

    public ParseException(int start, int posInLine, int lineNum, String text) {
        this(start, posInLine, lineNum, text, "");
    }

    public ParseException(int start, int posInLine, int lineNum, String text, String message) {
        this.start = start;
        this.line = lineNum;
        this.posInLine = posInLine;
        this.text = text;
        this.message = "line " + lineNum + "@" + posInLine + ":\"" + text + "\". " + message;
    }

    public int getStart() {
        return start;
    }

    public int getLine() {
        return line;
    }

    public int getPosInLine() {
        return posInLine;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
