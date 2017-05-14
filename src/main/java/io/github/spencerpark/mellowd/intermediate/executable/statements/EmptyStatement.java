package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

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
