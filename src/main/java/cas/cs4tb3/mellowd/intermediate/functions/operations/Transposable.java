package cas.cs4tb3.mellowd.intermediate.functions.operations;

/**
 * Created on 2016-06-19.
 */
public interface Transposable<T extends Transposable<T>> {

    T transpose(int numSemiTones);
}
