package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.executable.statements.Statement;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.parser.MellowD;

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
