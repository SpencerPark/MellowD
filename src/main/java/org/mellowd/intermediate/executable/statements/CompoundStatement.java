package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.compiler.ExecutionEnvironment;

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
