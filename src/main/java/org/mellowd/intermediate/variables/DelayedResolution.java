package org.mellowd.intermediate.variables;

public interface DelayedResolution<T> {
    T resolve(Memory scope);
}
