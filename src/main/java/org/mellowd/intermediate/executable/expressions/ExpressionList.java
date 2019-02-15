package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpressionList<T> implements Expression<List<T>> {
    private final List<Expression<T>> exprs;

    public ExpressionList(List<Expression<T>> exprs) {
        this.exprs = exprs;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> names = new LinkedHashSet<>();
        this.exprs.forEach(e -> names.addAll(e.getFreeVariables()));
        return names;
    }

    @Override
    public List<T> evaluate(ExecutionEnvironment env) {
        return exprs.stream().map(e -> e.evaluate(env)).collect(Collectors.toList());
    }
}
