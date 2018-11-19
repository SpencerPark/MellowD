package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.variables.IncorrectTypeException;

import java.util.Set;

public class RuntimeTypeCheck<T> implements Expression<T> {
    private final Class<T> type;
    private final Expression<?> expression;
    private final SourceLink sourceLink;

    public RuntimeTypeCheck(Class<T> type, Expression<?> expression, SourceLink sourceLink) {
        this.type = type;
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.expression.getFreeVariables();
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || type.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            // TODO maybe incorrect type should not take an identifier...
            throw sourceLink.toCompilationException(new IncorrectTypeException(QualifiedName.ofUnqualified(sourceLink.text), value.getClass(), type));
        }
    }
}
