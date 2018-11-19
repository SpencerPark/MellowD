package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.executable.ScopeDependent;

public interface Expression<T> extends ScopeDependent {
    public T evaluate(ExecutionEnvironment environment);
}
