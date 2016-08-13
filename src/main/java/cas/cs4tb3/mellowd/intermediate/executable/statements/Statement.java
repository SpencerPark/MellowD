package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public interface Statement {

    void execute(ExecutionEnvironment environment, Output output);
}
