package cas.cs4tb3.mellowd.intermediate.variables;

import java.util.function.Function;

public interface ContextDependent<T> {

    T resolve(Memory scope);

    default <R> ContextDependent<R> andThen(Function<T, R> after) {
        return (scope) -> after.apply(this.resolve(scope));
    }
}
