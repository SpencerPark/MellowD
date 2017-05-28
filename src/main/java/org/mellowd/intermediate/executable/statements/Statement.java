package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.parser.ExecutionEnvironment;

public interface Statement {

    void execute(ExecutionEnvironment environment, Output output);
}
