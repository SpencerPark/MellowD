package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.Phrase;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class PlayPhraseStatement implements Statement {
    private Expression<Phrase> toPlay;

    public PlayPhraseStatement(Expression<Phrase> toPlay) {
        this.toPlay = toPlay;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        Phrase phrase = this.toPlay.evaluate(environment);
        output.put(phrase);
    }
}
