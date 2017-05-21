package io.github.spencerpark.mellowd.intermediate.functions;

public class FunctionSignature {
    private final String name;
    private final Parameters parameters;

    public FunctionSignature(String name, Parameters parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public FunctionSignature(String name, Parameter... parameters) {
        this.name = name;
        this.parameters = new Parameters(parameters);
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return parameters.toString() + " => " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionSignature signature = (FunctionSignature) o;

        if (!name.equals(signature.name)) return false;
        return parameters.equals(signature.parameters);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }
}
