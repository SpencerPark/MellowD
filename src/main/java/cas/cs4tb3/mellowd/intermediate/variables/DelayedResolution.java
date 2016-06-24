package cas.cs4tb3.mellowd.intermediate.variables;

public interface DelayedResolution<T> {

    T resolve(Memory scope);
}
