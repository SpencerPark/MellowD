package org.mellowd.intermediate.executable.expressions;

import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.variables.UndefinedReferenceException;
import org.mellowd.parser.ExecutionEnvironment;

public class RuntimeNullCheck<T> implements Expression<T> {
    private final String referenceName;
    private final Expression<T> expression;
    private final SourceLink sourceLink;

    public RuntimeNullCheck(String referenceName, Expression<T> expression, SourceLink sourceLink) {
        this.referenceName = referenceName;
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T value = expression.evaluate(environment);
        if (value == null)
            throw sourceLink.toCompilationException(new UndefinedReferenceException(referenceName));
        return value;
    }
}
