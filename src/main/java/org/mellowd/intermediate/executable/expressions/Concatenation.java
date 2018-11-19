package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.primitives.ConcatenationDelegate;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Concatenation<T> implements Expression<T> {
    private final Supplier<T> root;
    private final ConcatenationDelegate<T> concatenationDelegate;
    private final List<Expression<?>> params;

    public Concatenation(Supplier<T> root, ConcatenationDelegate<T> concatenationDelegate) {
        this.root = root;
        this.concatenationDelegate = concatenationDelegate;
        this.params = new LinkedList<>();
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> names = new LinkedHashSet<>();
        this.params.forEach(e -> names.addAll(e.getFreeVariables()));
        return names;
    }

    public void addArgument(Expression<?> arg) {
        this.params.add(arg);
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T evalRes = root.get();
        this.params.forEach(e -> concatenationDelegate.append(evalRes, e.evaluate(environment)));
        return evalRes;
    }
}
