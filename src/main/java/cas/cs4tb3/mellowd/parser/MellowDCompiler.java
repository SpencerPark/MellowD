package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.intermediate.*;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.*;
import cas.cs4tb3.mellowd.intermediate.executable.statements.*;
import cas.cs4tb3.mellowd.intermediate.functions.*;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Articulatable;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Slurrable;
import cas.cs4tb3.mellowd.intermediate.variables.*;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;
import cas.cs4tb3.mellowd.midi.Knob;
import cas.cs4tb3.mellowd.midi.MIDIControl;
import cas.cs4tb3.mellowd.midi.Pedal;
import cas.cs4tb3.mellowd.primitives.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.util.*;

public class MellowDCompiler extends MellowDParserBaseVisitor {
    protected static final class Identifier {
        public final String[] qualifier;
        public final String name;

        public Identifier(String[] qualifier, String name) {
            this.qualifier = qualifier;
            this.name = name;
        }

        public boolean hasQualifier() {
            return this.qualifier.length != 0;
        }
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
        this.chordParamTypes = new Class[]{ Pitch.class, Chord.class };

        this.rhythmConcatenationDelegate.addDelegate(Beat.class, Rhythm::append);
        this.rhythmConcatenationDelegate.addDelegate(Rhythm.class, Rhythm::append);
        this.rhythmParamType = Slurrable.class;
    }

    @Override
    public Identifier visitIdentifier(MellowDParser.IdentifierContext ctx) {
        List<TerminalNode> fullyQualifiedID = ctx.IDENTIFIER();

        String[] qualifier = new String[fullyQualifiedID.size() - 1];
        for (int i = 0; i < qualifier.length; i++)
            qualifier[i] = fullyQualifiedID.get(i).getText();

        String name = fullyQualifiedID.get(fullyQualifiedID.size() - 1).getText();

        return new Identifier(qualifier, name);
    }

    private <T> Expression<T> lookupIdentifier(MellowDParser.IdentifierContext ctx, Class<T> desiredType) {
        Identifier identifier = visitIdentifier(ctx);
        return new RuntimeTypeCheck<>(desiredType, new ReferenceResolution(identifier.qualifier, identifier.name), ctx);
    }

    private Expression<Object> lookupIdentifier(MellowDParser.IdentifierContext ctx) {
        Identifier identifier = visitIdentifier(ctx);
        return new ReferenceResolution(identifier.qualifier, identifier.name);
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
        MellowDParser.IdentifierContext idCtx = ctx.identifier();
        if (idCtx != null) return lookupIdentifier(idCtx, Integer.class);
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

    private Expression<?> possiblyIndex(ParserRuleContext ctx, Expression<?> expr, MellowDParser.IndexContext indexContext) {
        if (indexContext == null) return expr;

        for (Pair<Expression<Integer>, Expression<Integer>> range : visitIndex(indexContext)) {
            Expression<Indexable<?, ?>> indexable = new RuntimeIndexingSupportCheck(expr, ctx);
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
    public Expression<Pitch> visitNote(MellowDParser.NoteContext ctx) {
        Pitch root = visitPitchRoot(ctx.pitchRoot());
        if (ctx.SHARP() != null) {
            root = root.sharp();
        } else if (ctx.FLAT() != null) {
            root = root.flat();
        }

        if (ctx.octaveShift != null)
            root = root.shiftOctave(visitDirectedNumber(ctx.octaveShift));

        return new Constant<>(root);
    }

    @Override
    public Expression<Chord> visitChord(MellowDParser.ChordContext ctx) {
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
            return visitNote(note);

        MellowDParser.ChordContext chord = ctx.chord();
        if (chord != null)
            return visitChord(chord);

        Expression<?> valueExpr;
        TerminalNode chordLiteral = ctx.CHORD_IDENTIFIER();
        if (chordLiteral != null) {
            valueExpr = new Constant<>(Chord.resolve(chordLiteral.getText()));
        } else {
            MellowDParser.IdentifierContext idCtx = ctx.identifier();
            valueExpr = lookupIdentifier(idCtx);
        }

        valueExpr = new RuntimeNullCheck<>(ctx.getText(), valueExpr, ctx);
        valueExpr = possiblyIndex(ctx, valueExpr, ctx.index());

        return new RuntimeUnionTypeCheck(valueExpr, ctx, this.chordParamTypes);
    }

    @Override
    public Expression<Melody> visitMelody(MellowDParser.MelodyContext ctx) {
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

        MellowDParser.ArticulationContext articulation = ctx.articulation();
        Expression<Articulation> articulationExpr = visitArticulation(articulation);

        MellowDParser.NoteContext note = ctx.note();
        if (note != null)
            return new Articulate(visitNote(note), articulationExpr);

        Expression<?> paramExpr;
        MellowDParser.ChordContext chord = ctx.chord();
        if (chord != null) {
            paramExpr = visitChord(chord);
        } else {
            MellowDParser.IdentifierContext identifier = ctx.identifier();
            if (identifier != null) {
                paramExpr = lookupIdentifier(identifier);
                paramExpr = new RuntimeNullCheck<>(identifier.getText(), paramExpr, identifier);
            } else {
                paramExpr = new Constant<>(Chord.resolve(ctx.CHORD_IDENTIFIER().getText()));
            }

            MellowDParser.IndexContext index = ctx.index();
            paramExpr = possiblyIndex(ctx, paramExpr, index);
        }

        if (articulation != null) {
            Expression<Articulatable> resolvedArticulatable = new RuntimeTypeCheck<>(Articulatable.class, paramExpr, ctx);
            resolvedArticulatable = new RuntimeNullCheck<>(ctx.getText(), resolvedArticulatable, ctx);
            return new Articulate(resolvedArticulatable, articulationExpr);
        }

        return new RuntimeUnionTypeCheck(paramExpr, ctx, this.melodyParamTypes);
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
    public Expression<Rhythm> visitSlurredRhythm(MellowDParser.SlurredRhythmContext ctx) {
        return buildRhythm(ctx.rhythmParam());
    }

    @Override
    public Expression<Rhythm> visitRhythm(MellowDParser.RhythmContext ctx) {
        return buildRhythm(ctx.rhythmParam());
    }

    @Override
    public Expression<Rhythm> visitTuplet(MellowDParser.TupletContext ctx) {
        Rhythm rhythm = new Rhythm();
        boolean slur = ctx.slurDepth % 2 == 1;
        int tupletNumerator = Integer.parseInt(ctx.num.getText());
        int tupletDenominator = ctx.div != null ? Integer.parseInt(ctx.div.getText()) : tupletNumerator - 1;

        if (tupletNumerator <= 1) {
            throw new IllegalArgumentException("Cannot create tuplet with a numerator of " + tupletNumerator);
        } else if (tupletDenominator < 1) {
            throw new IllegalArgumentException("Cannot create a tuplet with a denominator of " + tupletDenominator);
        }

        if (ctx.singleDivision != null) {
            Beat comp = visitBeat(ctx.singleDivision);
            comp = comp.tuplet(tupletNumerator, tupletDenominator);
            comp.setSlurred(slur);
            for (int i = 0; i < tupletNumerator; i++)
                rhythm.append(comp);
        } else {
            ctx.complexDivision.forEach(complexCtx -> {
                Beat comp = visitBeat(complexCtx);
                comp = comp.tuplet(tupletNumerator, tupletDenominator);
                comp.setSlurred(slur);
                rhythm.append(comp);
            });
        }

        return new Constant<>(rhythm);
    }

    @Override
    public Expression<?> visitRhythmParam(MellowDParser.RhythmParamContext ctx) {
        Constant<Boolean> slurred = ctx.slurDepth % 2 == 1 ? TRUE : FALSE;

        MellowDParser.BeatContext beat = ctx.beat();
        if (beat != null)
            return new RuntimeSlur<>(new Constant<>(visitBeat(beat)), slurred);

        MellowDParser.SlurredRhythmContext slurredRhythm = ctx.slurredRhythm();
        if (slurredRhythm != null)
            return new RuntimeSlur<>(visitSlurredRhythm(slurredRhythm), slurred);

        MellowDParser.TupletContext tuplet = ctx.tuplet();
        if (tuplet != null)
            return new RuntimeSlur<>(visitTuplet(tuplet), slurred);

        MellowDParser.IdentifierContext identifier = ctx.identifier();
        Expression<?> idExpr = lookupIdentifier(identifier);

        MellowDParser.IndexContext index = ctx.index();

        idExpr = new RuntimeNullCheck<>(identifier.getText(), idExpr, identifier);
        if (index != null) {
            //We want to make sure that it will actually be indexed to avoid a duplicate runtime null check
            idExpr = possiblyIndex(identifier, idExpr, index);
            idExpr = new RuntimeNullCheck<>(identifier.getText(), idExpr, identifier);
        }

        return new RuntimeSlur<>(new RuntimeTypeCheck<>(Slurrable.class, idExpr, ctx), slurred);
    }

    @Override
    public Expression<?> visitValue(MellowDParser.ValueContext ctx) {
        if (ctx.KEYWORD_TRUE() != null)
            return TRUE;

        if (ctx.KEYWORD_FALSE() != null)
            return FALSE;

        TerminalNode STRING = ctx.STRING();
        if (STRING != null) {
            String raw = STRING.getText();
            return new Constant<>(raw.substring(1, raw.length() - 1));
        }

        MellowDParser.NoteContext note = ctx.note();
        if (note != null)
            return visitNote(note);

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

        Expression<?> valueExpr;
        TerminalNode chordID = ctx.CHORD_IDENTIFIER();
        if (chordID != null) {
            valueExpr = new Constant<>(Chord.resolve(chordID.getText()));
        } else {
            MellowDParser.IdentifierContext identifier = ctx.identifier();
            valueExpr = lookupIdentifier(identifier);
        }

        MellowDParser.IndexContext index = ctx.index();

        valueExpr = new RuntimeNullCheck<>(ctx.getText(), valueExpr, ctx);
        if (index != null) {
            valueExpr = possiblyIndex(ctx, valueExpr, index);
            valueExpr = new RuntimeNullCheck<>(ctx.getText(), valueExpr, ctx);
        }

        return valueExpr;
    }

    public Statement visitVarDeclaration(MellowDParser.VarDeclarationContext ctx, boolean isField) {
        boolean percussionToggle = ctx.STAR() != null;

        Expression<?> valueExpr = visitValue(ctx.value());

        return new AssignmentStatement(new String[0], ctx.IDENTIFIER().getText(), valueExpr, isField, percussionToggle);
    }

    @Override
    public Statement visitVarDeclaration(MellowDParser.VarDeclarationContext ctx) {
        return visitVarDeclaration(ctx, true);
    }

    @Override
    public Statement visitDynamicDeclaration(MellowDParser.DynamicDeclarationContext ctx) {
        if (ctx.ARROWS_LEFT() != null)
            return new ContextFreeStatement(ctx, new GradualDynamicChange(ctx.dynamic, true));
        else if (ctx.ARROWS_LEFT() != null)
            return new ContextFreeStatement(ctx, new GradualDynamicChange(ctx.dynamic, false));
        else
            return new ContextFreeStatement(ctx, new DynamicChange(ctx.dynamic));
    }

    @Override
    public Expression<Phrase> visitPhrase(MellowDParser.PhraseContext ctx) {
        Expression<Melody> lhs;

        MellowDParser.MelodyContext melody = ctx.melody();
        if (melody != null) {
            lhs = visitMelody(melody);
        } else {
            lhs = lookupIdentifier(ctx.melodyRef, Melody.class);
            lhs = new RuntimeNullCheck<>(ctx.melodyRef.getText(), lhs, ctx.melodyRef);
        }

        Expression<Rhythm> rhs;
        MellowDParser.RhythmContext rhythm = ctx.rhythm();
        if (rhythm != null) {
            rhs = visitRhythm(rhythm);
        } else {
            rhs = lookupIdentifier(ctx.rhythmRef, Rhythm.class);
            rhs = new RuntimeNullCheck<>(ctx.rhythmRef.getText(), rhs, ctx.rhythmRef);
        }

        return new PhraseConstruction(lhs, rhs);
    }

    @Override
    public Statement visitBlockConfiguration(MellowDParser.BlockConfigurationContext ctx) {
        Playable playable;
        switch (ctx.IDENTIFIER().getText().toLowerCase()) {
            case "instrument":
                if (ctx.configVal instanceof Number)
                    playable = new InstrumentChange(((Number) ctx.configVal).intValue(), 0);
                else if (ctx.configVal instanceof String) {
                    GeneralMidiInstrument instrument = GeneralMidiInstrument.lookup((String) ctx.configVal);
                    if (instrument == null)
                        throw new CompilationException(ctx, new UndefinedReferenceException((String) ctx.configVal));
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
                MIDIControl<?> controller = MIDIControl.getController(ctx.IDENTIFIER().getText());
                if (controller == null)
                    throw new CompilationException(ctx.IDENTIFIER(), new IllegalArgumentException("Unknown configuration option '" + ctx.IDENTIFIER().getText() + "'"));

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
                    throw new Error("New controller type. visitBlockConfiguration() in the parser needs an update.");
                }
        }

        return new ContextFreeStatement(ctx, playable);
    }

    @Override
    public Void visitBlockDeclaration(MellowDParser.BlockDeclarationContext ctx) {
        CodeBlock config = null;
        if (ctx.BRACE_OPEN() != null) {
            config = new CodeBlock();
            for (MellowDParser.BlockConfigurationContext configCtx : ctx.blockConfiguration()) {
                config.add(visitBlockConfiguration(configCtx));
            }
        }

        for (TerminalNode id : ctx.IDENTIFIER()) {
            try {
                MellowDBlock block = this.mellowD.defineBlock(id.getText(), ctx.KEYWORD_PERCUSSION() != null);
                if (config != null) block.addFragment(config);
            } catch (AlreadyDefinedException e) {
                throw new CompilationException(id, e);
            }
        }
        return null;
    }

    @Override
    public Statement visitStatement(MellowDParser.StatementContext ctx) {
        TerminalNode NUMBER = ctx.NUMBER();
        MellowDParser.IdentifierContext identifier = ctx.identifier();
        if (NUMBER != null || identifier != null) {
            Expression<Number> repetitions;
            if (NUMBER != null)
                repetitions = new Constant<>(Integer.parseInt(NUMBER.getText()));
            else
                repetitions = lookupIdentifier(identifier, Number.class);

            return visitCodeBlock(ctx.codeBlock(), new RepeatedCodeBlock(repetitions));
        }

        MellowDParser.DynamicDeclarationContext dynamicDeclaration = ctx.dynamicDeclaration();
        if (dynamicDeclaration != null)
            return visitDynamicDeclaration(dynamicDeclaration);

        MellowDParser.PhraseContext phrase = ctx.phrase();
        if (phrase != null)
            return new PlayPhraseStatement(visitPhrase(phrase));

        MellowDParser.VarDeclarationContext varDeclaration = ctx.varDeclaration();
        if (varDeclaration != null)
            return visitVarDeclaration(varDeclaration, false);

        MellowDParser.FunctionCallContext functionCall = ctx.functionCall();
        return visitFunctionCall(functionCall);
    }

    @Override
    public Void visitImportStatement(MellowDParser.ImportStatementContext ctx) {
        Set<String> functions;
        if (ctx.STAR() != null) {
            functions = Collections.emptySet();
        } else {
            functions = new HashSet<>();
            functions.addAll(ctx.funcNames);
        }

        String[] path = ctx.path.toArray(new String[ctx.path.size()]);
        String[] as = ctx.as.isEmpty() ? null : ctx.as.toArray(new String[ctx.as.size()]);

        MellowDCompiler importCompiler = new MellowDSelectiveCompiler(this.mellowD, path, as, functions);

        try {
            File referencedSrc = this.mellowD.getSrcFinder().resolve(path);

            ANTLRInputStream inStream = new ANTLRFileStream(referencedSrc.getAbsolutePath());
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
        ctx.IDENTIFIER().forEach(id -> {
            MellowDBlock block = mellowD.getBlock(id.getText());
            if (block == null) {
                throw new CompilationException(id, new UndefinedReferenceException(id.getText()));
            }
            blocksReferenced.put(block, new SourceLink(id));
        });


        //Parse the inside for each of the blocks
        CodeBlock codeBlock = visitCodeBlock(ctx.codeBlock());

        if (blocksReferenced.size() > 1) {
            SyncLink link = new SyncLink(blocksReferenced.keySet());
            blocksReferenced.forEach((block, srcLink) -> {
                block.addFragment(new SyncStatement(srcLink, link));
                block.addFragment(codeBlock);
            });
        } else {
            blocksReferenced.keySet().forEach(block -> block.addFragment(codeBlock));
        }

        return null;
    }

    public CodeBlock visitCodeBlock(MellowDParser.CodeBlockContext ctx, CodeBlock block) {
        ctx.statement().forEach(stmtCtx -> block.add(visitStatement(stmtCtx)));

        return block;
    }

    @Override
    public CodeBlock visitCodeBlock(MellowDParser.CodeBlockContext ctx) {
        return visitCodeBlock(ctx, new CodeBlock());
    }

    @Override
    public Argument<?> visitArgument(MellowDParser.ArgumentContext ctx) {
        TerminalNode IDENTIFIER = ctx.IDENTIFIER();
        MellowDParser.ValueContext value = ctx.value();

        if (IDENTIFIER == null)
            if (value == null) return Argument.getEmptyArgInstance();
            else return new Argument<>(visitValue(value));
        else if (value == null) return new Argument<>(IDENTIFIER.getText());
        else return new Argument<>(IDENTIFIER.getText(), visitValue(value));
    }

    @Override
    public Statement visitFunctionCall(MellowDParser.FunctionCallContext ctx) {
        boolean shouldSave = ctx.KEYWORD_SAVE() != null;

        MellowDParser.IdentifierContext idCtx = ctx.identifier();
        Identifier identifier = visitIdentifier(idCtx);

        Argument<?>[] args = ctx.argument().stream()
                .map(this::visitArgument)
                .toArray(Argument<?>[]::new);
        FunctionBank bank = mellowD.getFunctionBank(identifier.qualifier);

        if (bank == null)
            throw new CompilationException(idCtx, new UndefinedReferenceException(idCtx.getText()));

        FunctionBank.PercussionPair[] options = bank.resolve(identifier.name, args);

        return new FunctionCall(new SourceLink(ctx), this.mellowD, options, identifier.name, shouldSave, args);
    }

    @Override
    public Parameter<?> visitParameter(MellowDParser.ParameterContext ctx) {
        MellowDParser.ValueContext valueContext = ctx.value();
        boolean optional = ctx.OPTIONAL() != null;
        Class<?> type = ctx.type != null ? ctx.type.getType() : null;

        Reference<?> reference;
        Object defaultValue = null;

        if (valueContext != null) {
            Expression<?> defaultValueExpr = visitValue(valueContext);
            defaultValue = defaultValueExpr.evaluate(ctx.percussion ? mellowD.PERCUSSION_TOGGLED_WRAPPER : mellowD);

            if (defaultValue != null && type != null && !type.isInstance(defaultValue))
                throw new CompilationException(valueContext, new IncorrectTypeException(valueContext.getText(), defaultValue.getClass(), type));
        } else if (optional && ctx.type != null) {
            defaultValue = ctx.type.createNew();
        }

        if (type != null)
            if (defaultValue != null)
                reference = new Reference(ctx.IDENTIFIER().getText(), type, defaultValue);
            else
                reference = new Reference<>(ctx.IDENTIFIER().getText(), type);
        else if (defaultValue != null)
            reference = new DynamicallyTypedReference(ctx.IDENTIFIER().getText(), defaultValue);
        else
            reference = new DynamicallyTypedReference(ctx.IDENTIFIER().getText());

        return new Parameter<>(reference, optional);
    }

    @Override
    public Parameters visitParameters(MellowDParser.ParametersContext ctx) {
        return new Parameters(ctx.parameter().stream().map(this::visitParameter).toArray(Parameter<?>[]::new));
    }

    @Override
    public Function visitFunctionDefinition(MellowDParser.FunctionDefinitionContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        boolean percussion = ctx.KEYWORD_PERCUSSION() != null;
        Parameters parameters = ctx.parameters() == null ? new Parameters() : visitParameters(ctx.parameters());

        FunctionSignature signature = new FunctionSignature(name, parameters);
        return new Function(signature, percussion, visitCodeBlock(ctx.codeBlock()));
    }

    @Override
    public Void visitSong(MellowDParser.SongContext ctx) {
        ctx.blockDeclaration().forEach(this::visitBlockDeclaration);
        //The output is null because in the global scope an output doesn't exist
        ctx.varDeclaration().forEach(varCtx -> visitVarDeclaration(varCtx, true).execute(mellowD, null));
        ctx.functionDefinition().forEach(funCtx -> this.mellowD.getFunctionBank().addFunction(visitFunctionDefinition(funCtx)));
        ctx.block().forEach(this::visitBlock);

        return null;
    }
}
