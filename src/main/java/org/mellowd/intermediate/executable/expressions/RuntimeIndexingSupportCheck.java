package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.executable.IndexingNotSupportedException;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.functions.operations.Indexable;

import java.util.Set;

public class RuntimeIndexingSupportCheck implements Expression<Indexable<?, ?>> {
    protected final Expression<?> expression;
    protected final SourceLink sourceLink;

    public RuntimeIndexingSupportCheck(Expression<?> expression, SourceLink sourceLink) {
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.expression.getFreeVariables();
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
