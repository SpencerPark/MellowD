package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.Phrase;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.primitives.Melody;
import cas.cs4tb3.mellowd.primitives.Rhythm;

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
