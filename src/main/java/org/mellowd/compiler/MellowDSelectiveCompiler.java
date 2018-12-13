package org.mellowd.compiler;

import org.mellowd.intermediate.NullOutput;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.Qualifier;

import java.util.Set;

public class MellowDSelectiveCompiler extends MellowDCompiler {
    private final Qualifier from;
    private final Qualifier as;
    private final Set<QualifiedName> includedNames;

    /**
     * Imported names are qualified with {@code as} if given, otherwise names are qualified with
     * {@code from} (their module name).
     *
     * @param mellowD
     * @param from
     * @param as
     * @param includedNames
     */
    public MellowDSelectiveCompiler(MellowD mellowD, Qualifier from, Qualifier as, Set<QualifiedName> includedNames) {
        super(mellowD);
        this.from = from;
        this.as = as;
        this.includedNames = includedNames;
    }

    @Override
    public Void visitTopLevelStmt(MellowDParser.TopLevelStmtContext ctx) {
        MellowDParser.AssignStmtContext assignStmt = ctx.assignStmt();
        if (assignStmt != null) {
            QualifiedName name = super.visitName(assignStmt.name());
            if (this.includedNames == null || this.includedNames.contains(name)) {
                if (this.as != null)
                    assignStmt.id = this.as.qualify(name);
                else
                    assignStmt.id = this.from.qualify(name);

                visitAssignStmt(assignStmt, true).execute(super.mellowD, NullOutput.getInstance());
            }

            return null;
        }

        MellowDParser.DoStmtContext doStmt = ctx.doStmt();
        if (doStmt != null) {
            visitDoStmt(doStmt).execute(this.mellowD, NullOutput.getInstance());
            return null;
        }

        return null;
    }
}
