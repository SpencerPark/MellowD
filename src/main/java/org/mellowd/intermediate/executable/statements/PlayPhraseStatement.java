package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.Phrase;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.expressions.Expression;

import java.util.Set;

public class PlayPhraseStatement implements Statement {
    private Expression<Phrase> toPlay;

    public PlayPhraseStatement(Expression<Phrase> toPlay) {
        this.toPlay = toPlay;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.toPlay.getFreeVariables();
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        Phrase phrase = this.toPlay.evaluate(environment);
        output.put(phrase);
    }
}
