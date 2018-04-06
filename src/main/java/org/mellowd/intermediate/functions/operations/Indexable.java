package org.mellowd.intermediate.functions.operations;

import java.util.function.IntConsumer;

/**
 * Implementations can be indexed to return an object of type {@link T} via
 * the {@link #getAtIndex(int)} method. They can also be indexed over a range to
 * return an object of type {@link U} via the {@link #getAtRange(int, int)} method.
 *
 * @param <T> The type of the object that exists in this container. The type
 *            of the object returned by {@link #getAtIndex(int)}
 * @param <U> The type of a sub container of this object. Usually the same type as the class but is essentially just
 *            the type returned by {@link #getAtRange(int, int)}
 */
public interface Indexable<T, U> {

    T getAtIndex(int index);

    U getAtRange(int lower, int upper);

    /**
     * Calculate the true, inbound, index given by the index notation.
     *
     * @param index the given index
     * @param size  the size of the indexable data
     *
     * @return the true index
     */
    static int calcIndex(int index, int size) {
        if (index < 0) return (size + (index % size)) % size;
        else return index % size;
    }

    /**
     * Calculate the number of repetitions required of the underlying indexable to get to the target index.
     * <p>
     * <strong>Ex.</strong> In a list of 3 things: {@code [0:"foo", 1:"bar", 2:"baz"]}:
     * <ul>
     * <li>
     * the index {@code 4} should get {@code "bar"} and it requires the list to look like {@code [0:"foo", 1:"bar",
     * 2:"baz", 3:"foo", 4:"bar", 5:"baz"]} meaning that the overflow would be {@code +1} list as it added one list to
     * the right
     * </li>
     * <li>
     * the index {@code -1} should get {@code "baz"} and it requires the list to look like {@code [-3:"foo", -2:"bar",
     * -1:"baz", 0:"foo", 1:"bar", 2:"baz"]} meaning that the overflow would be {@code -1} lists as it added one list
     * to
     * the left.
     * </li>
     * </ul>
     *
     * @param index the given index
     * @param size  the size of the indexable data
     *
     * @return the number of {@code size} repetitions required to reach the {@code index}
     *
     * @throws IllegalArgumentException if {@code size} is not in the range {@code [1, inf)}
     */
    static int calcIndexOverflow(int index, int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Cannot calculate index overflow of an empty indexable.");

        return index / size;
    }

    /**
     * Iterate over to described range. There are 3 types of iterations based on the bounds.
     * <p>
     * If {@code lower == upper}: the range is just a single index, the {@code lower = upper} index
     * <p>
     * If {@code upper > lower}: the standard case for iterating forwards from {@code lower} to {@code upper}
     * <p>
     * If {@code lower > upper}: iterating backwards from {@code upper} to {@code lower}.
     *
     * @param lower the left side of the range (inclusive)
     * @param upper the right side of the range (inclusive)
     * @param apply the consumer to apply to each index in the range
     */
    static void forEachInRange(int lower, int upper, IntConsumer apply) {
        if (upper == lower) {
            apply.accept(lower);
        } else if (lower < upper) {
            for (int i = lower; i <= upper; i++)
                apply.accept(i);
        } else {
            for (int i = lower; i >= upper; i--)
                apply.accept(i);
        }
    }

    /**
     * Calculate the number of indexes in the range
     *
     * @param lower the left side of the range (inclusive)
     * @param upper the right side of the range (inclusive)
     *
     * @return the number of indexes in the range
     */
    static int sizeOfRange(int lower, int upper) {
        if (upper >= lower) return upper - lower + 1;
        return lower - upper + 1;
    }
}
