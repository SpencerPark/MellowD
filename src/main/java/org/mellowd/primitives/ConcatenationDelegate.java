package org.mellowd.primitives;

import java.util.HashMap;
import java.util.Map;

/**
 * Essentially acts as a dynamic method invoker for selecting methods
 * at runtime. It's use is in concatenating objects.
 */
public class ConcatenationDelegate<R> {

    @FunctionalInterface
    public interface AppendDelegate<R, T> {
        void append(R root, T toAppend);
    }

    private final Map<Class<?>, AppendDelegate<R, ?>> appendDelegates;

    public ConcatenationDelegate() {
        this.appendDelegates = new HashMap<>();
    }

    /**
     * Add a new delegate to the collection of supported {@link AppendDelegate}s. This
     * usually looks something like:
     * <pre>
     *     addDelegate(Melody.class, Melody::appendTo);
     * </pre>
     * for adding the melody append delegate for a melody concatenation delegate.
     *
     * @param appendType the class of the object that can now be appended to the root.
     * @param delegate   the method that does the appending.
     * @param <T>        the type of the object that can now be appended to the root.
     */
    public <T> void addDelegate(Class<T> appendType, AppendDelegate<R, T> delegate) {
        this.appendDelegates.put(appendType, delegate);
    }

    /**
     * Append {@code other} to the {@code root}. This invokes the appropriate delegate. If
     * one is not found it throws an {@link IllegalArgumentException}.
     *
     * @param root  the root object that is being built upon
     * @param other the object being appended to the root
     *
     * @throws IllegalArgumentException if concatenation with the given object is not supported
     * @see #addDelegate(Class, AppendDelegate)
     */
    @SuppressWarnings("unchecked")
    public void append(R root, Object other) {
        Class<?> type = other.getClass();

        AppendDelegate delegate = null;
        for (Map.Entry<Class<?>, AppendDelegate<R, ?>> appendDelegateEntry : this.appendDelegates.entrySet()) {
            if (appendDelegateEntry.getKey().isAssignableFrom(type)) {
                delegate = appendDelegateEntry.getValue();
                break;
            }
        }

        if (delegate == null)
            throw new IllegalArgumentException("Concatenation with " + root.getClass().getSimpleName() + " and " + other.getClass().getSimpleName() + " is not supported");

        delegate.append(root, other);
    }
}
