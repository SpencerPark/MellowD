package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.intermediate.Phrase;
import io.github.spencerpark.mellowd.intermediate.executable.expressions.Expression;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

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
