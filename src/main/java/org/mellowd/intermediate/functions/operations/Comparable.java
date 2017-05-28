package org.mellowd.intermediate.functions.operations;

public interface Comparable {
    enum Operator {
        LT {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult < 0;
            }
        },
        LEQ {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult <= 0;
            }
        },
        GT {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult > 0;
            }
        },
        GEQ {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult >= 0;
            }
        },
        EQ {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult == 0;
            }
        },
        NEQ {
            @Override
            public boolean isTrue(int comparisonResult) {
                return comparisonResult != 0;
            }
        };

        public abstract boolean isTrue(int comparisonResult);
    }

    /**
     * Compare this element to the other
     * @param other the other to compare this object to
     * @return <ul>
     *              <li><strong>&lt; 0</strong> if {@code this} object is less than the {@code other}</li>
     *              <li><strong>0</strong> if {@code this} object is the same as the {@code other}</li>
     *              <li><strong>&gt; 0</strong> if {@code this} object is greater than the {@code other}</li>
     *         </ul>
     * @throws IllegalArgumentException if the type of {@code other} is not comparable to this
     *                                  object.
     */
    int compareTo(Object other) throws IllegalArgumentException;
}
