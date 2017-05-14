package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

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
