package io.github.spencerpark.mellowd.intermediate.executable.expressions;

import io.github.spencerpark.mellowd.intermediate.Phrase;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;
import io.github.spencerpark.mellowd.primitives.Melody;
import io.github.spencerpark.mellowd.primitives.Rhythm;

public class PhraseConstruction implements Expression<Phrase> {
    private final Expression<Melody> melodyExpression;
    private final Expression<Rhythm> rhythmExpression;

    public PhraseConstruction(Expression<Melody> melodyExpression, Expression<Rhythm> rhythmExpression) {
        this.melodyExpression = melodyExpression;
        this.rhythmExpression = rhythmExpression;
    }

    @Override
    public Phrase evaluate(ExecutionEnvironment environment) {
        Melody melody = melodyExpression.evaluate(environment);
        Rhythm rhythm = rhythmExpression.evaluate(environment);
        return new Phrase(melody, rhythm);
    }
}
