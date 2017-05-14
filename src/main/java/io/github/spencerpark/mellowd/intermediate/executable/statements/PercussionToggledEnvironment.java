package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.intermediate.variables.Memory;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

public class PercussionToggledEnvironment implements ExecutionEnvironment {
    private final ExecutionEnvironment wrapped;

    public PercussionToggledEnvironment(ExecutionEnvironment wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isPercussion() {
        return !wrapped.isPercussion();
    }

    @Override
    public Memory getMemory(String... qualifier) {
        return wrapped.getMemory(qualifier);
    }

    @Override
    public Memory createScope(String... qualifier) {
        return wrapped.createScope(qualifier);
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return wrapped.getTimingEnvironment();
    }
}
