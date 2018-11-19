package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Phrase;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.primitives.Melody;
import org.mellowd.primitives.Rhythm;

import java.util.LinkedHashSet;
import java.util.Set;

public class PhraseConstruction implements Expression<Phrase> {
    private final Expression<Melody> melodyExpression;
    private final Expression<Rhythm> rhythmExpression;

    public PhraseConstruction(Expression<Melody> melodyExpression, Expression<Rhythm> rhythmExpression) {
        this.melodyExpression = melodyExpression;
        this.rhythmExpression = rhythmExpression;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> free = new LinkedHashSet<>(this.melodyExpression.getFreeVariables());
        free.addAll(this.rhythmExpression.getFreeVariables());
        return free;
    }

    @Override
    public Phrase evaluate(ExecutionEnvironment environment) {
        Melody melody = melodyExpression.evaluate(environment);
        Rhythm rhythm = rhythmExpression.evaluate(environment);
        return new Phrase(melody, rhythm);
    }
}
