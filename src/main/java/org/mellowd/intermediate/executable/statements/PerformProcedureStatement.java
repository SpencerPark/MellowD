package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.ScopeDependent;
import org.mellowd.intermediate.executable.expressions.Expression;
import org.mellowd.intermediate.functions.Argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class PerformProcedureStatement implements Statement {
    private final Expression<Closure> procedure;
    private final Argument<?>[] args;

    public PerformProcedureStatement(Expression<Closure> procedure, Argument<?>[] args) {
        this.procedure = procedure;
        this.args = args;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> free = new LinkedHashSet<>(this.procedure.getFreeVariables());

        Arrays.stream(this.args)
                .map(Argument::getValue)
                .filter(Objects::nonNull)
                .map(ScopeDependent::getFreeVariables)
                .forEach(free::addAll);

        return free;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        Closure closure = this.procedure.evaluate(environment);
        closure.call(environment, output, this.args);
    }
}
