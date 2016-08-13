package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;
import cas.cs4tb3.mellowd.intermediate.variables.DelayedResolution;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.regex.Pattern;

public class AssignmentStatement implements Statement {
    private static final Pattern SPLITTER = Pattern.compile("\\.");

    private final String[] qualifier;
    private final String name;
    private final Expression<?> value;
    private final boolean isField;
    private final boolean percussionToggle;

    public AssignmentStatement(String fullyQualifiedName, Expression<?> value, boolean isField, boolean percussionToggle) {
        String[] split = SPLITTER.split(fullyQualifiedName);
        this.qualifier = new String[split.length - 1];
        System.arraycopy(split, 0, this.qualifier, 0, this.qualifier.length);
        this.name = split[split.length - 1];
        this.value = value;
        this.isField = isField;
        this.percussionToggle = percussionToggle;
    }

    public AssignmentStatement(String fullyQualifiedName, Expression<?> value) {
        this(fullyQualifiedName, value, false, false);
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
        if (isField) {
            //These closure assignments will build the value when it is called
            //so that all definitions first get a chance to be assigned allowing
            //the assignment order to not matter
            memory.set(name, (DelayedResolution) mem -> value.evaluate(env));
        } else {
            memory.set(name, value.evaluate(env));
        }
    }
}
