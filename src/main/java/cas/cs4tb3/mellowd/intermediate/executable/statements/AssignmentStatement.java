package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;
import cas.cs4tb3.mellowd.intermediate.variables.DelayedResolution;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class AssignmentStatement implements Statement {
    private final String[] qualifier;
    private final String name;
    private final Expression<?> value;
    private final boolean isFinal;
    private final boolean delayResolution;
    private final boolean percussionToggle;

    public AssignmentStatement(String[] qualifier, String name, Expression<?> value, boolean isFinal, boolean delayResolution, boolean percussionToggle) {
        this.qualifier = qualifier;
        this.name = name;
        this.value = value;
        this.isFinal = isFinal;
        this.delayResolution = delayResolution;
        this.percussionToggle = percussionToggle;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        ExecutionEnvironment env;
        if (percussionToggle) {
            env = new PercussionToggledEnvironment(environment);
        } else {
            env = environment;
        }

        Memory memory = environment.getMemory(qualifier);
        Object toStore = this.delayResolution
                ? (DelayedResolution) mem -> value.evaluate(env)
                : value.evaluate(env);

        if (this.isFinal)
            memory.define(this.name, toStore);
        else
            memory.set(this.name, toStore);
    }
}
