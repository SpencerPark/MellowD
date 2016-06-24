package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;

public class Parameters {
    private final Parameter<?>[] params;

    public Parameters(Parameter<?>... params) {
        this.params = params;
    }

    public int size() {
        return params.length;
    }

    public Parameter<?> getParam(int i) {
        if (i < 0 || i >= size())
            throw new java.lang.IllegalArgumentException("Parameter at "+i+" does not exist.");

        return params[i];
    }

    public Arguments constructArguments(Object... values) {
        if (values.length != params.length)
            throw new Error(); //TODO implement an exception for these cases

        Memory argumentValues = new SymbolTable();
        for (int i = 0; i < values.length; i++) {
            Parameter<?> parameter = params[i];
            Object argumentValue = values[i];

            if (argumentValue == null) {
                if (!parameter.isOptional()) {
                    //An argument is missing
                    throw new Error(); //TODO implement an exception
                } else {
                    continue;
                }
            }

            if (!parameter.getReference().getType().isAssignableFrom(argumentValue.getClass()))
                throw new Error(); //TODO incorrect type exception

            //Put the variable in the scope
            argumentValues.set(parameter.getReference().getIdentifier(), argumentValue);
        }

        return new Arguments(argumentValues);
    }


}
