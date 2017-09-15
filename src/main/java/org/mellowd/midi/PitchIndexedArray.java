package org.mellowd.midi;

import org.mellowd.primitives.Pitch;

import java.util.function.BiConsumer;

public class PitchIndexedArray<T> {
    private static final int REST_INDEX = 128;
    private final T[] data;

    public PitchIndexedArray() {
        this.data = (T[]) new Object[129];
    }

    public PitchIndexedArray(T defaultValue) {
        this.data = (T[]) new Object[129];
        this.setAll(defaultValue);
    }

    public T get(Pitch index) {
        if (index == Pitch.REST)
            return data[REST_INDEX];
        return data[index.getMidiNum()];
    }

    public void set(Pitch index, T value) {
        if (index == Pitch.REST)
            data[REST_INDEX] = value;
        else
            data[index.getMidiNum()] = value;
    }

    public void setAll(T value) {
        for (int i = 0; i < 129; i++)
            this.data[i] = value;
    }

    public void forEach(BiConsumer<Pitch, T> consumer) {
        for (int i = 0; i < 128; i++)
            consumer.accept(Pitch.getPitch(i), data[i]);
        consumer.accept(Pitch.REST, data[REST_INDEX]);
    }
}
