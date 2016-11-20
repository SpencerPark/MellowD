package cas.cs4tb3.mellowd.intermediate.functions.operations;

/**
 * Implementations can be indexed to return an object of type {@link T} via
 * the {@link #getAtIndex(int)} method.
 *
 * @param <T> The type of the object that exists in this container. The type
 *            of the object returned by {@link #getAtIndex(int)}
 */
public interface Indexable<T> {

    T getAtIndex(int index);

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

        if (index < 0) return index / size;
        else return (index + 1) / size;
    }
}
