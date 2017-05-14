package io.github.spencerpark.mellowd.intermediate.executable.expressions;

import io.github.spencerpark.mellowd.intermediate.variables.Memory;
import io.github.spencerpark.mellowd.midi.GeneralMidiPercussion;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

public class ReferenceResolution implements Expression<Object> {
    private final String[] qualifier;
    private final String name;

    public ReferenceResolution(String[] qualifier, String name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        if (environment.isPercussion() && qualifier.length == 0) {
            GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup(name);
            if (drumSound != null)
                return drumSound.getAsPitch();
        }

        Memory memory = environment.getMemory(qualifier);
        if (memory == null) return null;
        return memory.get(name);
    }
}
