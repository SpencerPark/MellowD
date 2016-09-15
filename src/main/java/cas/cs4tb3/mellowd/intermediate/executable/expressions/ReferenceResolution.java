package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.midi.GeneralMidiPercussion;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

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
