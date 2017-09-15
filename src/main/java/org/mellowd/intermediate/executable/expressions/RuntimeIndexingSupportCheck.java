package org.mellowd.intermediate.executable.expressions;

import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.compiler.IndexingNotSupportedException;

public class RuntimeIndexingSupportCheck implements Expression<Indexable<?, ?>> {
    protected final Expression<?> expression;
    protected final SourceLink sourceLink;

    public RuntimeIndexingSupportCheck(Expression<?> expression, SourceLink sourceLink) {
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public Indexable<?, ?> evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || value instanceof Indexable) {
            return (Indexable) value;
        } else {
            throw sourceLink.toCompilationException(new IndexingNotSupportedException(sourceLink.text));
        }
    }
}
