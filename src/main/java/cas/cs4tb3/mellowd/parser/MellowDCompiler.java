package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.intermediate.*;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.*;
import cas.cs4tb3.mellowd.intermediate.executable.statements.*;
import cas.cs4tb3.mellowd.intermediate.functions.*;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.intermediate.variables.*;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;
import cas.cs4tb3.mellowd.midi.Knob;
import cas.cs4tb3.mellowd.midi.MIDIControl;
import cas.cs4tb3.mellowd.midi.Pedal;
import cas.cs4tb3.mellowd.primitives.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.util.*;

public class MellowDCompiler extends MellowDParserBaseVisitor {
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

    public MellowDCompiler(MellowD mellowD) {
        this.mellowD = mellowD;
    }

    public <T> Expression<T> visitReference(MellowDParser.ReferenceContext ctx, Class<T> desiredType) {
        List<TerminalNode> fullyQualifiedID = ctx.IDENTIFIER();
        if (!fullyQualifiedID.isEmpty()) {
            String[] qualifier = new String[fullyQualifiedID.size()-1];
            for (int i = 0; i < qualifier.length; i++)
                qualifier[i] = fullyQualifiedID.get(i).getText();

            String name = fullyQualifiedID.get(fullyQualifiedID.size()-1).getText();
            return new RuntimeTypeCheck<>(desiredType, new ReferenceResolution(qualifier, name), ctx);
        } else {
            Constant<Chord> chordConstant = new Constant<>(Chord.resolve(ctx.CHORD_IDENTIFIER().getText()));
            return new RuntimeTypeCheck<>(desiredType, chordConstant, ctx.CHORD_IDENTIFIER());
        }
    }

    @Override
    public Expression<Object> visitReference(MellowDParser.ReferenceContext ctx) {
        return visitReference(ctx, Object.class);
    }

    @Override
    public Constant<Articulation> visitArticulation(MellowDParser.ArticulationContext ctx) {
        if (ctx == null || ctx.art == null) return ARTICULATION_NONE;
        switch (ctx.art) {
            case NONE:          return ARTICULATION_NONE;
            case STACCATO:      return ARTICULATION_STACCATO;
            case STACCATISSIMO: return ARTICULATION_STACCATISSIMO;
            case MARCATO:       return ARTICULATION_MARCATO;
            case ACCENT:        return ARTICULATION_ACCENT;
            case TENUTO:        return ARTICULATION_TENUTO;
            case GLISCANDO:     return ARTICULATION_GLISCANDO;
            default:    throw new Error("New articulation created without updating visitArticulation()");
        }
    }

    @Override
    public Pitch visitNoteChar(MellowDParser.NoteCharContext ctx) {
        return ctx.pitch;
    }

    @Override
    public Expression<Pitch> visitNoteDef(MellowDParser.NoteDefContext ctx) {
        MellowDParser.NoteCharContext noteChar = ctx.noteChar();
        if (noteChar != null) {
            Pitch basePitch = visitNoteChar(noteChar);
            if (ctx.SHARP() != null) {
                basePitch = basePitch.sharp();
            } else if (ctx.FLAT() != null) {
                basePitch = basePitch.flat();
            }
            MellowDParser.OctaveShiftContext octaveShift = ctx.octaveShift();
            if (octaveShift != null) {
                basePitch = basePitch.shiftOctave(octaveShift.amt);
            }
            return new Constant<>(basePitch);
        } else {
            Expression<?> refValue = visitReference(ctx.reference());
            Expression<Indexable<?>> toIndex = new RuntimeIndexingSupportCheck(refValue, ctx.reference());
            int index = Integer.parseInt(ctx.NUMBER().getText());
            Expression<?> indexExp = new IndexExpression(toIndex, new Constant<>(index));
            return new RuntimeTypeCheck<>(Pitch.class, indexExp, ctx.reference());
        }
    }

    @Override
    public Expression<? extends ConcatableComponent.TypeChord> visitChordParam(MellowDParser.ChordParamContext ctx) {
        MellowDParser.NoteDefContext noteDef = ctx.noteDef();
        if (noteDef != null) {
            return visitNoteDef(noteDef);
        } else {
            MellowDParser.ReferenceContext refId = ctx.reference();
            Expression<?> valueExpr = new RuntimeNullCheck<>(refId.getText(), visitReference(refId), refId);

            TerminalNode indexNode = ctx.NUMBER();
            if (indexNode != null) {
                Expression<Indexable<?>> toIndex = new RuntimeIndexingSupportCheck(valueExpr, refId);
                int index = Integer.parseInt(indexNode.getText());
                Expression<?> indexExpr = new IndexExpression(toIndex, new Constant<>(index));
                return new RuntimeTypeCheck<>(ConcatableComponent.TypeChord.class, indexExpr, refId);
            }


            return new RuntimeTypeCheck<>(ConcatableComponent.TypeChord.class, valueExpr, refId);
        }
    }

    @Override
    public Expression<Chord> visitChord(MellowDParser.ChordContext ctx) {
        Concatenation<Chord, ConcatableComponent.TypeChord> result = new Concatenation<>(Chord::new);
        ctx.params.forEach(paramCtx -> {
            Expression<? extends ConcatableComponent.TypeChord> paramExpr = visitChordParam(paramCtx);
            result.addArgument(paramExpr);
        });
        return result;
    }

    @Override
    public Expression<? extends ConcatableComponent.TypeMelody> visitMelodyParam(MellowDParser.MelodyParamContext ctx) {
        if (ctx.STAR() != null)
            return REST;

        //Visit the first child, one of noteDef, chord or reference
        ParserRuleContext elementCtx = (ParserRuleContext) ctx.getChild(0);
        MellowDParser.ArticulationContext articulationCtx = ctx.articulation();
        Expression<Articulation> articulation = visitArticulation(articulationCtx);
        switch (elementCtx.getRuleIndex()) {
            case MellowDParser.RULE_noteDef:
                Expression<Pitch> pitchExpr = visitNoteDef((MellowDParser.NoteDefContext) elementCtx);
                return new Articulate<>(pitchExpr, articulation);
            case MellowDParser.RULE_chord:
                Expression<Chord> chordExpr = visitChord((MellowDParser.ChordContext) elementCtx);
                return new Articulate<>(chordExpr, articulation);
            case MellowDParser.RULE_reference:
                Expression<ConcatableComponent.TypeMelody> melody = visitReference((MellowDParser.ReferenceContext) elementCtx, ConcatableComponent.TypeMelody.class);
                melody = new RuntimeNullCheck<>(elementCtx.getText(), melody, elementCtx);
                if (articulationCtx != null) {
                    Expression<Articulatable> comp = new RuntimeTypeCheck<>(Articulatable.class, melody, elementCtx);
                    return new Articulate<>(comp, articulation);
                }
                return melody;
            default:
                throw new Error("A new choice was added to the mellodyParam rule without updating visitMelodyParam()");
        }
    }


    @Override
    public Expression<Melody> visitMelody(MellowDParser.MelodyContext ctx) {
        Concatenation<Melody, ConcatableComponent.TypeMelody> result = new Concatenation<>(Melody::new);
        ctx.params.forEach(paramCtx -> {
            Expression<? extends ConcatableComponent.TypeMelody> paramExpr = visitMelodyParam(paramCtx);
            result.addArgument(paramExpr);
        });
        return result;
    }

    @Override
    public Beat visitRhythmDef(MellowDParser.RhythmDefContext ctx) {
        Beat root = ctx.rhythmChar().beat;

        int numDots = ctx.DOT().size();
        if (numDots != 0)
            root = root.dot(numDots);

        return root;
    }

    @Override
    public Expression<Rhythm> visitTuplet(MellowDParser.TupletContext ctx) {
        Rhythm rhythm = new Rhythm();
        boolean slur = ctx.slurDepth % 2 == 1;
        int tupletNumerator = Integer.parseInt(ctx.num.getText());
        int tupletDenominator = ctx.div != null ? Integer.parseInt(ctx.div.getText()) : tupletNumerator - 1;

        if (tupletNumerator <= 1) {
            throw new IllegalArgumentException("Cannot create tuplet with a numerator of "+tupletNumerator);
        } else if (tupletDenominator < 1) {
            throw new IllegalArgumentException("Cannot create a tuplet with a denominator of "+tupletDenominator);
        }

        if (ctx.singleDivision != null) {
            Beat comp = visitRhythmDef(ctx.singleDivision);
            comp = comp.tuplet(tupletNumerator, tupletDenominator);
            comp.setSlurred(slur);
            for (int i = 0; i < tupletNumerator; i++)
                rhythm.append(comp);
        } else {
            ctx.complexDivision.forEach(complexCtx -> {
                Beat comp = visitRhythmDef(complexCtx);
                comp = comp.tuplet(tupletNumerator, tupletDenominator);
                comp.setSlurred(slur);
                rhythm.append(comp);
            });
        }

        return new Constant<>(rhythm);
    }

    @Override
    public Expression<? extends ConcatableComponent.TypeRhythm> visitRhythmParam(MellowDParser.RhythmParamContext ctx) {
        ParserRuleContext paramCtx = ((ParserRuleContext) ctx.getChild(0));
        switch (paramCtx.getRuleIndex()) {
            case MellowDParser.RULE_rhythmDef:
                return new Constant<>(visitRhythmDef((MellowDParser.RhythmDefContext) paramCtx));
            case MellowDParser.RULE_reference:
                Expression<ConcatableComponent.TypeRhythm> comp = visitReference((MellowDParser.ReferenceContext) paramCtx, ConcatableComponent.TypeRhythm.class);
                boolean slur = ctx.slurDepth % 2 == 1;
                return e -> {
                    ConcatableComponent.TypeRhythm val = comp.evaluate(e);
                    val.setSlurred(slur);
                    return val;
                };
            case MellowDParser.RULE_slurredRhythm:
                return visitSlurredRhythm((MellowDParser.SlurredRhythmContext) paramCtx);
            case MellowDParser.RULE_tuplet:
                return visitTuplet((MellowDParser.TupletContext) paramCtx);
            default:
                throw new Error("Another branch was added to the MellowDParser without updating the visitRhythmParam() method.");
        }
    }

    private Expression<Rhythm> buildRhythm(List<MellowDParser.RhythmParamContext> params) {
        Concatenation<Rhythm, ConcatableComponent.TypeRhythm> result = new Concatenation<>(Rhythm::new);
        params.forEach(paramCtx -> {
            Expression<? extends ConcatableComponent.TypeRhythm> paramExpr = visitRhythmParam(paramCtx);
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
    public Expression<?> visitValue(MellowDParser.ValueContext ctx) {
        if (ctx.NUMBER() != null) {
            Integer i = Integer.parseInt(ctx.NUMBER().getText());
            if (ctx.MINUS() != null) i = -i;
            return new Constant<>(i);
        } else if (ctx.STRING() != null) {
            String raw = ctx.getText();
            //Trim the surrounding quotes
            return new Constant<>(raw.substring(1, raw.length()-1));
        } else if (ctx.KEYWORD_TRUE() != null) {
            return TRUE;
        } else if (ctx.KEYWORD_FALSE() != null) {
            return FALSE;
        }

        ParserRuleContext valueCtx = (ParserRuleContext) ctx.getChild(0);
        switch (valueCtx.getRuleIndex()) {
            case MellowDParser.RULE_reference: return visitReference((MellowDParser.ReferenceContext) valueCtx);
            case MellowDParser.RULE_chord: return visitChord((MellowDParser.ChordContext) valueCtx);
            case MellowDParser.RULE_melody: return visitMelody((MellowDParser.MelodyContext) valueCtx);
            case MellowDParser.RULE_rhythm: return visitRhythm((MellowDParser.RhythmContext) valueCtx);
            case MellowDParser.RULE_phrase: return visitPhrase((MellowDParser.PhraseContext) valueCtx);
            default:
                throw new Error("Another branch was added to the MellowDParser without updating the visitValue() method.");
        }
    }

    public Statement visitVarDeclaration(MellowDParser.VarDeclarationContext ctx, boolean isField) {
        boolean percussionToggle = ctx.STAR() != null;

        List<TerminalNode> fullyQualifiedID = ctx.IDENTIFIER();
        if (ctx.KEYWORD_RETURN() != null)
            fullyQualifiedID.add(0, ctx.KEYWORD_RETURN());
        String[] qualifier = new String[fullyQualifiedID.size()-1];
        for (int i = 0; i < qualifier.length; i++)
            qualifier[i] = fullyQualifiedID.get(i).getText();

        String name = fullyQualifiedID.get(fullyQualifiedID.size()-1).getText();

        Expression<?> valueExpr = visitValue(ctx.value());

        return new AssignmentStatement(qualifier, name, valueExpr, isField, percussionToggle);
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
        ParserRuleContext lhsCtx = (ParserRuleContext) ctx.getChild(0);
        Expression<Melody> melody;
        switch (lhsCtx.getRuleIndex()) {
            case MellowDParser.RULE_melody:
                melody = visitMelody((MellowDParser.MelodyContext) lhsCtx);
                break;
            case MellowDParser.RULE_chord:
                melody = new Articulate<>(visitChord((MellowDParser.ChordContext) lhsCtx), visitArticulation(ctx.art))
                        .thenApply(param -> new Melody(Collections.singletonList(param)));
                break;
            case MellowDParser.RULE_reference:
                SourceLink srcLink = new SourceLink(lhsCtx);
                Expression<?> value = visitReference((MellowDParser.ReferenceContext) lhsCtx);
                Articulation art = ctx.art == null ? Articulation.NONE : ctx.art.art;

                melody = new RuntimeNullCheck<>(lhsCtx.getText(), value, lhsCtx).thenApply(lhsResolved -> {
                    if (lhsResolved instanceof Chord ) {
                        return new Melody(new ArticulatedChord((Chord) lhsResolved, art));
                    } else if (lhsResolved instanceof Melody) {
                        return (Melody) lhsResolved;
                    } else if (lhsResolved instanceof Articulated) {
                        return new Melody((Articulated) lhsResolved);
                    } else {
                        return srcLink.throwCompilationException(new IncorrectTypeException(srcLink.text, lhsResolved.getClass(), Melody.class, Chord.class));
                    }
                });
                break;
            default:
                throw new Error("Another branch was added to the MellowDParser without updating the visitPhrase() method.");
        }

        Expression<Rhythm> rhythm;

        if (ctx.rhythmRef != null) {
            rhythm = new RuntimeNullCheck<>(ctx.rhythmRef.getText(), visitReference(ctx.rhythmRef, Rhythm.class), ctx.rhythmRef);
        } else {
            rhythm = visitRhythm(ctx.rhythm());
        }

        return new PhraseConstruction(melody, rhythm);
    }

    @Override
    public Statement visitBlockConfiguration(MellowDParser.BlockConfigurationContext ctx) {
        Playable playable;
        switch (ctx.IDENTIFIER().getText()) {
            case "instrument":
                if (ctx.configVal instanceof Number)
                    playable = new InstrumentChange(((Number) ctx.configVal).intValue(), 0);
                else if (ctx.configVal instanceof String) {
                    GeneralMidiInstrument instrument = GeneralMidiInstrument.lookup((String) ctx.configVal);
                    if (instrument == null) throw new CompilationException(ctx, new UndefinedReferenceException((String) ctx.configVal));
                    playable = new InstrumentChange(instrument);
                } else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot set an instrument to be "+ctx.configVal));
                break;
            case "octave":
                if (ctx.configVal instanceof Number)
                    playable = new OctaveShift(((Number) ctx.configVal).intValue());
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot shift the octave by "+ctx.configVal));
                break;
            case "soundbank":
                if (ctx.configVal instanceof Number)
                    playable = new SoundbankChange(((Number) ctx.configVal).intValue());
                else
                    throw new CompilationException(ctx, new IllegalArgumentException("Cannot set sound bank to  "+ctx.configVal));
                break;
            default:
                MIDIControl<?> controller = MIDIControl.getController(ctx.IDENTIFIER().getText());
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

    public CodeBlock visitCodeBlock(MellowDParser.CodeBlockContext ctx, CodeBlock block) {
        ctx.statement().forEach(stmtCtx -> block.add(visitStatement(stmtCtx)));

        return block;
    }

    @Override
    public CodeBlock visitCodeBlock(MellowDParser.CodeBlockContext ctx) {
        return visitCodeBlock(ctx, new CodeBlock());
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

    @Override
    public Argument<?> visitArgument(MellowDParser.ArgumentContext ctx) {
        String name = ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText();
        Expression<?> value = ctx.value() == null ? null : visitValue(ctx.value());

        if (name == null)
            if (value == null) return Argument.getEmptyArgInstance();
            else               return new Argument<>(value);
        else
            if (value == null) return new Argument<>(name);
            else               return new Argument<>(name, value);
    }

    @Override
    public Statement visitFunctionCall(MellowDParser.FunctionCallContext ctx) {
        boolean shouldReturn = ctx.KEYWORD_RETURN() != null;

        List<TerminalNode> fullFunctionName = ctx.IDENTIFIER();
        String[] bankQualifier = new String[fullFunctionName.size()-1];
        for (int i = 0; i < bankQualifier.length; i++)
            bankQualifier[i] = fullFunctionName.get(i).getText();
        String functionName = fullFunctionName.get(fullFunctionName.size()-1).getText();

        Argument<?>[] args = ctx.argument().stream()
                .map(this::visitArgument)
                .toArray(Argument<?>[]::new);
        FunctionBank bank = mellowD.getFunctionBank(bankQualifier);
        if (bank == null) {
            Token start = fullFunctionName.get(0).getSymbol();
            Token stop = fullFunctionName.get(fullFunctionName.size()-1).getSymbol();
            throw new CompilationException(start.getStartIndex(),
                    start.getLine(),
                    start.getCharPositionInLine(),
                    stop.getStopIndex(),
                    Arrays.toString(bankQualifier),
                    new UndefinedReferenceException(ctx.IDENTIFIER().toString()));
        }
        FunctionBank.PercussionPair[] options = bank.resolve(functionName, args);

        return new FunctionCall(new SourceLink(ctx), options, functionName, shouldReturn, args);
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
        else
            if (defaultValue != null)
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

    @Override
    public Statement visitStatement(MellowDParser.StatementContext ctx) {
        if (ctx.NUMBER() != null) {
            int repetitions = Integer.parseInt(ctx.NUMBER().getText());
            if (repetitions == 0) return new CodeBlock();
            if (repetitions == 1) return visitCodeBlock(ctx.codeBlock());
            return visitCodeBlock(ctx.codeBlock(), new RepeatedCodeBlock(repetitions));
        } else {
            ParserRuleContext stmtContext = (ParserRuleContext) ctx.getChild(0);
            //one of dynamicDeclaration, phrase, reference, varDeclaration, functionCall
            switch (stmtContext.getRuleIndex()) {
                case MellowDParser.RULE_dynamicDeclaration:
                    return visitDynamicDeclaration((MellowDParser.DynamicDeclarationContext) stmtContext);
                case MellowDParser.RULE_phrase:
                    return new PlayPhraseStatement(visitPhrase((MellowDParser.PhraseContext) stmtContext));
                case MellowDParser.RULE_reference:
                    return new PlayPhraseStatement(new RuntimeNullCheck<>(stmtContext.getText(), visitReference((MellowDParser.ReferenceContext) stmtContext, Phrase.class), stmtContext));
                case MellowDParser.RULE_varDeclaration:
                    return visitVarDeclaration((MellowDParser.VarDeclarationContext) stmtContext, false);
                case MellowDParser.RULE_functionCall:
                    return visitFunctionCall((MellowDParser.FunctionCallContext) stmtContext);
                default:
                    throw new Error("choice added to 'statement' in the parser without updating visitStatement()");
            }
        }
    }

    @Override
    public Void visitImportStatement(MellowDParser.ImportStatementContext ctx) {
        Set<String> functions;
        if (ctx.STAR() != null) {
            functions = Collections.EMPTY_SET;
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
}
