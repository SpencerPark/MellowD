package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

public interface Statement {

    void execute(ExecutionEnvironment environment, Output output);
}
