package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.functions.Argument;
import cas.cs4tb3.mellowd.intermediate.functions.FunctionBank;
import cas.cs4tb3.mellowd.intermediate.functions.FunctionResolutionException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.parser.MellowD;

public class FunctionCall implements Statement {
    private final SourceLink sourceLink;
    private final MellowD global;
    private final FunctionBank.PercussionPair[] options;
    private final String name;
    private final Argument<?>[] args;
    private final boolean shouldReturn;

    public FunctionCall(SourceLink sourceLink, MellowD global, FunctionBank.PercussionPair[] options, String name, boolean shouldReturn, Argument<?>... args) {
        this.sourceLink = sourceLink;
        this.global = global;
        this.options = options;
        this.name = name;
        this.args = args;
        this.shouldReturn = shouldReturn;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        if (options.length != 1) {
            //Too many functions, can't resolve just one.
            sourceLink.throwCompilationException(new FunctionResolutionException(options, name, environment.isPercussion(), args));
        } else {
            options[0].getPreferredVariant(environment.isPercussion()).evaluate(global, environment, output, shouldReturn, args);
        }
    }
}
