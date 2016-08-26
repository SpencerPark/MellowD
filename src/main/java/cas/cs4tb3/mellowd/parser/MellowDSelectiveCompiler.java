package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.intermediate.functions.FunctionBank;

import java.util.Set;

public class MellowDSelectiveCompiler extends MellowDCompiler {
    private final String[] qualifier;
    private final String[] givenName;
    private final Set<String> functionNames;
    private final boolean importAll;

    public MellowDSelectiveCompiler(MellowD mellowD, String[] qualifier, String[] givenName, Set<String> functionNames) {
        super(mellowD);
        this.qualifier = qualifier;
        this.givenName = givenName;
        this.functionNames = functionNames;
        this.importAll = functionNames.isEmpty();
    }

    @Override
    public Void visitSong(MellowDParser.SongContext ctx) {
        //Import everything that is needed
        ctx.importStatement().forEach(super::visitImportStatement);

        //Add the functions
        FunctionBank fullyQualifiedBank = super.mellowD.getOrCreateFunctionBank(qualifier);
        FunctionBank givenNameBank = givenName == null ? null : super.mellowD.getOrCreateFunctionBank(givenName);

        ctx.functionDefinition().stream()
                .filter(funCtx -> importAll || functionNames.contains(funCtx.IDENTIFIER().getText()))
                .map(super::visitFunctionDefinition)
                .forEach(function -> {
                    fullyQualifiedBank.addFunction(function);
                    if (givenNameBank != null)
                        givenNameBank.addFunction(function);
                });

        return null;
    }
}
