package org.mellowd.intermediate.functions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.SymbolTable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public class Parameters implements Iterable<Parameter<?>> {
    private final Parameter<?>[] params;
    private final int minSize;

    public Parameters(Parameter<?>... params) {
        this.params = params;
        int minSize = params.length;
        for (int i = params.length - 1; i >= 0; i--)
            if (params[i].isOptional()) minSize--;
            else break;
        this.minSize = minSize;
    }

    public int size() {
        return params.length;
    }

    // Optional parameters at the end may be omitted
    public int minSize() {
        return minSize;
    }

    public Parameter<?> getParam(int i) {
        if (i < 0 || i >= size())
            return null;

        return params[i];
    }

    public Parameter<?> getParam(String name) {
        for (Parameter<?> parameter : this.params) {
            if (parameter.getName().equals(name))
                return parameter;
        }

        return null;
    }

    public Memory prepareCall(ExecutionEnvironment callerEnv, Memory parentScope, Argument<?>... args) {
        // If the braces are empty then treat the first arg as not present.
        boolean isEmptyBraces = args.length == 0 || (args.length == 1 && args[0].isEmpty());
        if (isEmptyBraces && params.length == 0)
            return new SymbolTable(parentScope);

        if (args.length < minSize)
            throw new FunctionInvocationException(String.format("Not enough args. %d given but %d required.", args.length, minSize));
        if (args.length > params.length)
            throw new FunctionInvocationException(String.format("Too many args. %d given but at most %d expected.", args.length, params.length));

        // Create a block of memory to put the arguments into
        Memory memory = new SymbolTable(parentScope);

        // Put argument values into the scope.
        for (int i = 0; i < args.length; i++) {
            // Lookup the parameter that the argument is specifying.
            Argument<?> arg = args[i];
            Parameter<?> parameter = arg.isNamed() ? this.getParam(arg.getName()) : params[i];
            if (parameter == null)
                throw new FunctionInvocationException(String.format("No parameters are named \"%s\".", arg.getName()));

            // Evaluate the argument.
            Object value = arg.isDeclaredNull() ? null : arg.getValue().evaluate(callerEnv);

            if (value == null) {
                if (!parameter.isOptional()) {
                    // An argument is missing
                    throw new FunctionInvocationException(String.format("Parameter '%s' is not optional and was not given.", parameter.toString()));
                } else {
                    // The argument is allowed to be null, leave that cell empty
                    continue;
                }
            }

            // Check that a typed parameter is given the correct value.
            parameter.checkIsAssignable(value);

            // Put the variable in the scope
            memory.set(parameter.getNameAsQualified(), value);
        }

        return memory;
    }

    @Override
    public Iterator<Parameter<?>> iterator() {
        return Arrays.asList(this.params).iterator();
    }

    @Override
    public String toString() {
        StringJoiner str = new StringJoiner(", ", "{ ", " }");
        for (Parameter<?> p : params)
            str.add(p.toString());
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameters that = (Parameters) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(params, that.params);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(params);
    }
}
