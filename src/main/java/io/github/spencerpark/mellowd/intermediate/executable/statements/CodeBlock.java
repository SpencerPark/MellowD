package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

import java.util.LinkedList;
import java.util.List;

public class CodeBlock implements Statement {
    protected final List<Statement> statements;

    public CodeBlock() {
        this.statements = new LinkedList<>();
    }

    public void add(Statement statement) {
        this.statements.add(statement);
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (Statement stmt : statements) {
            stmt.execute(environment, output);
        }
    }
}
