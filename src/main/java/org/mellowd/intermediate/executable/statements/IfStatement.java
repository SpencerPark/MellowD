package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.expressions.Expression;

import java.util.*;

public class IfStatement implements Statement {
    private static class Branch {
        private final Expression<Boolean> condition;
        private final Statement statement;

        public Branch(Expression<Boolean> condition, Statement statement) {
            this.condition = condition;
            this.statement = statement;
        }

        public Expression<Boolean> getCondition() {
            return condition;
        }

        public Statement getStatement() {
            return statement;
        }
    }

    public static class Builder {
        private final List<Branch> branches;
        private Statement elseBranch;

        public Builder(Expression<Boolean> condition, Statement statement) {
            this.branches = new LinkedList<>();
            this.branches.add(new Branch(condition, statement));
            this.elseBranch = null;
        }

        public Builder addElseIf(Expression<Boolean> condition, Statement statement) {
            this.branches.add(new Branch(condition, statement));
            return this;
        }

        public Builder setElse(Statement statement) {
            this.elseBranch = statement;
            return this;
        }

        public IfStatement build() {
            return new IfStatement(branches, elseBranch);
        }
    }

    private final Expression<Boolean>[] conditions;
    private final Statement[] branches;
    private final Statement elseStatement;

    public IfStatement(List<Branch> branches, Statement elseStatement) {
        if (branches.isEmpty())
            throw new IllegalArgumentException("If-Statement is missing an if branch");

        this.conditions = new Expression[branches.size()];
        this.branches = new Statement[branches.size()];
        int i = 0;
        for (Branch branch : branches) {
            this.conditions[i] = branch.getCondition();
            this.branches[i] = branch.getStatement();
            i++;
        }

        this.elseStatement = elseStatement != null ? elseStatement : EmptyStatement.getInstance();
    }

    public boolean hasElseBranch() {
        return this.elseStatement != EmptyStatement.getInstance();
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> names = new LinkedHashSet<>();
        Arrays.asList(this.conditions).forEach(e -> names.addAll(e.getFreeVariables()));
        Arrays.asList(this.branches).forEach(s -> names.addAll(s.getFreeVariables()));
        names.addAll(this.elseStatement.getFreeVariables());
        return names;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (int i = 0; i < conditions.length; i++) {
            Expression<Boolean> condition = this.conditions[i];
            if (condition.evaluate(environment)) {
                this.branches[i].execute(environment, output);
                return;
            }
        }

        if (hasElseBranch())
            elseStatement.execute(environment, output);
    }
}
