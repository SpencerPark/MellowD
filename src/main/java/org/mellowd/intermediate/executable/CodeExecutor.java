package org.mellowd.intermediate.executable;

import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.compiler.ExecutionEnvironment;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeExecutor extends Thread {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    private final ExecutionEnvironment environment;
    private final Output output;
    private final List<? extends Statement> code;
    private volatile Exception problem = null;

    public CodeExecutor(String name, ExecutionEnvironment environment, Output output, List<? extends Statement> code) {
        super("CodeExecutor-" + name + "-" + THREAD_ID.getAndIncrement());
        this.environment = environment;
        this.output = output;
        this.code = code;
    }

    @Override
    public void run() {
        try {
            for (Statement s : code) {
                s.execute(environment, output);
            }

            output.close();
        } catch (Exception e) {
            this.problem = e;
        }
    }

    public boolean errorWhileExecuting() {
        return this.problem != null;
    }

    public Exception getExecutionError() {
        return this.problem;
    }
}
