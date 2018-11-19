package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.NullOutput;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.ScopeDependent;
import org.mellowd.intermediate.functions.Argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class FunctionCall implements Expression<Object> {
    private final Expression<Closure> function;
    private final Argument<?>[] args;

    public FunctionCall(Expression<Closure> function, Argument<?>[] args) {
        this.function = function;
        this.args = args;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> free = new LinkedHashSet<>(this.function.getFreeVariables());

        Arrays.stream(this.args)
                .map(Argument::getValue)
                .filter(Objects::nonNull)
                .map(ScopeDependent::getFreeVariables)
                .forEach(free::addAll);

        return free;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        Closure closure = this.function.evaluate(environment);
        return closure.call(environment, NullOutput.getInstance(), this.args);
    }
}
