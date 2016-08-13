package cas.cs4tb3.mellowd.primitives;

public interface ConcatableComponent {
    //We need distinct classes because some of the type checking
    //occurs at runtime and generic types are erased at that time.

    interface TypeMelody extends ConcatableComponent {
        /**
         * Append this component to the {@code root}
         * @param root the root of the concatenation
         */
        void appendTo(Melody root);

        @Override
        default void appendTo(Object root) {
            if (root instanceof Melody)
                appendTo((Melody) root);
            else
                throw new IllegalArgumentException("Cannot append a melody concatable component to a " + root.getClass().getName());
        }
    }

    interface TypeChord extends ConcatableComponent {
        /**
         * Append this component to the {@code root}
         * @param root the root of the concatenation
         */
        void appendTo(Chord root);

        @Override
        default void appendTo(Object root) {
            if (root instanceof Chord)
                appendTo((Chord) root);
            else
                throw new IllegalArgumentException("Cannot append a chord concatable component to a " + root.getClass().getName());
        }
    }

    interface TypeRhythm extends ConcatableComponent {
        void setSlurred(boolean slur);

        /**
         * Append this component to the {@code root}
         * @param root the root of the concatenation
         */
        void appendTo(Rhythm root);

        @Override
        default void appendTo(Object root) {
            if (root instanceof Rhythm)
                appendTo((Rhythm) root);
            else
                throw new IllegalArgumentException("Cannot append a rhythm concatable component to a " + root.getClass().getName());
        }
    }

    void appendTo(Object root);
}
