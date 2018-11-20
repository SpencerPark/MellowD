package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.ScopeDependent;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public interface Expression<T> extends ScopeDependent {
    public static <T> Expression<T> lift(Function<ExecutionEnvironment, T> exprBody) {
        return new Expression<T>() {
            @Override
            public T evaluate(ExecutionEnvironment environment) {
                return exprBody.apply(environment);
            }

            @Override
            public Set<QualifiedName> getFreeVariables() {
                return Collections.emptySet();
            }
        };
    }

    public T evaluate(ExecutionEnvironment environment);
}
