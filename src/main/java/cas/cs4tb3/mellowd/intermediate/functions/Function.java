package cas.cs4tb3.mellowd.intermediate.functions;

public abstract class Function<R> {
    private final String name;
    private final Parameters parameters;

    public Function(String name, Parameters parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Function(String name, Parameter... parameters) {
        this.name = name;
        this.parameters = new Parameters(parameters);
    }

    public abstract R evaluate(Arguments arguments);
}
