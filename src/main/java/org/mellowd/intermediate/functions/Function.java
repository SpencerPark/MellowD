package org.mellowd.intermediate.functions;

import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.compiler.MellowD;

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
