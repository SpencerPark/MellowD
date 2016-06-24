package cas.cs4tb3.mellowd.intermediate.functions.operations;

/**
 * Created on 2016-06-20.
 */
public interface Concatable<T extends Concatable<T>> {

    T concat(T after);
}
