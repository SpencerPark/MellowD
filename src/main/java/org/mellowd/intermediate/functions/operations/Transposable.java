package org.mellowd.intermediate.functions.operations;

/**
 * Implementations can be transposed via the implemented
 * {@link #transpose(int)} method.
 *
 * @param <T> Generally the same type as the implementing class but is technically
 *            the resulting type of preforming an transpose on this class.
 */
public interface Transposable<T> {

    T transpose(int numSemiTones);
}
