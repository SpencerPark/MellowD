//Mellow D Parser
//==============

parser grammar MellowDParser;

//Import the token vocabulary described by the [*MellowD Lexer*](MellowDLexer.html)
options {
	tokenVocab = MellowDLexer;
}

//imports
@header {
    import cas.cs4tb3.mellowd.*;
    import cas.cs4tb3.mellowd.midi.*;
    import cas.cs4tb3.mellowd.primitives.*;
    import javax.sound.midi.*;
    import java.util.*;
}

//methods and class body
@members {
    private BlockOptions globalOptions = new BlockOptions();
    private SymbolTable symbolTable = new SymbolTable();
    private TimingEnvironment timingEnv;
    private Sequence midiSequence;
    private TrackManager trackManager;

    public MellowDParser(TokenStream inputStream, TimingEnvironment timingEnv, TrackManager trackManager) {
        this(inputStream);
        this.timingEnv = timingEnv;
        this.trackManager = trackManager;
        this.midiSequence = timingEnv.createSequence();
    }

    public BlockOptions getGlobalOptions() {
        return this.globalOptions;
    }

    public Sequence getSequence() {
        return this.midiSequence;
    }

    private Chord lookupChord(Token identifier, int octave) {
        this.symbolTable.checkType(identifier, Chord.class);

        Chord chord = this.symbolTable.getDeclarationValue(identifier.getText(), Chord.class);
        if (chord != null) return chord.inOctave(octave);

        chord = Chord.resolve(identifier.getText(), octave);

        if (chord == null)
            throw new UndefinedReferenceException(identifier, "Identifier ("+identifier.getText()+") is undefined.");

        return chord;
    }

    private void appendToMelody(Melody root, BlockOptions context, MidiNoteMessageSource notes, Token identifier, Articulation articulation) {
        if (notes == null) {
            if (this.symbolTable.identifierTypeIs(identifier.getText(), Melody.class)) {
                Melody mel = this.symbolTable.getDeclarationValue(identifier.getText(), Melody.class).inOctave(context.getOctave());
                root.add(mel);
            } else {
                Chord chord = lookupChord(identifier, context.getOctave());
                root.add(new ArticulatedSound(chord, articulation));
            }
        } else {
            root.add(new ArticulatedSound(notes, articulation));
        }
    }
}

octaveShift
    : ( PLUS | MINUS ) NUMBER;

articulation
returns [Articulation art]
    : DOT           {$art = Articulation.STACCATO;      }
    | EXCLAMATION   {$art = Articulation.STACCATISSIMO; }
    | HAT           {$art = Articulation.MARCATO;       }
    | BACK_TICK     {$art = Articulation.ACCENT;        }
    | USCORE        {$art = Articulation.TENUTO;        }
    | TILDA         {$art = Articulation.GLISCANDO;     }
    ;

noteChar[BlockOptions options]
returns [Pitch pitch]
    : A     {$pitch = Pitch.A.inOctave($options.getOctave());}
    | B     {$pitch = Pitch.B.inOctave($options.getOctave());}
    | C     {$pitch = Pitch.C.inOctave($options.getOctave());}
    | D     {$pitch = Pitch.D.inOctave($options.getOctave());}
    | E     {$pitch = Pitch.E.inOctave($options.getOctave());}
    | F     {$pitch = Pitch.F.inOctave($options.getOctave());}
    | G     {$pitch = Pitch.G.inOctave($options.getOctave());}
    ;

noteDef[BlockOptions options]
returns [Pitch pitch]
    :   ( noteChar[$options]    {$pitch = $noteChar.pitch;}
            (   SHARP           {$pitch = $pitch.sharp();}
            |   FLAT            {$pitch = $pitch.flat();}
            )?
            ( octaveShift {$pitch = $pitch.shiftOctave(Integer.parseInt($octaveShift.text));} )?
        )
    ;

chordParam[BlockOptions options]
returns [List<Pitch> pitches]
    : noteDef[$options] {$pitches = Collections.singletonList($noteDef.pitch);}
    | IDENTIFIER {!$options.isPercussion()}? ( COLON NUMBER )?
        {
            if ($options.isPercussion()) {
                GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup($IDENTIFIER.getText());
                if (drumSound != null)
                    $pitches = Collections.singletonList(drumSound.getAsPitch());
            }
            if ($pitches != null) {
                Chord chord = lookupChord($IDENTIFIER, $options.getOctave());
                if ($NUMBER == null) {
                    $pitches = chord.getPitches();
                } else {
                    int index = $NUMBER.int - 1; //Indexing will start at 1
                    if (index > chord.size())
                        throw new ParseException($IDENTIFIER, "Index "+index+" is larger than the chord size of "+chord.size());
                    $pitches = Collections.singletonList(chord.getPitch(index));
                }
            }
        }
    ;

chord[BlockOptions options]
returns [ArticulatedSound sound]
locals [List<Pitch> pitches = new LinkedList<>()]
    :   PAREN_OPEN
        chordParam[$options] {$pitches.addAll($chordParam.pitches);}
        ( COMMA chordParam[$options] {$pitches.addAll($chordParam.pitches);} )*
        PAREN_CLOSE {$sound = new ArticulatedSound(new Chord($pitches), Articulation.NONE);}
        ( articulation {$sound.setArticulation($articulation.art);} )? ;

melodyParam[BlockOptions options]
returns [MidiNoteMessageSource notes = null, Token identifier = null, Articulation art = Articulation.NONE]
    :    noteDef[$options] {$notes = $noteDef.pitch;}
            ( articulation {$art = $articulation.art;} )?

    |    IDENTIFIER
            ({!$options.isPercussion()}? COLON NUMBER
                {
                    Chord chord = lookupChord($IDENTIFIER, $options.getOctave());
                    int index = $NUMBER.int - 1; //Indexing will start at 1
                    if (index > chord.size())
                        throw new ParseException($IDENTIFIER, "Index "+index+" is larger than the chord size of "+chord.size());
                    $notes = chord.getPitch(index);
                }
            )?
            {   if ($notes == null) {
                    if ($options.isPercussion()) {
                        GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup($IDENTIFIER.getText());
                        if (drumSound == null)
                            throw new UndefinedReferenceException($IDENTIFIER, "Unknown drum sound "+$IDENTIFIER.getText());
                        $notes = drumSound.getAsPitch();
                    } else {
                        $identifier = $IDENTIFIER;
                    }
                }
            }
            ( articulation {$art = $articulation.art;} )?

    | chord[$options]   {
                            $notes = $chord.sound.getSound();
                            $art = $chord.sound.getArticulation();
                        }
    | STAR {$notes = Pitch.REST;}
    ;

melody[BlockOptions options]
returns [Melody mel = new Melody(new LinkedList<ArticulatedSound>())]
    :   BRACKET_OPEN
        melodyParam[$options] {appendToMelody($mel, $options, $melodyParam.notes, $melodyParam.identifier, $melodyParam.art);}
        ( COMMA melodyParam[$options] {appendToMelody($mel, $options, $melodyParam.notes, $melodyParam.identifier, $melodyParam.art);} )*
        BRACKET_CLOSE
    ;

rhythmChar
returns [Beat beat]
    : W {$beat = Beat.WHOLE;        }
    | H {$beat = Beat.HALF;         }
    | Q {$beat = Beat.QUARTER;      }
    | E {$beat = Beat.EIGHTH;       }
    | S {$beat = Beat.SIXTEENTH;    }
    | T {$beat = Beat.THIRTYSECOND; }
    ;

rhythmDef
returns [Beat beat]
    : rhythmChar ( DOT )* {$beat = $DOT == null ? $rhythmChar.beat : $rhythmChar.beat.dot($DOT.text.length());};

slurredRhythm[Rhythm rhy]
    : PAREN_OPEN rhythmParam[$rhy, true] ( COMMA rhythmParam[$rhy, true] )* PAREN_CLOSE ;

rhythmParam[Rhythm rhy, boolean slur]
    : rhythmDef     { $rhy.append($rhythmDef.beat, $slur);}
    | IDENTIFIER    {
                        Rhythm value = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Rhythm.class);
                        $rhy.append(value, $slur);
                    }
    | slurredRhythm[$rhy]
    ;

rhythm
returns [Rhythm rhy = new Rhythm()]
    : P_BRACKET_OPEN rhythmParam[$rhy, false] ( COMMA rhythmParam[$rhy, false] )* P_BRACKET_CLOSE ;

varDeclaration
    : IDENTIFIER ASSIGNMENT ( chord [this.globalOptions]    {this.symbolTable.addDeclaration($IDENTIFIER.text, $chord.sound.getSound());}
                            | melody[this.globalOptions]    {this.symbolTable.addDeclaration($IDENTIFIER.text, $melody.mel);}
                            | rhythm                        {this.symbolTable.addDeclaration($IDENTIFIER.text, $rhythm.rhy);}
                            | phrase[this.globalOptions]    {this.symbolTable.addDeclaration($IDENTIFIER.text, $phrase.phr);}
                            )
    ;

dynamicDeclaration
returns [Dynamic dynamic]
    : PPPP  {$dynamic = Dynamic.pppp;}
    | PPP   {$dynamic = Dynamic.ppp; }
    | PP    {$dynamic = Dynamic.pp;  }
    | P     {$dynamic = Dynamic.p;   }
    | MP    {$dynamic = Dynamic.mp;  }
    | MF    {$dynamic = Dynamic.mf;  }
    | F     {$dynamic = Dynamic.f;   }
    | FF    {$dynamic = Dynamic.ff;  }
    | FFF   {$dynamic = Dynamic.fff; }
    | FFFF  {$dynamic = Dynamic.ffff;}
    ;

blockOptions[BlockOptions options]
    : BRACKET_OPEN ( ( propertyOption[$options] | flagOption[$options] ) ( COMMA ( propertyOption[$options] | flagOption[$options] ) )* )? BRACKET_CLOSE;

propertyOption[BlockOptions options]
    : key=IDENTIFIER ASSIGNMENT ( valueID=IDENTIFIER | valueNum=NUMBER )
        {   switch($key.text.toLowerCase()) {
                case "instrument":
                    if ($valueID != null) {
                        $options.setInstrument($valueID);
                    } else {
                        int inst = $valueNum.int;
                        if (inst > 127) throw new ParseException($valueNum, "Instrument code must be less than 128.");
                        $options.setInstrument(inst);
                    }
                    break;
                case "soundbank":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected a soundbank id but found "+$valueID.text);
                    $options.setSoundbank($valueNum.int);
                    break;
                case "octave":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected octave number but found "+$valueID.text);
                    int octave = $valueNum.int;
                    if (octave > 10) throw new ParseException($valueNum, "Octave is too high. 10 is highest byt found "+octave);
                    $options.setOctave(octave);
                    break;
                case "loop":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected a loop count but found "+$valueID.text);
                    $options.setLoopCount($valueNum.int);
                    break;
                case "onchannel":
                case "samechannelas":
                case "sharechannel":
                    if ($valueID == null)
                        throw new ParseException($valueNum, "Expected the name of the block that this channel should share a channel with"
                            + " but found " + $valueNum.text);
                    $options.setShareChannel($valueID.text);
                    break;
                case "channel":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected the number of the MIDI channel to requests but"
                            + " found " + $valueID.text);
                    $options.setChannel($valueNum.int);
                    break;
                default:
                    throw new ParseException($key, "Unknow block option " + $key.text + ".");
            }
        }
    ;

flagOption[BlockOptions options]
    :   ( off=MINUS )? flag=IDENTIFIER
        {
            switch ($flag.text.toLowerCase()) {
                case "percussion":
                case "drums":
                    $options.setPercussion($off == null);
                    break;
                default:
                    throw new ParseException($flag, "Unknow block option " + $flag.text + ".");
            }
        }
    ;

phrase[BlockOptions options]
returns [Phrase phr]
locals [Melody mel]
    :   (   ( melody[$options]  {$mel = $melody.mel;}
            | chord[$options]   {$mel = new Melody(Arrays.asList($chord.sound));}
            | IDENTIFIER articulation?
                            {
                                if (this.symbolTable.identifierTypeIs($IDENTIFIER.getText(), Melody.class)) {
                                    $mel = this.symbolTable.getDeclarationValue($IDENTIFIER.getText(), Melody.class).inOctave($options.getOctave());
                                    if ($articulation.ctx != null)
                                        throw new IncorrectTypeException($IDENTIFIER, "Cannot articulate a melody.");
                                } else {
                                    Chord chord = lookupChord($IDENTIFIER, $options.getOctave());
                                    List<ArticulatedSound> notes = new ArrayList<>();
                                    notes.add(new ArticulatedSound(chord, $articulation.ctx == null ? Articulation.NONE : $articulation.art));
                                    $mel = new Melody(notes);
                                }
                            }
            ) STAR
            ( rhythm {$phr = $rhythm.rhy.createPhrase($mel);}
            | IDENTIFIER {$phr = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Rhythm.class).createPhrase($mel);}
            )
        )
        |
        ( IDENTIFIER {$phr = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Phrase.class).inOctave($options.getOctave());} )
    ;

block
locals [Block b, BlockOptions options]
    :   IDENTIFIER {$b = this.trackManager.getBlock($IDENTIFIER.text); $options  = new BlockOptions($b == null ? this.globalOptions : $b.getOptions());} blockOptions[$options]? BRACE_OPEN
            {
                if ($b == null)
                    $b = this.trackManager.createBlock($IDENTIFIER, this.timingEnv, this.midiSequence.createTrack(), $options);
                try {
                    $b.enterBlock($IDENTIFIER, $options);
                } catch (InvalidMidiDataException e) {
                    throw new ParseException($blockOptions.start, e.getMessage());
                }
            }
        (
            ( dynamicDeclaration    {
                                        try {
                                            $b.setDynamic($dynamicDeclaration.dynamic);
                                        } catch (InvalidMidiDataException e) {
                                            throw new ParseException($dynamicDeclaration.start, e.getMessage());
                                        }
                                    }
                ( DYNAMIC_CRES      {$b.crescendo($DYNAMIC_CRES);}
                | DYNAMIC_DECRES    {$b.decrescendo($DYNAMIC_DECRES);}
                )?
            )
        | phrase[$options]    {
                                            try {
                                                $b.addPhrase($phrase.phr);
                                            } catch (InvalidMidiDataException e) {
                                                throw new ParseException($dynamicDeclaration.start, e.getMessage());
                                            }
                                        }
        )* BRACE_CLOSE {$b.leaveBlock();}
    ;

song
    :   ( varDeclaration
        | block
        )*
    ;