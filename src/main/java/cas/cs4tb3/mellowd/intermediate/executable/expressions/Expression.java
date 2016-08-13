package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.function.Function;

public interface Expression<T> {
    T evaluate(ExecutionEnvironment environment);

    default <U> Expression<U> thenApply(Function<T, U> function) {
        return env -> {
            T value = this.evaluate(env);
            return function.apply(value);
        };
    }
}
