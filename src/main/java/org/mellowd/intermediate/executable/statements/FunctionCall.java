package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.functions.Argument;
import org.mellowd.intermediate.functions.FunctionBank;
import org.mellowd.intermediate.functions.FunctionResolutionException;
import org.mellowd.parser.ExecutionEnvironment;
import org.mellowd.parser.MellowD;

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
            throw sourceLink.toCompilationException(new FunctionResolutionException(options, name, environment, args));
        } else {
            options[0].getPreferredVariant(environment.isPercussion()).evaluate(global, environment, output, shouldReturn, args);
        }
    }
}
