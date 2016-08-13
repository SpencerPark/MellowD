package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

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
