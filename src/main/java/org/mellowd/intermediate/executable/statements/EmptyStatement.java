package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;

import java.util.Collections;
import java.util.Set;

public class EmptyStatement implements Statement {
    private static final EmptyStatement instance = new EmptyStatement();

    public static EmptyStatement getInstance() {
        return instance;
    }

    private EmptyStatement() { }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return Collections.emptySet();
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
    }
}
