package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.ScopeDependent;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

public interface Statement extends ScopeDependent {
    public static Statement lift(BiConsumer<ExecutionEnvironment, Output> stmtBody) {
        return new Statement() {
            @Override
            public void execute(ExecutionEnvironment environment, Output output) {
                stmtBody.accept(environment, output);
            }

            @Override
            public Set<QualifiedName> getFreeVariables() {
                return Collections.emptySet();
            }
        };
    }

    public void execute(ExecutionEnvironment environment, Output output);
}
