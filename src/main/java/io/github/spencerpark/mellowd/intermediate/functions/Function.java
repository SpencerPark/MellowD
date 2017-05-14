package io.github.spencerpark.mellowd.intermediate.functions;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.intermediate.executable.statements.Statement;
import io.github.spencerpark.mellowd.intermediate.variables.Memory;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;
import io.github.spencerpark.mellowd.parser.MellowD;

public class Function {
    private final FunctionSignature signature;
    private final boolean percussion;
    private final Statement body;

    public Function(FunctionSignature signature, boolean percussion, Statement body) {
        this.signature = signature;
        this.percussion = percussion;
        this.body = body;
    }

    public boolean isPercussion() {
        return percussion;
    }

    public void evaluate(MellowD mellowD, ExecutionEnvironment environment, Output output, boolean shouldReturn, Argument<?>... args) {
        Memory argument = signature.getParameters().prepareCall(environment, args);
        FunctionExecutionEnvironment functionEnv = new FunctionExecutionEnvironment(mellowD, environment, argument, signature.getName(), shouldReturn, percussion);

        body.execute(functionEnv, output);
    }

    public FunctionSignature getSignature() {
        return signature;
    }
}
