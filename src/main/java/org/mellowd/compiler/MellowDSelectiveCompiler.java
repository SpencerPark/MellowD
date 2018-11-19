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
    public Void visitSong(MellowDParser.SongContext ctx) {
        // Import everything that is needed
        ctx.importStmt().forEach(super::visitImportStmt);

        ctx.assignStmt().stream()
                .filter(assignCtx -> this.includedNames == null || this.includedNames.contains(super.visitName(assignCtx.name())))
                .map(stmt -> {
                    if (this.as != null)
                        stmt.id = this.as.qualify(visitName(stmt.name()));
                    else
                        stmt.id = this.from.qualify(visitName(stmt.name()));

                    return super.visitAssignStmt(stmt, true);
                })
                .forEach(s -> s.execute(super.mellowD, NullOutput.getInstance()));

        return null;
    }
}
