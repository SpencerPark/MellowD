package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.*;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.intermediate.variables.*;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;
import cas.cs4tb3.mellowd.midi.GeneralMidiPercussion;
import cas.cs4tb3.mellowd.primitives.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class MellowDParseTreeWalker extends MellowDParserBaseVisitor {
    public static final Reference<Integer> TRANSPOSE_AMT = new Reference<>("transpose", Integer.class, 0);
    public static final Reference<Integer> OCTAVE_SHIFT = new Reference<>("octave", Integer.class, 0);
    public static final Reference<Boolean> PERCUSSION = new Reference<>("percussion", Boolean.class, false);

    private final MellowD mellowD;
    private MellowDBlock currentBlock;

    public MellowDParseTreeWalker(MellowD mellowD) {
        this.mellowD = mellowD;
    }

    private Memory getMemory() {
        return currentBlock != null ? currentBlock.getLocalMemory() : mellowD.getGlobalMemory();
    }

    //TODO adjust melodies and chords that haven't been resolved in the right memory
    //EX: chord declared in global scope will be wrong

    private static Pitch adjust(Pitch pitch, Memory memory) {
        int octave = OCTAVE_SHIFT.dereference(memory);
        return pitch.shiftOctave(octave - pitch.getOctave());
    }

    private static Chord adjust(Chord chord, Memory memory) {
        return chord.transpose(TRANSPOSE_AMT.dereference(memory)).shiftOctave(OCTAVE_SHIFT.dereference(memory));
    }

    private static <T> T checkTypeOrThrow(ParseTree identifier, Object value, Class<T> expectedType) {
        if (value == null || expectedType.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            throw new CompilationException(identifier, new IncorrectTypeException(identifier.getText(), value.getClass(), expectedType));
        }
    }

    private static Object checkTypeOrThrow(ParseTree identifier, Object value, Class<?>... expectedTypes) {
        if (value == null)
            return null;

        Class<?> valueType = value.getClass();
        for (Class<?> expectedType : expectedTypes) {
            if (expectedType.isAssignableFrom(valueType)) return value;
        }

        throw new CompilationException(identifier, new IncorrectTypeException(identifier.getText(), value.getClass(), expectedTypes));
    }

    private static <T> T checkDefined(ParseTree identifier, T value) {
        if (value != null) return value;
        throw new CompilationException(identifier, new UndefinedReferenceException(identifier.getText()));
    }

    private static Indexable<?> checkSupportsIndexing(ParseTree identifier, Object value) {
        if (Indexable.class.isAssignableFrom(value.getClass())) {
            return (Indexable<?>) value;
        } else {
            throw new CompilationException(identifier, new IndexingNotSupportedException("Cannot index "+identifier.getText()));
        }
    }

    public <T> T visitReference(MellowDParser.ReferenceContext ctx, Class<T> desiredType) {
        Memory memory = getMemory();

        TerminalNode idToken = ctx.IDENTIFIER();
        if (idToken != null) {
            String id = idToken.getText();
            //Do a quick keyword check
            switch (id.toLowerCase()) {
                case "true":
                case "yes":
                case "on":
                    return checkTypeOrThrow(idToken, true, desiredType);
                case "false":
                case "no":
                case "off":
                    return checkTypeOrThrow(idToken, false, desiredType);
            }

            if (PERCUSSION.dereference(memory)) {
                GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup(id);
                if (drumSound != null)
                    return checkTypeOrThrow(idToken, drumSound.getAsPitch(), desiredType);
            }

            return checkTypeOrThrow(idToken, memory.get(id), desiredType);
        } else {
            String id = ctx.CHORD_IDENTIFIER().getText();
            return checkTypeOrThrow(ctx.CHORD_IDENTIFIER(), Chord.resolve(id), desiredType);
        }
    }
    @Override
    public Object visitReference(MellowDParser.ReferenceContext ctx) {
        return visitReference(ctx, Object.class);
    }

    @Override
    public Articulation visitArticulation(MellowDParser.ArticulationContext ctx) {
        return ctx.art;
    }

    @Override
    public Pitch visitNoteChar(MellowDParser.NoteCharContext ctx) {
        return ctx.pitch;
    }

    @Override
    public Pitch visitNoteDef(MellowDParser.NoteDefContext ctx) {
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
            return basePitch;
        } else {
            Object refValue = checkDefined(ctx.reference(), visitReference(ctx.reference()));
            Indexable<?> toIndex = checkSupportsIndexing(ctx.reference(), refValue);
            int index = Integer.parseInt(ctx.NUMBER().getText());
            return checkTypeOrThrow(ctx, toIndex.getAt(index), Pitch.class);
        }
    }

    @Override
    public ChordElement visitChordParam(MellowDParser.ChordParamContext ctx) {
        MellowDParser.NoteDefContext noteDef = ctx.noteDef();
        if (noteDef != null) {
            return visitNoteDef(noteDef);
        } else {
            MellowDParser.ReferenceContext refId = ctx.reference();
            Object value = checkDefined(refId, visitReference(refId));

            TerminalNode indexNode = ctx.NUMBER();
            if (indexNode != null) {
                Indexable<?> toIndex = checkSupportsIndexing(refId, value);
                int index = Integer.parseInt(indexNode.getText());
                return checkTypeOrThrow(ctx, toIndex.getAt(index), ChordElement.class);
            }

            return checkTypeOrThrow(ctx, value, ChordElement.class);
        }
    }

    @Override
    public Chord visitChord(MellowDParser.ChordContext ctx) {
        List<ChordElement> elements = new LinkedList<>();
        ctx.params.forEach(paramCtx -> elements.add(visitChordParam(paramCtx)));
        return new Chord(elements);
    }

    /**
     * This method will always return a {@link Melody} or an {@link Articulated}
     */
    @Override
    public final Object visitMelodyParam(MellowDParser.MelodyParamContext ctx) {
        if (ctx.STAR() != null)
            return new ArticulatedPitch(Pitch.REST);

        //Visit the first child, one of noteDef, chord or reference
        ParseTree elementCtx = ctx.getChild(0);
        Object param = visit(elementCtx);
        MellowDParser.ArticulationContext articulationCtx = ctx.articulation();
        if (articulationCtx != null) {
            Articulatable element = checkTypeOrThrow(elementCtx, param, Articulatable.class);
            return element.articulate(articulationCtx.art);
        } else if (param instanceof Articulatable) {
            return ((Articulatable) param).articulate(Articulation.NONE);
        } else {
            return checkDefined(elementCtx, checkTypeOrThrow(elementCtx, param, Melody.class));
        }
    }

    @Override
    public Melody visitMelody(MellowDParser.MelodyContext ctx) {
        Melody melody = new Melody();
        ctx.params.forEach(paramCtx -> {
            Object param = visitMelodyParam(paramCtx);
            if (param instanceof Melody) {
                melody.add((Melody) param);
            } else if (param instanceof Articulated){
                melody.add(((Articulated) param));
            } else {
                throw new Error("visitMelodyParam broke it's contract. The return type was a "+param.getClass());
            }
        });
        return melody;
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
    public Rhythm visitTuplet(MellowDParser.TupletContext ctx) {
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
            for (int i = 0; i < tupletNumerator; i++)
                rhythm.append(comp, slur);
        } else {
            ctx.complexDivision.forEach(complexCtx -> {
                Beat comp = visitRhythmDef(complexCtx);
                comp = comp.tuplet(tupletNumerator, tupletDenominator);
                rhythm.append(comp, slur);
            });
        }

        return rhythm;
    }

    /**
     * This method will always return a {@link Rhythm} or an {@link Beat}
     */
    @Override
    public final Object visitRhythmParam(MellowDParser.RhythmParamContext ctx) {
        ParserRuleContext paramCtx = ((ParserRuleContext) ctx.getChild(0));
        switch (paramCtx.getRuleIndex()) {
            case MellowDParser.RULE_rhythmDef:
                return visitRhythmDef((MellowDParser.RhythmDefContext) paramCtx);
            case MellowDParser.RULE_reference:
                Object resolvedReference = checkTypeOrThrow(paramCtx, visitReference((MellowDParser.ReferenceContext) paramCtx), Beat.class, Rhythm.class);
                if (resolvedReference instanceof Rhythm && ctx.slurDepth % 2 == 1)
                    ((Rhythm) resolvedReference).slurAll();
                return resolvedReference;
            case MellowDParser.RULE_slurredRhythm:
                return visitSlurredRhythm((MellowDParser.SlurredRhythmContext) paramCtx);
            case MellowDParser.RULE_tuplet:
                return visitTuplet((MellowDParser.TupletContext) paramCtx);
            default:
                throw new Error("Another branch was added to the MellowDParser without updating the visitRhythmParam() method.");
        }
    }

    private Rhythm buildRhythm(List<MellowDParser.RhythmParamContext> params) {
        Rhythm rhythm = new Rhythm();

        params.forEach(paramCtx -> {
            boolean slur = paramCtx.slurDepth % 2 == 1;

            Object param = visitRhythmParam(paramCtx);
            if (param instanceof Beat) {
                rhythm.append((Beat) param, slur);
            } else if (param instanceof Rhythm) {
                rhythm.append((Rhythm) param);
            } else {
                throw new Error("visitRhythmParam broke it's contract. The return type was a "+param.getClass());
            }
        });

        return rhythm;
    }

    @Override
    public Rhythm visitSlurredRhythm(MellowDParser.SlurredRhythmContext ctx) {
        return buildRhythm(ctx.rhythmParam());
    }

    @Override
    public Rhythm visitRhythm(MellowDParser.RhythmContext ctx) {
        return buildRhythm(ctx.rhythmParam());
    }

    @Override
    public Object visitValue(MellowDParser.ValueContext ctx) {
        if (ctx.NUMBER() != null) {
            Integer i = Integer.parseInt(ctx.NUMBER().getText());
            if (ctx.MINUS() != null) i = -i;
            return i;
        }

        return visit(ctx.getChild(0));
    }

    @Override
    public Void visitVarDeclaration(MellowDParser.VarDeclarationContext ctx) {
        boolean percussionMode = ctx.STAR() != null;

        String id = ctx.IDENTIFIER().getText();

        MellowDParser.ValueContext value = ctx.value();

        //These closure assignments will build the value when it is called
        //so that all definitions first get a chance to be assigned allowing
        //the assignment order to not matter
        getMemory().set(id, (DelayedResolution) memory -> {
            boolean wasInPercussionMode = PERCUSSION.dereference(memory);
            PERCUSSION.redefine(memory, percussionMode);

            Object toReturn = visitValue(value);

            PERCUSSION.redefine(memory, wasInPercussionMode);

            return toReturn;
        });

        return null;
    }

    @Override
    public DynamicChange visitDynamicDeclaration(MellowDParser.DynamicDeclarationContext ctx) {
        if (ctx.DYNAMIC_CRES() != null)
            return new GradualDynamicChange(ctx.dynamic, true);
        else if (ctx.DYNAMIC_DECRES() != null)
            return new GradualDynamicChange(ctx.dynamic, false);
        else
            return new DynamicChange(ctx.dynamic);
    }

    @Override
    public Phrase visitPhrase(MellowDParser.PhraseContext ctx) {
        Melody melody;

        ParserRuleContext lhsCtx = (ParserRuleContext) ctx.getChild(0);
        Object lhs;
        switch (lhsCtx.getRuleIndex()) {
            case MellowDParser.RULE_melody:
                lhs = visitMelody((MellowDParser.MelodyContext) lhsCtx);
                break;
            case MellowDParser.RULE_chord:
                lhs = visitChord((MellowDParser.ChordContext) lhsCtx);
                break;
            case MellowDParser.RULE_reference:
                lhs = checkTypeOrThrow(lhsCtx, visitReference((MellowDParser.ReferenceContext) lhsCtx), Chord.class, Melody.class);
                break;
            default:
                throw new Error("Another branch was added to the MellowDParser without updating the visitPhrase() method.");
        }

        if (lhs instanceof Melody) {
            melody = (Melody) lhs;
        } else {
            Articulated articulated;
            if (ctx.art != null) {
                articulated = ((Chord) lhs).articulate(visitArticulation(ctx.art));
            } else {
                articulated = ((Chord) lhs).articulate(Articulation.NONE);
            }
            melody = new Melody(Collections.singletonList(articulated));
        }

        Rhythm rhythm;

        if (ctx.rhythmRef != null) {
            rhythm = checkDefined(ctx.rhythmRef, visitReference(ctx.rhythmRef, Rhythm.class));
        } else {
            rhythm = visitRhythm(ctx.rhythm());
        }

        return new Phrase(melody, rhythm);
    }

    @Override
    public Void visitBlock(MellowDParser.BlockContext ctx) {
        List<MellowDBlock> blocksReferenced = new LinkedList<>();
        ctx.IDENTIFIER().forEach(id -> blocksReferenced.add(mellowD.getBlock(id.getText())));

        //Sync them up
        if (blocksReferenced.size() > 1) {
            TimingEnvironment timingEnvironment = this.mellowD.getTimingEnvironment();
            Map<MellowDBlock, Long> times = new HashMap<>(blocksReferenced.size());
            long longestTime = 0;
            for (MellowDBlock block : blocksReferenced) {
                long time = block.calculateDuration(timingEnvironment);
                if (time > longestTime) {
                    longestTime = time;
                }
                times.put(block, time);
            }

            for (Map.Entry<MellowDBlock, Long> entry : times.entrySet()) {
                if (entry.getValue() < longestTime) {
                    entry.getKey().add(new LeapInTime(longestTime - entry.getValue()));
                }
            }
        }

        //Parse the inside for each of the blocks
        blocksReferenced.forEach(currentBlock -> {
            this.currentBlock = currentBlock;

            ctx.blockContents.forEach(contentCtx -> {
                if (contentCtx instanceof MellowDParser.ReferenceContext) {
                    this.currentBlock.add(checkDefined(contentCtx, visitReference((MellowDParser.ReferenceContext) contentCtx, Phrase.class)));
                    return;
                }

                Object content = visit(contentCtx);
                if (content instanceof Playable) {
                    this.currentBlock.add((Playable) content);
                }
            });
        });

        this.currentBlock = null;

        return null;
    }

    //TODO non-pure functions, need a way to deal with invocations outside of a block. Where does it play?
    @Override
    public Object visitFunctionCall(MellowDParser.FunctionCallContext ctx) {
        List<Object> args = new ArrayList<>(ctx.value().size());
        ctx.value().forEach(value -> {
            Object val = visitValue(value);
            if (val == null) args.add(value.getText());
            else             args.add(val);
        });

        //Catch all of the default functions here
        switch (ctx.IDENTIFIER().getText()) {
            case "octave":
                if (args.size() != 1 || !(args.get(0) instanceof Number))
                    throw new IllegalArgumentException("The octave function takes 1 argument. The shift amount");
                return new OctaveShift(((Number) args.get(0)).intValue());
            case "instrument":
                if ((args.size() != 1 && args.size() != 2))
                    throw new IllegalArgumentException("The instrument function takes 1 or 2 arguments. The instrument and optionally the soundbank");
                Object arg1 = args.get(0);
                int instrument;
                if (arg1 instanceof Number) {
                    instrument = ((Number) arg1).intValue();
                } else if (arg1 instanceof String) {
                    instrument = GeneralMidiInstrument.lookup((String) arg1).midiNum();
                } else {
                    throw new IllegalArgumentException("Wrong type. " + arg1.getClass());
                }
                int soundBank = 0;
                if (args.size() == 2) {
                    Object arg2 = args.get(1);
                    if (arg2 instanceof Number) {
                        soundBank = ((Number) arg2).intValue();
                    } else {
                        throw new IllegalArgumentException("Wrong type. " + arg1.getClass());
                    }
                }
                return new InsrtumentChange(instrument, soundBank);
        }

        //TODO declared saved functions
        return null;
    }
}
