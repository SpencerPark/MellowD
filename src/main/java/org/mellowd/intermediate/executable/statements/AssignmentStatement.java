package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.PercussionToggledEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.expressions.Expression;
import org.mellowd.intermediate.variables.DelayedResolution;
import org.mellowd.intermediate.variables.Memory;

import java.util.Set;

public class AssignmentStatement implements Statement {
    private final QualifiedName name;
    private final Expression<?> value;
    private final boolean isFinal;
    private final boolean delayResolution;
    private final boolean percussionToggle;

    public AssignmentStatement(QualifiedName name, Expression<?> value, boolean isFinal, boolean delayResolution, boolean percussionToggle) {
        this.name = name;
        this.value = value;
        this.isFinal = isFinal;
        this.delayResolution = delayResolution;
        this.percussionToggle = percussionToggle;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.value.getFreeVariables();
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        ExecutionEnvironment env;
        if (percussionToggle) {
            env = new PercussionToggledEnvironment(environment);
        } else {
            env = environment;
        }

        Memory memory = environment.getMemory();
        Object toStore = this.delayResolution
                ? (DelayedResolution) mem -> value.evaluate(env)
                : value.evaluate(env);

        if (this.isFinal)
            memory.define(this.name, toStore);
        else
            memory.set(this.name, toStore);
    }
}
