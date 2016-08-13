package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.primitives.ConcatableComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Concatenation<T, U extends ConcatableComponent> implements Expression<T> {
    private final Supplier<T> root;
    private final List<Expression<? extends U>> params;

    public Concatenation(Supplier<T> root) {
        this.root = root;
        this.params = new LinkedList<>();
    }

    public void addArgument(Expression<? extends U> arg) {
        this.params.add(arg);
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T evalRes = root.get();
        this.params.forEach(e -> e.evaluate(environment).appendTo(evalRes));
        return evalRes;
    }
}
