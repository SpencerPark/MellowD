package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.PercussionToggledEnvironment;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.functions.Parameters;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.SymbolTable;

import java.util.LinkedHashSet;
import java.util.Set;

// Default parameter values are evaluated at declaration time.
public class Abstraction implements Expression<Closure> {
    private final Parameters parameters;
    private final boolean percussion;
    private final Statement body;

    public Abstraction(Parameters parameters, boolean percussion, Statement body) {
        this.parameters = parameters;
        this.percussion = percussion;
        this.body = body;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public boolean isPercussion() {
        return percussion;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> freeVars = new LinkedHashSet<>(this.getBody().getFreeVariables());

        // Remove the free variables that are captured by the parameters.
        freeVars.removeIf(name ->
                name.isUnqualified() && this.getParameters().getParam(name.getName()) != null);

        return freeVars;
    }

    @Override
    public Closure evaluate(ExecutionEnvironment environment) {
        Set<QualifiedName> freeVars = this.getFreeVariables();

        Memory enclosing = environment.getMemory();
        Memory captured = new SymbolTable();

        freeVars.forEach(name -> {
            Object value = enclosing.get(name);
            captured.set(name, value);
        });

        // Evaluate the default parameters and store their values in the closure scope.
        ExecutionEnvironment paramEvalEnv;
        if (this.isPercussion() != environment.isPercussion())
            paramEvalEnv = new PercussionToggledEnvironment(environment);
        else
            paramEvalEnv = environment;
        this.parameters.forEach(param -> {
            if (param.hasDefualtValue()) {
                Object defaultValue = param.getDefaultValue().evaluate(paramEvalEnv);
                param.checkIsAssignable(defaultValue);
                captured.set(param.getNameAsQualified(), defaultValue);
            }
        });

        return new Closure(captured, this);
    }
}
