package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.midi.GeneralMidiPercussion;

import java.util.Collections;
import java.util.Set;

public class ReferenceResolution implements Expression<Object> {
    private final QualifiedName name;

    public ReferenceResolution(QualifiedName name) {
        this.name = name;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return Collections.singleton(this.name);
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        if (environment.isPercussion() && this.name.isUnqualified()) {
            GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup(this.name.getName());
            if (drumSound != null)
                return drumSound.getAsPitch();
        }

        Memory memory = environment.getMemory();
        return memory.get(this.name);
    }
}
