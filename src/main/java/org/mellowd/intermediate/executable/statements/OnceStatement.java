package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class OnceStatement implements Statement {
    private final Statement statement;

    private final AtomicBoolean evaluated = new AtomicBoolean(false);

    public OnceStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return null;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        if (this.evaluated.get()) return;

        try {
            this.statement.execute(environment, output);
        } finally {
            this.evaluated.set(true);
        }
    }
}
