package org.mellowd.compiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.mellowd.intermediate.*;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.executable.expressions.*;
import org.mellowd.intermediate.executable.statements.*;
import org.mellowd.intermediate.functions.Argument;
import org.mellowd.intermediate.functions.Parameter;
import org.mellowd.intermediate.functions.Parameters;
import org.mellowd.intermediate.functions.operations.Articulatable;
import org.mellowd.intermediate.functions.operations.Comparable;
import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.intermediate.functions.operations.Slurrable;
import org.mellowd.intermediate.variables.AlreadyDefinedException;
import org.mellowd.intermediate.variables.UndefinedReferenceException;
import org.mellowd.midi.GeneralMidiInstrument;
import org.mellowd.midi.Knob;
import org.mellowd.midi.MIDIControl;
import org.mellowd.midi.Pedal;
import org.mellowd.primitives.*;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MellowDCompiler extends MellowDParserBaseVisitor {
    private static String getText(ParserRuleContext rule) {
        Interval interval = Interval.of(rule.start.getStartIndex(), rule.stop.getStopIndex());
        return rule.start.getInputStream().getText(interval);
    }

    private static String getText(TerminalNode node) {
        return node.getText();
    }

    private static String getText(Token token) {
        return token.getText();
    }

    private static final Constant<Boolean> TRUE = new Constant<>(true);
    private static final Constant<Boolean> FALSE = new Constant<>(false);
    private static final Constant<Articulated> REST = new Constant<>(new ArticulatedPitch(Pitch.REST, Articulation.NONE));

    private static final Constant<Articulation> ARTICULATION_NONE = new Constant<>(Articulation.NONE);
    private static final Constant<Articulation> ARTICULATION_ACCENT = new Constant<>(Articulation.ACCENT);
    private static final Constant<Articulation> ARTICULATION_GLISCANDO = new Constant<>(Articulation.GLISCANDO);
    private static final Constant<Articulation> ARTICULATION_MARCATO = new Constant<>(Articulation.MARCATO);
    private static final Constant<Articulation> ARTICULATION_STACCATISSIMO = new Constant<>(Articulation.STACCATISSIMO);
    private static final Constant<Articulation> ARTICULATION_STACCATO = new Constant<>(Articulation.STACCATO);
    private static final Constant<Articulation> ARTICULATION_TENUTO = new Constant<>(Articulation.TENUTO);

    protected final MellowD mellowD;
    protected final ConcatenationDelegate<Melody> melodyConcatenationDelegate = new ConcatenationDelegate<>();
    protected final ConcatenationDelegate<Chord> chordConcatenationDelegate = new ConcatenationDelegate<>();
    protected final ConcatenationDelegate<Rhythm> rhythmConcatenationDelegate = new ConcatenationDelegate<>();

    protected final Class<?>[] melodyParamTypes;
    protected final Class<?>[] chordParamTypes;
    protected final Class<Slurrable> rhythmParamType;

    public MellowDCompiler(MellowD mellowD) {
        this.mellowD = mellowD;

        this.melodyConcatenationDelegate.addDelegate(Melody.class, Melody::append);
        this.melodyConcatenationDelegate.addDelegate(Articulated.class, Melody::append);
        this.melodyConcatenationDelegate.addDelegate(Pitch.class, Melody::append);
        this.melodyConcatenationDelegate.addDelegate(Chord.class, Melody::append);
        this.melodyParamTypes = new Class[]{ Melody.class, Articulated.class, Pitch.class, Chord.class };

        this.chordConcatenationDelegate.addDelegate(Pitch.class, Chord::append);
        this.chordConcatenationDelegate.addDelegate(Chord.class, Chord::append);
        this.chordConcatenationDelegate.addDelegate(Articulated.class, Chord::append);
        this.chordParamTypes = new Class[]{ Pitch.class, Chord.class, Articulated.class };

        this.rhythmConcatenationDelegate.addDelegate(Beat.class, Rhythm::append);
        this.rhythmConcatenationDelegate.addDelegate(Rhythm.class, Rhythm::append);
        this.rhythmParamType = Slurrable.class;
    }

    @Override
    public QualifiedName visitName(MellowDParser.NameContext ctx) {
        List<TerminalNode> fullyQualifiedID = ctx.IDENTIFIER();

        String[] qualifier = new String[fullyQualifiedID.size() - 1];
        for (int i = 0; i < qualifier.length; i++)
            qualifier[i] = getText(fullyQualifiedID.get(i));

        String name = getText(fullyQualifiedID.get(fullyQualifiedID.size() - 1));

        return QualifiedName.of(qualifier, name);
    }

    public Qualifier compileQualifier(MellowDParser.NameContext ctx) {
        return Qualifier.of(ctx.IDENTIFIER().stream().map(MellowDCompiler::getText).toArray(String[]::new));
    }

    private <T> Expression<T> lookupName(MellowDParser.NameContext ctx, Class<T> desiredType) {
        QualifiedName identifier = visitName(ctx);
        return new RuntimeTypeCheck<>(desiredType, new ReferenceResolution(identifier), new SourceLink(ctx));
    }

    private Expression<Object> lookupName(MellowDParser.NameContext ctx) {
        QualifiedName identifier = visitName(ctx);
        return new ReferenceResolution(identifier);
    }

    private Expression<?> compileIndexedNameOrChordLiteral(MellowDParser.NameContext name, TerminalNode chordId, MellowDParser.IndexContext index) {
        Expression<?> valueExpr;
        QualifiedName refName;
        SourceLink link;

        if (name != null) {
            valueExpr = lookupName(name);
            refName = visitName(name);
            link = new SourceLink(name);
        } else {
            valueExpr = new Constant<>(Chord.resolve(getText(chordId)));
            refName = QualifiedName.ofUnqualified(getText(chordId));
            link = new SourceLink(chordId);
        }

        valueExpr = new RuntimeNullCheck<>(refName, valueExpr, link);
        return possiblyIndex(link, valueExpr, index);
    }

    @Override
    public Integer visitDirectedNumber(MellowDParser.DirectedNumberContext ctx) {
        return ctx.amt;
    }

    @Override
    public Integer visitNumber(MellowDParser.NumberContext ctx) {
        return ctx.amt;
    }

    @Override
    public Expression<Integer> visitNumberOrId(MellowDParser.NumberOrIdContext ctx) {
        MellowDParser.NameContext idCtx = ctx.name();
        if (idCtx != null)
            return new RuntimeNullCheck<>(visitName(idCtx), lookupName(idCtx, Integer.class), new SourceLink(idCtx));
        return new Constant<>(visitNumber(ctx.number()));
    }

    @Override
    public Pair<Expression<Integer>, Expression<Integer>> visitRange(MellowDParser.RangeContext ctx) {
        Expression<Integer> lower = visitNumberOrId(ctx.lower);
        Expression<Integer> upper = ctx.upper != null ? visitNumberOrId(ctx.upper) : null;

        return new Pair<>(lower, upper);
    }

    @Override
    public List<Pair<Expression<Integer>, Expression<Integer>>> visitIndex(MellowDParser.IndexContext ctx) {
        List<Pair<Expression<Integer>, Expression<Integer>>> indexes = new LinkedList<>();

        ctx.range().forEach(rangeCtx -> indexes.add(visitRange(rangeCtx)));

        return indexes;
    }

    private Expression<?> possiblyIndex(SourceLink link, Expression<?> expr, MellowDParser.IndexContext indexContext) {
        if (indexContext == null) return expr;

        for (Pair<Expression<Integer>, Expression<Integer>> range : visitIndex(indexContext)) {
            Expression<Indexable<?, ?>> indexable = new RuntimeIndexingSupportCheck(expr, link);
            expr = new IndexExpression(indexable, range.a, range.b);
        }

        return expr;
    }

    @Override
    public Constant<Articulation> visitArticulation(MellowDParser.ArticulationContext ctx) {
        if (ctx == null || ctx.art == null) return ARTICULATION_NONE;
        switch (ctx.art) {
            case NONE:
                return ARTICULATION_NONE;
            case STACCATO:
                return ARTICULATION_STACCATO;
            case STACCATISSIMO:
                return ARTICULATION_STACCATISSIMO;
            case MARCATO:
                return ARTICULATION_MARCATO;
            case ACCENT:
                return ARTICULATION_ACCENT;
            case TENUTO:
                return ARTICULATION_TENUTO;
            case GLISCANDO:
                return ARTICULATION_GLISCANDO;
            default:
                throw new Error("New articulation created without updating visitArticulation()");
        }
    }

    @Override
    public Pitch visitPitchRoot(MellowDParser.PitchRootContext ctx) {
        return ctx.pitch;
    }

    @Override
    public Pitch visitNote(MellowDParser.NoteContext ctx) {
        Pitch root = visitPitchRoot(ctx.pitchRoot());
        if (ctx.SHARP() != null) {
            root = root.sharp();
        } else if (ctx.FLAT() != null) {
            root = root.flat();
        }

        if (ctx.octaveShift != null)
            root = root.shiftOctave(visitDirectedNumber(ctx.octaveShift));

        return root;
    }

    @Override
    public Expression<Chord> visitChord(MellowDParser.ChordContext ctx) {
        MellowDParser.CallContext call = ctx.call();
        if (call != null)
            return new RuntimeTypeCheck<>(Chord.class, visitCall(call), new SourceLink(ctx));

        // Otherwise build from the params
        Concatenation<Chord> result = new Concatenation<>(Chord::new, this.chordConcatenationDelegate);
        ctx.params.forEach(paramCtx -> {
            Expression<?> paramExpr = visitChordParam(paramCtx);
            result.addArgument(paramExpr);
        });

        return result;
    }

    @Override
    public Expression<?> visitChordParam(MellowDParser.ChordParamContext ctx) {
        MellowDParser.NoteContext note = ctx.note();
        if (note != null)
            return new Constant<>(visitNote(note));

        MellowDParser.ChordContext chord = ctx.chord();
        if (chord != null)
            return visitChord(chord);

        Expression<?> valueExpr = compileIndexedNameOrChordLiteral(ctx.name(), ctx.CHORD_IDENTIFIER(), ctx.index());
        return new RuntimeUnionTypeCheck(valueExpr, this.chordParamTypes, new SourceLink(ctx));
    }

    @Override
    public Expression<Melody> visitMelody(MellowDParser.MelodyContext ctx) {
        MellowDParser.CallContext call = ctx.call();
        if (call != null)
            return new RuntimeTypeCheck<>(Melody.class, visitCall(call), new SourceLink(ctx));

        // Otherwise build from the params
        Concatenation<Melody> result = new Concatenation<>(Melody::new, this.melodyConcatenationDelegate);
        ctx.params.forEach(paramCtx -> {
            Expression<?> paramExpr = visitMelodyParam(paramCtx);
            result.addArgument(paramExpr);
        });
        return result;
    }

    @Override
    public Expression<?> visitMelodyParam(MellowDParser.MelodyParamContext ctx) {
        if (ctx.STAR() != null)
            return REST;

        MellowDParser.MelodyContext melody = ctx.melody();
        if (melody != null)
            return visitMelody(melody);

        MellowDParser.ArticulationContext articulation = ctx.articulation();
        Expression<Articulation> articulationExpr = visitArticulation(articulation);

        MellowDParser.NoteContext note = ctx.note();
        if (note != null)
            return new Articulate(new Constant<>(visitNote(note)), articulationExpr);

        Expression<?> paramExpr;
        MellowDParser.ChordContext chord = ctx.chord();
        if (chord != null) {
            paramExpr = visitChord(chord);
        } else {
            paramExpr = compileIndexedNameOrChordLiteral(ctx.name(), ctx.CHORD_IDENTIFIER(), ctx.index());
        }

        SourceLink link = new SourceLink(ctx);
        if (articulation != null) {
            Expression<Articulatable> resolvedArticulatable = new RuntimeTypeCheck<>(Articulatable.class, paramExpr, link);
            resolvedArticulatable = new RuntimeNullCheck<>(QualifiedName.ofUnqualified(getText(ctx)), resolvedArticulatable, link);
            return new Articulate(resolvedArticulatable, articulationExpr);
        }

        return new RuntimeUnionTypeCheck(paramExpr, this.melodyParamTypes, link);
    }

    @Override
    public Beat visitBeat(MellowDParser.BeatContext ctx) {
        return ctx.b;
    }

    private Expression<Rhythm> buildRhythm(List<MellowDParser.RhythmParamContext> params) {
        Concatenation<Rhythm> result = new Concatenation<>(Rhythm::new, this.rhythmConcatenationDelegate);
        params.forEach(paramCtx -> {
            Expression<?> paramExpr = visitRhythmParam(paramCtx);
            result.addArgument(paramExpr);
        });
        return result;
    }

    @Override
    public Expression<Rhythm> visitRhythm(MellowDParser.RhythmContext ctx) {
        MellowDParser.CallContext call = ctx.call();
        if (call != null)
            return new RuntimeTypeCheck<>(Rhythm.class, visitCall(call), new SourceLink(ctx));

        // Otherwise build from the params
        return buildRhythm(ctx.rhythmParam());
    }

    @Override
    public Expression<Rhythm> visitSlurredRhythm(MellowDParser.SlurredRhythmContext ctx) {
        return new RuntimeSlur<>(buildRhythm(ctx.rhythmParam()));
    }

    @Override
    public Expression<Rhythm> visitTuplet(MellowDParser.TupletContext ctx) {
        Rhythm rhythm = new Rhythm();
        int tupletNumerator = Integer.parseInt(getText(ctx.num));
        int tupletDenominator = ctx.div != null ? Integer.parseInt(getText(ctx.div)) : tupletNumerator - 1;

        if (tupletNumerator <= 1) {
            throw new IllegalArgumentException("Cannot create tuplet with a numerator of " + tupletNumerator);
        } else if (tupletDenominator < 1) {
            throw new IllegalArgumentException("Cannot create a tuplet with a denominator of " + tupletDenominator);
        }

        if (ctx.singleDivision != null) {
            Beat comp = visitBeat(ctx.singleDivision);
            comp = comp.tuplet(tupletNumerator, tupletDenominator);
            for (int i = 0; i < tupletNumerator; i++)
                rhythm.append(comp);
        } else {
            ctx.complexDivision.forEach(complexCtx -> {
                Beat comp = visitBeat(complexCtx);
                comp = comp.tuplet(tupletNumerator, tupletDenominator);
                rhythm.append(comp);
            });
        }

        return new Constant<>(rhythm);
    }

    @Override
    public Expression<?> visitRhythmParam(MellowDParser.RhythmParamContext ctx) {
        MellowDParser.BeatContext beat = ctx.beat();
        if (beat != null)
            return new Constant<>(visitBeat(beat));

        MellowDParser.TupletContext tuplet = ctx.tuplet();
        if (tuplet != null)
            return visitTuplet(tuplet);

        MellowDParser.RhythmContext rhythm = ctx.rhythm();
        if (rhythm != null)
            return visitRhythm(rhythm);

        MellowDParser.SlurredRhythmContext slurredRhythm = ctx.slurredRhythm();
        if (slurredRhythm != null)
            return visitSlurredRhythm(slurredRhythm);

        MellowDParser.NameContext identifier = ctx.name();
        Expression<?> idExpr = lookupName(identifier);

        SourceLink link = new SourceLink(ctx);

        MellowDParser.IndexContext index = ctx.index();

        idExpr = new RuntimeNullCheck<>(visitName(identifier), idExpr, link);
        if (index != null) {
            //We want to make sure that it will actually be indexed to avoid a duplicate runtime null check
            idExpr = possiblyIndex(link, idExpr, index);
            idExpr = new RuntimeNullCheck<>(visitName(identifier), idExpr, link);
        }

        return new RuntimeTypeCheck<>(Slurrable.class, idExpr, link);
    }

    @Override
    public Comparable.Operator visitComparisonOperator(MellowDParser.ComparisonOperatorContext ctx) {
        return ctx.op;
    }

    @Override
    public Expression<?> visitAtom(MellowDParser.AtomContext ctx) {
        if (ctx.KEYWORD_TRUE() != null)
            return TRUE;

        if (ctx.KEYWORD_FALSE() != null)
            return FALSE;

        TerminalNode STRING = ctx.STRING();
        if (STRING != null) {
            String raw = getText(STRING);
            return new Constant<>(raw.substring(1, raw.length() - 1));
        }

        MellowDParser.NoteContext note = ctx.note();
        if (note != null)
            return new Constant<>(visitNote(note));

        MellowDParser.BeatContext beat = ctx.beat();
        if (beat != null)
            return new Constant<>(visitBeat(beat));

        MellowDParser.NumberContext number = ctx.number();
        if (number != null)
            return new Constant<>(visitNumber(number));

        MellowDParser.ChordContext chord = ctx.chord();
        if (chord != null)
            return visitChord(chord);

        MellowDParser.MelodyContext melody = ctx.melody();
        if (melody != null)
            return visitMelody(melody);

        MellowDParser.RhythmContext rhythm = ctx.rhythm();
        if (rhythm != null)
            return visitRhythm(rhythm);

        TerminalNode chordID = ctx.CHORD_IDENTIFIER();
        MellowDParser.NameContext name = ctx.name();
        if (chordID != null || name != null)
            return compileIndexedNameOrChordLiteral(name, chordID, ctx.index());

        MellowDParser.FuncDeclContext funcDecl = ctx.funcDecl();
        if (funcDecl != null)
            return visitFuncDecl(funcDecl);

        MellowDParser.ProcDeclContext procDecl = ctx.procDecl();
        if (procDecl != null)
            return visitProcDecl(procDecl);

        if (ctx.BRACE_OPEN() != null) {
            MellowDParser.CallContext call = ctx.call();
            if (call != null)
                return visitCall(call);
            return visitExpr(ctx.expr());
        }

        throw new AssertionError("visitAtom compiler not updated after atom changed");
    }

    @Override
    public Expression<?> visitExpr(MellowDParser.ExprContext ctx) {
        if (ctx.KEYWORD_NOT() != null)
            return new BooleanNotExpression(new BooleanEvaluationExpression(visitExpr(ctx.expr(0))));

        List<MellowDParser.ComparisonOperatorContext> operators = ctx.comparisonOperator();
        if (!operators.isEmpty()) {
            List<MellowDParser.AtomContext> operands = ctx.atom();

            List<Expression<Boolean>> comparisons = new ArrayList<>(operators.size());

            Comparable.Operator op;
            Expression<?> left, right;
            // a lt b lt c -> a lt b AND b lt c
            for (int i = 0; i < operators.size(); i++) {
                left = visitAtom(operands.get(i));
                op = visitComparisonOperator(operators.get(i));
                right = visitAtom(operands.get(i + 1));

                comparisons.add(new Comparison(left, op, right));
            }

            return new BooleanANDChain(comparisons);
        }

        if (ctx.KEYWORD_AND() != null)
            return new BooleanANDChain(ctx.expr().stream()
                    .map(this::visitExpr)
                    .map(BooleanEvaluationExpression::new)
                    .collect(Collectors.toList()));

        if (ctx.KEYWORD_OR() != null)
            return new BooleanORChain(ctx.expr().stream()
                    .map(this::visitExpr)
                    .map(BooleanEvaluationExpression::new)
                    .collect(Collectors.toList()));

        return visitAtom(ctx.atom(0));
    }

    @Override
    public Statement visitIfStmt(MellowDParser.IfStmtContext ctx) {
        List<MellowDParser.DoStmtContext> blocks = ctx.doStmt();
        List<MellowDParser.ExprContext> conditions = ctx.expr();

        // Initialize the ifStmt builder with the first condition and block
        Expression<Boolean> condition = new BooleanEvaluationExpression(visitExpr(conditions.get(0)));
        Statement block = visitDoStmt(blocks.get(0));
        IfStatement.Builder ifStmt = new IfStatement.Builder(condition, block);

        // Else If branches
        for (int branchIdx = 1; branchIdx < conditions.size(); branchIdx++) {
            condition = new BooleanEvaluationExpression(visitExpr(conditions.get(branchIdx)));
            block = visitDoStmt(blocks.get(branchIdx));

            ifStmt.addElseIf(condition, block);
        }

        if (conditions.size() < blocks.size()) {
            //Else branch, add last block
            ifStmt.setElse(visitDoStmt(blocks.get(blocks.size() - 1)));
        }

        return ifStmt.build();
    }


    public Statement visitAssignStmt(MellowDParser.AssignStmtContext ctx, boolean isField) {
        boolean percussionToggle = ctx.STAR() != null;
        boolean isConstant = ctx.KEYWORD_DEF() != null;

        if (ctx.id == null)
            ctx.id = visitName(ctx.name());

        Expression<?> valueExpr = visitExpr(ctx.expr());

        return new AssignmentStatement(ctx.id, valueExpr,
                isConstant, isField, percussionToggle);
    }

    @Override
    public Statement visitAssignStmt(MellowDParser.AssignStmtContext ctx) {
        return visitAssignStmt(ctx, true);
    }

    @Override
    public Statement visitDynamicChangeStmt(MellowDParser.DynamicChangeStmtContext ctx) {
        SourceLink link = new SourceLink(ctx);
        if (ctx.ARROWS_L() != null)
            return new ContextFreeStatement(link, new GradualDynamicChange(ctx.dynamic, true));
        else if (ctx.ARROWS_R() != null)
            return new ContextFreeStatement(link, new GradualDynamicChange(ctx.dynamic, false));
        else
            return new ContextFreeStatement(link, new DynamicChange(ctx.dynamic));
    }

    @Override
    public Statement visitPerformStmt(MellowDParser.PerformStmtContext ctx) {
        SourceLink link = new SourceLink(ctx);

        Expression<Melody> lhs;

        MellowDParser.MelodyContext melody = ctx.melody();
        if (melody != null) {
            lhs = visitMelody(melody);
        } else {
            lhs = lookupName(ctx.melodyRef, Melody.class);
            lhs = new RuntimeNullCheck<>(visitName(ctx.melodyRef), lhs, link);
        }

        Expression<Rhythm> rhs;
        MellowDParser.RhythmContext rhythm = ctx.rhythm();
        if (rhythm != null) {
            rhs = visitRhythm(rhythm);
        } else {
            rhs = lookupName(ctx.rhythmRef, Rhythm.class);
            rhs = new RuntimeNullCheck<>(visitName(ctx.rhythmRef), rhs, link);
        }

        return new PlayPhraseStatement(new PhraseConstruction(lhs, rhs));
    }

    @Override
    public Statement visitBlockConfigField(MellowDParser.BlockConfigFieldContext ctx) {
        Playable playable;
        switch (getText(ctx.IDENTIFIER()).toLowerCase()) {
            case "instrument":
                if (ctx.configVal instanceof Number)
                    playable = new InstrumentChange(((Number) ctx.configVal).intValue(), 0);
                else if (ctx.configVal instanceof String) {
                    GeneralMidiInstrument instrument = GeneralMidiInstrument.lookup((String) ctx.configVal);
                    if (instrument == null)
                        throw new CompilationException(ctx, new UndefinedReferenceException(QualifiedName.ofUnqualified((String) ctx.configVal)));
                    playable = new InstrumentChange(instrument);
                } else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot set an instrument to be " + ctx.configVal));
                break;
            case "octave":
                if (ctx.configVal instanceof Number)
                    playable = new OctaveShift(((Number) ctx.configVal).intValue());
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot shift the octave by " + ctx.configVal));
                break;
            case "soundbank":
                if (ctx.configVal instanceof Number)
                    playable = new SoundbankChange(((Number) ctx.configVal).intValue());
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot set sound bank to  " + ctx.configVal));
                break;
            case "transpose":
                if (ctx.configVal instanceof Number)
                    playable = new TransposeChange(((Number) ctx.configVal).intValue());
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot transpose by " + ctx.configVal));
                break;
            case "mute":
                if (ctx.configVal instanceof Boolean)
                    playable = ChannelMute.getInstance((Boolean) ctx.configVal);
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Mute is a switch and can be configured by 'true' or 'false' for muted or un-muted. Not '" + ctx.configVal + "'"));
                break;
            default:
                MIDIControl<?> controller = MIDIControl.getController(getText(ctx.IDENTIFIER()));
                if (controller == null)
                    throw new CompilationException(ctx.IDENTIFIER(), new IllegalArgumentException("Unknown configuration option '" + getText(ctx.IDENTIFIER()) + "'"));

                if (controller.getControllerType().equals(Knob.class)) {
                    if (ctx.configVal instanceof Number)
                        playable = new MIDIKnobChange((MIDIControl<Knob>) controller, ((Number) ctx.configVal).intValue());
                    else
                        throw new CompilationException(ctx, new IllegalArgumentException("Cannot twist the " + controller.getName() + " knob to " + ctx.configVal));
                    break;
                } else if (controller.getControllerType().equals(Pedal.class)) {
                    if (ctx.configVal instanceof Boolean)
                        playable = new MIDIPedalChange((MIDIControl<Pedal>) controller, (Boolean) ctx.configVal);
                    else
                        throw new CompilationException(ctx, new IllegalArgumentException("Cannot set the " + controller.getName() + " pedal to " + ctx.configVal));
                    break;
                } else {
                    throw new Error("New controller type. visitBlockConfiguration() in the compiler needs an update.");
                }
        }

        return new OnceStatement(new ContextFreeStatement(new SourceLink(ctx), playable));
    }

    @Override
    public Void visitBlockDeclStmt(MellowDParser.BlockDeclStmtContext ctx) {
        StatementList config = null;
        if (ctx.BRACE_OPEN() != null) {
            config = new StatementList();
            for (MellowDParser.BlockConfigFieldContext configCtx : ctx.blockConfigField()) {
                config.add(visitBlockConfigField(configCtx));
            }
        }

        for (TerminalNode id : ctx.IDENTIFIER()) {
            try {
                MellowDBlock block = this.mellowD.defineBlock(getText(id), ctx.KEYWORD_PERCUSSION() != null);
                if (config != null) block.appendStatement(config);
            } catch (AlreadyDefinedException e) {
                throw new CompilationException(id, e);
            }
        }
        return null;
    }

    @Override
    public Statement visitDoStmt(MellowDParser.DoStmtContext ctx) {
        MellowDParser.CallContext call = ctx.call();
        if (call != null) {
            MellowDParser.NameContext name = call.name();

            Expression<Closure> procedure = lookupName(name, Closure.class);
            procedure = new RuntimeNullCheck<>(visitName(name), procedure, new SourceLink(call));
            Argument<?>[] arguments = visitArgumentList(call.argumentList());

            return new PerformProcedureStatement(procedure, arguments);
            // TODO split into side effect free statements for functions and perform for procedures
        }

        MellowDParser.StmtListContext stmtList = ctx.stmtList();
        if (stmtList != null) {
            return visitStmtList(stmtList);
        }

        MellowDParser.StmtContext stmt = ctx.stmt();
        return visitStmt(stmt);
    }

    @Override
    public Statement visitOnceStmt(MellowDParser.OnceStmtContext ctx) {
        MellowDParser.StmtListContext stmtList = ctx.stmtList();
        if (stmtList != null)
            return new OnceStatement(visitStmtList(stmtList));

        Statement statement = visitStmt(ctx.stmt());
        return new OnceStatement(statement);
    }

    @Override
    public Statement visitStmt(MellowDParser.StmtContext ctx) {
        TerminalNode NUMBER = ctx.NUMBER();
        MellowDParser.NameContext identifier = ctx.name();
        if (NUMBER != null || identifier != null) {
            Expression<Number> repetitions;
            if (NUMBER != null)
                repetitions = new Constant<>(Integer.parseInt(getText(NUMBER)));
            else
                repetitions = lookupName(identifier, Number.class);

            return visitStmtList(ctx.stmtList(), new RepeatedStatementList(repetitions));
        }

        MellowDParser.DynamicChangeStmtContext dynamicChangeStmt = ctx.dynamicChangeStmt();
        if (dynamicChangeStmt != null)
            return visitDynamicChangeStmt(dynamicChangeStmt);

        MellowDParser.PerformStmtContext performStmt = ctx.performStmt();
        if (performStmt != null)
            return visitPerformStmt(performStmt);

        MellowDParser.AssignStmtContext assignStmt = ctx.assignStmt();
        if (assignStmt != null)
            return visitAssignStmt(assignStmt, false);

        MellowDParser.IfStmtContext ifStmt = ctx.ifStmt();
        if (ifStmt != null)
            return visitIfStmt(ifStmt);

        MellowDParser.DoStmtContext doStmt = ctx.doStmt();
        if (doStmt != null)
            return visitDoStmt(doStmt);

        MellowDParser.OnceStmtContext onceStmt = ctx.onceStmt();
        if (onceStmt != null)
            return visitOnceStmt(onceStmt);

        throw new AssertionError("visitStmt compiler not updated after stmt changed");
    }

    @Override
    public Void visitImportStmt(MellowDParser.ImportStmtContext ctx) {
        Set<QualifiedName> functions;
        if (ctx.STAR() != null)
            functions = null;
        else
            functions = ctx.funcs.stream()
                    .map(this::visitName)
                    .collect(Collectors.toSet());

        Qualifier path = compileQualifier(ctx.path);
        Qualifier as = ctx.as == null ? null : compileQualifier(ctx.as);

        MellowDCompiler importCompiler = new MellowDSelectiveCompiler(this.mellowD, path, as, functions);

        try {
            InputStream referencedSrc = this.mellowD.getSrcFinder().resolve(path.getPath());

            CharStream inStream = CharStreams.fromStream(referencedSrc);
            MellowDLexer lexer = new MellowDLexer(inStream);

            //The parser takes the tokens from the lexer as well as the timing environment constructed
            //from the input arguments and a track manager.
            TokenStream tokens = new CommonTokenStream(lexer);
            MellowDParser parser = new MellowDParser(tokens);

            MellowDParser.SongContext parseResult = parser.song();

            importCompiler.visitSong(parseResult);
        } catch (Exception e) {
            throw new CompilationException(ctx, e);
        }

        return null;
    }

    @Override
    public Void visitBlock(MellowDParser.BlockContext ctx) {
        Map<MellowDBlock, SourceLink> blocksReferenced = new LinkedHashMap<>();
        // TODO this could just be a statement that is evaluated at the top level?
        ctx.IDENTIFIER().forEach(id -> {
            MellowDBlock block = mellowD.getBlock(getText(id));
            if (block == null) {
                throw new CompilationException(id, new UndefinedReferenceException(QualifiedName.ofUnqualified(getText(id))));
            }
            blocksReferenced.put(block, new SourceLink(id));
        });


        // Parse the inside for each of the blocks
        StatementList stmtList = visitStmtList(ctx.stmtList());

        if (blocksReferenced.size() > 1) {
            SyncLink link = new SyncLink(blocksReferenced.keySet());
            blocksReferenced.forEach((block, srcLink) -> {
                block.appendStatement(new SyncStatement(srcLink, link));
                block.appendStatement(stmtList);
            });
        } else {
            blocksReferenced.keySet().forEach(block -> block.appendStatement(stmtList));
        }

        MellowDParser.SchedDirsContext schedDirsCtx = ctx.schedDirs();
        if (schedDirsCtx != null) {
            SchedulerDirectives dirs = visitSchedDirs(schedDirsCtx).evaluate(this.mellowD);
            blocksReferenced.keySet().forEach(block -> block.setSchedulerDirectives(dirs));
        }

        return null;
    }

    @Override
    public Expression<SchedulerDirectives.Quantize> visitSchedQuantize(MellowDParser.SchedQuantizeContext ctx) {
        MellowDParser.NumberContext numberCtx = ctx.size;
        if (ctx.size != null)
            return new Constant<>(SchedulerDirectives.quantize(visitNumber(numberCtx), ctx.offset != null ? visitNumber(ctx.offset) : 0));

        return new Constant<>(SchedulerDirectives.alignBlock(getText(ctx.IDENTIFIER())));
    }

    @Override
    public Expression<SchedulerDirectives.Align> visitSchedAlign(MellowDParser.SchedAlignContext ctx) {
        Expression<Beat> padding = null;

        MellowDParser.RhythmContext rhythmCtx = ctx.rhythm();
        if (rhythmCtx != null) {
            Expression<Rhythm> rhythm = visitRhythm(rhythmCtx);
            padding = rhythm.then(Rhythm::getDuration);
        }

        MellowDParser.BeatContext beatCtx = ctx.beat();
        if (beatCtx != null)
            padding = new Constant<>(visitBeat(beatCtx));

        if (ctx.ARROWS_L() != null)
            return padding == null
                    ? new Constant<>(SchedulerDirectives.alignLeft())
                    : padding.then(SchedulerDirectives::alignLeft);
        else
            return padding == null
                    ? new Constant<>(SchedulerDirectives.alignRight())
                    : padding.then(SchedulerDirectives::alignRight);
    }

    @Override
    public Expression<SchedulerDirectives.Finite> visitSchedFinite(MellowDParser.SchedFiniteContext ctx) {
        Integer n = visitNumber(ctx.number());

        if (ctx.STAR() != null)
            return new Constant<>(SchedulerDirectives.runNTimes(n));

        MellowDParser.SchedDirsContext schedDirsCtx = ctx.schedDirs();
        return schedDirsCtx == null
                ? new Constant<>(SchedulerDirectives.interruptNTimes(n))
                : visitSchedDirs(schedDirsCtx).then(nextDirs -> SchedulerDirectives.interruptNTimes(n, nextDirs));
    }

    @Override
    public Expression<SchedulerDirectives> visitSchedDirs(MellowDParser.SchedDirsContext ctx) {
        return new ExpressionList<>(
                ctx.schedDir().stream()
                        .map(dir -> (Expression<Object>) this.visit(dir))
                        .collect(Collectors.toList())
        ).then(dirs -> {
            SchedulerDirectives.Quantize quantize = null;
            SchedulerDirectives.Align align = null;
            SchedulerDirectives.Finite finite = null;

            for (Object dir : dirs) {
                if (dir instanceof SchedulerDirectives.Quantize)
                    quantize = (SchedulerDirectives.Quantize) dir;
                else if (dir instanceof SchedulerDirectives.Align)
                    align = (SchedulerDirectives.Align) dir;
                else if (dir instanceof SchedulerDirectives.Finite)
                    finite = (SchedulerDirectives.Finite) dir;
            }

            return new SchedulerDirectives(quantize, align, finite);
        });
    }

    public StatementList visitStmtList(MellowDParser.StmtListContext ctx, StatementList stmts) {
        ctx.stmt().forEach(stmtCtx -> stmts.add(visitStmt(stmtCtx)));

        return stmts;
    }

    public StatementList visitStmtList(MellowDParser.StmtListContext ctx) {
        return visitStmtList(ctx, new StatementList());
    }

    @Override
    public Parameter<?> visitParameter(MellowDParser.ParameterContext ctx) {
        return this.visitParameterInternal(ctx);
    }

    private <U> Parameter<?> visitParameterInternal(MellowDParser.ParameterContext ctx) {
        MellowDParser.TypeContext typeCtx = ctx.type();
        MellowDParser.ExprContext exprCtx = ctx.expr();

        String name = getText(ctx.IDENTIFIER());
        Class<U> type = typeCtx != null ? (Class<U>) typeCtx.primType.getType() : null;
        Expression<?> defaultValueExpr = exprCtx != null ? visitExpr(exprCtx) : null;
        boolean optional = ctx.OPTIONAL() != null;

        if (type != null) {
            if (optional) {
                if (defaultValueExpr != null)
                    return Parameter.newParameterWithDefault(name, type, new RuntimeTypeCheck<>(type, defaultValueExpr, new SourceLink(exprCtx)));
                else
                    return Parameter.newOptionalParameter(name, type);
            } else {
                return Parameter.newRequiredParameter(name, type);
            }
        } else {
            if (optional) {
                if (defaultValueExpr != null)
                    return Parameter.newParameterWithDefault(name, (Expression<Object>) defaultValueExpr);
                else
                    return Parameter.newOptionalParameter(name);
            } else {
                return Parameter.newRequiredParameter(name);
            }
        }
    }

    @Override
    public Parameters visitParameterList(MellowDParser.ParameterListContext ctx) {
        return new Parameters(
                ctx.parameter().stream()
                        .map(this::visitParameter)
                        .toArray(Parameter<?>[]::new)
        );
    }

    @Override
    public Argument<?> visitArgument(MellowDParser.ArgumentContext ctx) {
        TerminalNode identifier = ctx.IDENTIFIER();
        MellowDParser.ExprContext expr = ctx.expr();

        if (identifier == null)
            if (expr == null)
                return Argument.getEmptyArgInstance();
            else
                return new Argument<>(visitExpr(expr));
        else if (expr == null)
            return new Argument<>(getText(identifier));
        else
            return new Argument<>(getText(identifier), visitExpr(expr));
    }

    @Override
    public Argument<?>[] visitArgumentList(MellowDParser.ArgumentListContext ctx) {
        return ctx.argument().stream()
                .map(this::visitArgument)
                .toArray(Argument[]::new);
    }

    @Override
    public Expression<?> visitCall(MellowDParser.CallContext ctx) {
        Expression<Closure> func = lookupName(ctx.name(), Closure.class);
        Argument<?>[] arguments = visitArgumentList(ctx.argumentList());

        return new FunctionCall(func, arguments);
    }

    @Override
    public Expression<Closure> visitFuncDecl(MellowDParser.FuncDeclContext ctx) {
        MellowDParser.ParameterListContext parameterList = ctx.parameterList();
        Parameters params = parameterList != null
                ? visitParameterList(parameterList)
                : new Parameters();

        boolean percussion = ctx.KEYWORD_PERCUSSION() != null;

        Statement body = visitStmtList(ctx.stmtList());

        return new Abstraction(params, percussion, body);
    }

    // TODO in the future this should be different than a func, a func can be used for a proc but not the other way around

    @Override
    public Expression<Closure> visitProcDecl(MellowDParser.ProcDeclContext ctx) {
        MellowDParser.ParameterListContext parameterList = ctx.parameterList();
        Parameters params = parameterList != null
                ? visitParameterList(parameterList)
                : new Parameters();

        boolean percussion = ctx.KEYWORD_PERCUSSION() != null;

        Statement body = visitStmtList(ctx.stmtList());

        return new Abstraction(params, percussion, body);
    }

    @Override
    public Void visitTopLevelStmt(MellowDParser.TopLevelStmtContext ctx) {
        MellowDParser.AssignStmtContext assignStmt = ctx.assignStmt();
        if (assignStmt != null) {
            visitAssignStmt(assignStmt, true).execute(this.mellowD, NullOutput.getInstance());
            return null;
        }

        MellowDParser.DoStmtContext doStmt = ctx.doStmt();
        if (doStmt != null) {
            visitDoStmt(doStmt).execute(this.mellowD, NullOutput.getInstance());
            return null;
        }

        MellowDParser.BlockContext block = ctx.block();
        this.visitBlock(block);

        return null;
    }

    @Override
    public Void visitSong(MellowDParser.SongContext ctx) {
        ctx.blockDeclStmt().forEach(this::visitBlockDeclStmt);

        ctx.topLevelStmt().forEach(this::visitTopLevelStmt);

        return null;
    }
}
