package org.mellowd.intermediate;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.executable.expressions.Abstraction;
import org.mellowd.intermediate.functions.Argument;
import org.mellowd.intermediate.variables.Memory;

public class Closure {
    public static final QualifiedName RETURN_NAME = QualifiedName.ofUnqualified("return");

    private final Memory captured;
    private final Abstraction abstraction;

    public Closure(Memory captured, Abstraction abstraction) {
        this.captured = captured;
        this.abstraction = abstraction;
    }

    public Object call(ExecutionEnvironment callingEnv, Output out, Argument<?>... args) {
        Memory bodyScope = this.abstraction.getParameters().prepareCall(callingEnv, this.captured, args);

        ExecutionEnvironment bodyEnv = new ClosureEvaluationEnvironment(callingEnv, bodyScope, abstraction.isPercussion());
        abstraction.getBody().execute(bodyEnv, out);
        return bodyScope.get(RETURN_NAME);
    }
}
