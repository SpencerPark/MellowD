package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.midi.GeneralMidiPercussion;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ReferenceResolution implements Expression<Object> {
    private static final Pattern SPLITTER = Pattern.compile("\\.");

    private final String[] qualifier;
    private final String name;

    public ReferenceResolution(String fullyQualifiedName) {
        String[] split = SPLITTER.split(fullyQualifiedName);
        this.qualifier = new String[split.length - 1];
        System.arraycopy(split, 0, this.qualifier, 0, this.qualifier.length);
        this.name = split[split.length - 1];
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        if (environment.isPercussion() && qualifier.length == 0) {
            GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup(name);
            if (drumSound != null)
                return drumSound.getAsPitch();
        }

        Memory memory = environment.getMemory(qualifier);
        return memory.get(name);
    }
}
