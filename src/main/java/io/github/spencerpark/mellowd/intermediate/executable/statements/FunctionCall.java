package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.intermediate.executable.SourceLink;
import io.github.spencerpark.mellowd.intermediate.functions.Argument;
import io.github.spencerpark.mellowd.intermediate.functions.FunctionBank;
import io.github.spencerpark.mellowd.intermediate.functions.FunctionResolutionException;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;
import io.github.spencerpark.mellowd.parser.MellowD;

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
            throw sourceLink.toCompilationException(new FunctionResolutionException(options, name, environment.isPercussion(), args));
        } else {
            options[0].getPreferredVariant(environment.isPercussion()).evaluate(global, environment, output, shouldReturn, args);
        }
    }
}
