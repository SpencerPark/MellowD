package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.variables.UndefinedReferenceException;

import java.util.Set;

public class RuntimeNullCheck<T> implements Expression<T> {
    private final QualifiedName referenceName;
    private final Expression<T> expression;
    private final SourceLink sourceLink;

    public RuntimeNullCheck(QualifiedName referenceName, Expression<T> expression, SourceLink sourceLink) {
        this.referenceName = referenceName;
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.expression.getFreeVariables();
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T value = expression.evaluate(environment);
        if (value == null)
            throw sourceLink.toCompilationException(new UndefinedReferenceException(referenceName));
        return value;
    }
}
