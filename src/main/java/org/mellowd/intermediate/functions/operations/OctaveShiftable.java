package org.mellowd.intermediate.functions.operations;

/**
 * Implementations can be shifted up or down a number of octaves via the implemented
 * {@link #shiftOctave(int)} method.
 *
 * @param <T> Generally the same type as the implementing class but is technically
 *            the resulting type of preforming an octave shift on this class.
 */
public interface OctaveShiftable<T> {

    T shiftOctave(int amount);
}
