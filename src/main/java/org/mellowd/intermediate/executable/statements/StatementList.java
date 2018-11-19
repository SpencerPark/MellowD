package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StatementList implements Statement {
    protected final List<Statement> statements;

    public StatementList() {
        this.statements = new LinkedList<>();
    }

    public void add(Statement statement) {
        this.statements.add(statement);
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> names = new LinkedHashSet<>();
        this.statements.forEach(s -> names.addAll(s.getFreeVariables()));
        return names;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (Statement stmt : statements) {
            stmt.execute(environment, output);
        }
    }
}
