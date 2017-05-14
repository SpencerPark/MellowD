package io.github.spencerpark.mellowd.intermediate.executable.expressions;

import io.github.spencerpark.mellowd.intermediate.executable.SourceLink;
import io.github.spencerpark.mellowd.intermediate.variables.UndefinedReferenceException;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

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
