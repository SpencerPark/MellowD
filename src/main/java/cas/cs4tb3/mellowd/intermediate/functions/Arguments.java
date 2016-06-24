package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.variables.Memory;

public class Arguments {
    private final Memory paramValues;

    public Arguments(Memory paramValues) {
        this.paramValues = paramValues;
    }

    public <T> T get(Parameter<T> parameter) {
        return parameter.getReference().dereference(paramValues);
    }
}
