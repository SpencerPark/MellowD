package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class CompoundStatement implements Statement {
    private final Statement[] statements;

    public CompoundStatement(Statement... statements) {
        this.statements = statements;
    }

    public Statement[] getStatements() {
        return this.statements;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (Statement stmt : statements)
            stmt.execute(environment, output);
    }
}
