package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.compiler.ExecutionEnvironment;

public class EmptyStatement implements Statement {
    private static final EmptyStatement instance = new EmptyStatement();

    public static EmptyStatement getInstance() {
        return instance;
    }

    private EmptyStatement() { }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
    }
}
