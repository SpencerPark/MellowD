package org.mellowd.intermediate.functions.operations;

/**
 * Implementations can be concatenated with objects of type {@link P} to produce
 * a resulting object of type {@link R} via the {@link #concat(P)} method
 *
 * @param <P> Generally the same type as the implementing class but is technically
 *            the of the object that is concatenated with this object.
 * @param <R> Generally the same type as the implementing class but is technically
 *            the resulting type of preforming a concatenation with an object of type
 *            {@link P}
 */
public interface Concatable<P, R> {

    R concat(P after);
}
