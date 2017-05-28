package org.mellowd.primitives;

import org.mellowd.intermediate.Phrase;

import java.util.function.Supplier;

public enum Primitives {
    CHORD(Chord.class, Chord::new),
    RHYTHM(Rhythm.class, Rhythm::new),
    MELODY(Melody.class, Melody::new),
    PHRASE(Phrase.class, () -> new Phrase(new Melody(), new Rhythm()));

    private final Class<?> type;
    private final Supplier<?> supplier;

    Primitives(Class<?> type, Supplier<?> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    public Class<?> getType() {
        return type;
    }

    public Object createNew() {
        return supplier.get();
    }
}
