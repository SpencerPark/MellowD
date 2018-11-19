package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.executable.ScopeDependent;

public interface Statement extends ScopeDependent {
    public void execute(ExecutionEnvironment environment, Output output);
}
