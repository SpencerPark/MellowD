package io.github.spencerpark.mellowd.midi;

import io.github.spencerpark.mellowd.primitives.Pitch;

public class PitchIndexedArray<T> {
    private static final int REST_INDEX = 128;
    private final T[] data;

    public PitchIndexedArray() {
        this.data = (T[]) new Object[129];
    }

    public PitchIndexedArray(T defaultValue) {
        this.data = (T[]) new Object[129];
        for (int i = 0; i < 129; i++)
            this.data[i] = defaultValue;
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
}
