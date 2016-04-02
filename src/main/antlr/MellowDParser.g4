//Mellow D Parser
//==============

parser grammar MellowDParser;

//Import the token vocabulary described by the [*MellowD Lexer*](MellowDLexer.html)
options {
	tokenVocab = MellowDLexer;
}

//Declare all of the imports needed. These go in the class header
@header {
    import cas.cs4tb3.mellowd.*;
    import cas.cs4tb3.mellowd.midi.*;
    import cas.cs4tb3.mellowd.primitives.*;
    import javax.sound.midi.*;
    import java.util.*;
}

//The `@members` block declares code that should be defined inside the generated parser class.
@members {
    //The evaluation of everything depends on [BlockOptions](BlockOptions.html) and the global
    //options will serve as the options for variable evaluation.
    private BlockOptions globalOptions = new BlockOptions();

    //The symbol table will be a reference table for all variable declarations
    private SymbolTable symbolTable = new SymbolTable();

    //The timing environment is described in the compiler arguments and dictates the duration
    //of notes.
    private TimingEnvironment timingEnv;

    //The entire goal of the compiler is to build this `midiSequence`. This sequence stores the actual
    //MIDI data.
    private Sequence midiSequence;

    //The track manager is mainly used to instantiate [blocks](Block.html) but also manages these
    //blocks which wrap a MIDI track.
    private TrackManager trackManager;

    //An additional constructor (the default should not be used but we can't get rid of it)
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

    private int getOctaveShiftRequired(int target) {
        return target - this.globalOptions.getOctave();
    }

    //[Chords](../primitives/Chord.html) are resolved differently then a direct symbol table lookup.
    //There are a standard set of names for chords and we want to lookup these names as if they
    //exist in the symbol table without actually putting them in there.
    private Chord lookupChord(Token identifier, int octave) {
        //Preforming a type check will throw an exception if the identifier is defined for something
        //other than a chord. This means if the identifier is defined as a chord or is undefined
        //the program will continue.
        this.symbolTable.checkType(identifier, Chord.class);

        //The value in the table is pulled out of the table. If this value is not null then
        //a chord definition exists so we can simply put it in the correct octave and return it.
        Chord chord = this.symbolTable.getDeclarationValue(identifier.getText(), Chord.class);
        if (chord != null) return chord.shiftOctave(this.getOctaveShiftRequired(octave));

        //Otherwise the reference is not defined in the symbol table so try to resolve one of the
        //default chords based on the name.
        chord = Chord.resolve(identifier.getText(), octave);

        //If the chord is still null then it could not be resolved and we should therefor throw
        //an undefined reference exception.
        if (chord == null)
            throw new UndefinedReferenceException(identifier, "Identifier ("+identifier.getText()+") is undefined.");

        //Otherwise the resolution was successful and the chord can be returned.
        return chord;
    }

    //[Melody](../primitives/Melody.html) concatenation may be spimply adding a single note to the end of
    //the sequence or looking up a pointer to a melody and adding the entire melody to the root.
    private void appendToMelody(Melody root, BlockOptions context, MidiNoteMessageSource notes, Token identifier, Articulation articulation) {
        //Although the approach is not very clean, this method assumes that `notes == null` &rarr; `identifier != null`. This allows
        //this method to control how to resolve the code to add.
        if (notes == null) {
            //The notes are null so try and lookup the data pointed to by the identifier. If the type
            //of the pointer is a melody then get the data and append it to the root.
            if (this.symbolTable.identifierTypeIs(identifier.getText(), Melody.class)) {
                Melody mel = this.symbolTable.getDeclarationValue(identifier.getText(), Melody.class).shiftOctave(this.getOctaveShiftRequired(context.getOctave()));
                root.add(mel);
            //Otherwise attempt to resolve the identifier as a chord and add the chord as a single sound.
            } else {
                Chord chord = lookupChord(identifier, context.getOctave());
                root.add(new ArticulatedSound(chord, articulation));
            }
        //If the notes are not null then we have the simple case of appending a single sound
        //to the end of the root melody.
        } else {
            root.add(new ArticulatedSound(notes, articulation));
        }
    }

    private GeneralMidiPercussion resolvePercussionID(Token identifier, Token indexNum) {
        GeneralMidiPercussion drumSound = GeneralMidiPercussion.lookup(identifier.getText());
        //If the sound is not null then it was successfully resolved.
        if (drumSound != null) {
            //If NUMBER is not null and the percussion sound was resolved we have a semantic
            //issue. A single sound cannot be indexed.
            if (indexNum != null)
                throw new ParseException(indexNum, "Cannot index a percussion sound.");
        }
        return drumSound;
    }

    private MidiNoteMessageSource resolveChordID(Token identifier, Token indexNum, int octave) {
        Chord chord = lookupChord(identifier, octave);
        //If the indexNum is null there is no indexing and we can return the chord.
        if (indexNum == null) {
            return chord;
        //Otherwise attempt to pull a single pitch out of the chord.
        } else {
            //The indexNum is a valid int because the pattern
            //that is must match to become the NUMBER token is an int description.
            int index = Integer.parseInt(indexNum.getText());
            if (index > chord.size())
                throw new ParseException(identifier, "Index "+index+" is larger than the chord size of "+chord.size());
            return chord.getPitch(index);
        }
    }

    private void resolveChordParamID(List<Pitch> pitches, Token identifier, Token indexNum, int octave, boolean percussion) {
        //If inside a percussion block, first attempt to resolve the identifier as the
        //name of a percussion sound.
        if (percussion) {
            GeneralMidiPercussion drumSound = resolvePercussionID(identifier, indexNum);
            if (drumSound != null) {
                pitches.add(drumSound.getAsPitch());
                return;
            }
        }

        //If pitches is still null then it was not resolved to a percussion sound. Now whether
        //or not we are in a percussion block the next step is to try and resolve a chord from
        //the identifier.
        MidiNoteMessageSource noteSource = resolveChordID(identifier, indexNum, octave);
        if (noteSource instanceof Chord) pitches.addAll(((Chord) noteSource).getPitches());
        else                             pitches.add((Pitch) noteSource);
    }

    private void resolveMelodyParamID(Melody root, Token identifier, Token indexNum, Articulation art, int octave, boolean percussion) {
        //If inside a percussion block, first attempt to resolve the identifier as the
        //name of a percussion sound.
        if (percussion) {
            GeneralMidiPercussion drumSound = resolvePercussionID(identifier, indexNum);
            if (drumSound != null) {
                root.add(new ArticulatedSound(drumSound.getAsPitch(), art));
                return;
            }
        }

        //A percussion sound was not resolved so try and lookup the data pointed to by the identifier.
        //If the type of the pointer is a melody then get the data and append it to the root.
        if (this.symbolTable.identifierTypeIs(identifier.getText(), Melody.class)) {
            Melody mel = this.symbolTable.getDeclarationValue(identifier.getText(), Melody.class);
            mel = mel.shiftOctave(this.getOctaveShiftRequired(octave));
            root.add(mel);
            if (art != Articulation.NONE)
                throw new IncorrectTypeException(identifier, "Cannot articulate a melody with "+art.name().toLowerCase());
        //Otherwise attempt to resolve the identifier as a chord and add the chord as a single sound.
        } else {
            MidiNoteMessageSource noteSource = resolveChordID(identifier, indexNum, octave);
            root.add(new ArticulatedSound(noteSource, art));
        }
    }
}

//Begin defining the parser rules.

//An octaveShift moves the note up or down an number of octaves. It matches the
//pattern for a java integer so the string matched by this rule can be passed directly
//into `Integer.parseInt` and doesn't need to return anything.
octaveShift
    : ( PLUS | MINUS ) NUMBER;

//Articulation is a single articulation character. This rule returns the described
//[Articulation](../primitives/Articulation.html)
articulation
returns [Articulation art]
    : DOT           {$art = Articulation.STACCATO;      }
    | EXCLAMATION   {$art = Articulation.STACCATISSIMO; }
    | HAT           {$art = Articulation.MARCATO;       }
    | BACK_TICK     {$art = Articulation.ACCENT;        }
    | USCORE        {$art = Articulation.TENUTO;        }
    | TILDA         {$art = Articulation.GLISCANDO;     }
    ;

//A noteChar is a single lowercase letter from `A` to `G` that describes a ptich. This
//pitch is different depending on the octave so the resolution requires some [BlockOptions](BlockOptions.html)
//to obtain the octave for the current context.
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

//A noteDef fully describes a pitch. It consists of a `noteChar` optionally followed by a
//sharp or flat symbol and an optional `octaveShift`. This is the top level pitch resolution
//rule.
noteDef[BlockOptions options]
returns [Pitch pitch]
    :   ( noteChar[$options]    {$pitch = $noteChar.pitch;}
            (   SHARP           {$pitch = $pitch.sharp(); }
            |   FLAT            {$pitch = $pitch.flat();  }
            )?
            ( octaveShift {$pitch = $pitch.shiftOctave(Integer.parseInt($octaveShift.text));} )?
        )
    ;
//A `chord` definition is one or more `chordParam`s between `(` and `)` seperated by commas. A chord
//can be articulated which is the equivalent of articulating each pitch in the chord with the articulation.
//No individual note articulation is accepted.
chord[BlockOptions options]
returns [ArticulatedSound sound]
locals [List<Pitch> pitches = new LinkedList<>()]
    :   PAREN_OPEN
        chordParam[$options, $pitches] ( COMMA chordParam[$options, $pitches] )*
        PAREN_CLOSE {$sound = new ArticulatedSound(new Chord($pitches));}
        ( articulation {$sound.setArticulation($articulation.art);} )?
    ;

//A chord param can be a note or a pointer to another chord that is optionally indexed.
//As this param may consist of multiple pitches the rule returns a list of
//pitches. Chord may be indexed for their individual pitches so the order of the pitches is important
//and the list is the collection required to accomplish this.
chordParam[BlockOptions options, List<Pitch> pitches]
    :   noteDef[$options]
            { $pitches.add($noteDef.pitch); }
    |   IDENTIFIER ( COLON NUMBER )?
            { resolveChordParamID($pitches, $IDENTIFIER, $NUMBER, $options.getOctave(),
                $options.isPercussion()); }
    ;



//A `melody` is made up of 1 or more `melodyParam`s seperated by a comma. Each melodyParam is
//responsible for appending itself to the melody. The melody definition begins with a `[`
//and ends with a `]`.
melody[BlockOptions options]
returns [Melody mel = new Melody(new LinkedList<ArticulatedSound>())]
    :   BRACKET_OPEN
        melodyParam[$mel, $options] ( COMMA melodyParam[$mel, $options] )*
        BRACKET_CLOSE
    ;

//Each melody parameter is an articulated note, a chord, a pointer to a melody or chord,
//or a STAR for a rest. Depending on the option matched this rule may add one or many sounds to the
//melody. The `*` star character representing a rest.
melodyParam[Melody mel, BlockOptions options]
locals [Articulation art = Articulation.NONE]
    :   noteDef[$options] ( articulation {$art = $articulation.art;} )?
            { $mel.add(new ArticulatedSound($noteDef.pitch, $art)); }
    |   IDENTIFIER ( COLON NUMBER )? ( articulation {$art = $articulation.art;})?
            { this.resolveMelodyParamID($mel, $IDENTIFIER, $NUMBER, $art, $options.getOctave(),
                $options.isPercussion()); }
    |   chord[$options]
            { $mel.add($chord.sound); }
    |   STAR
            { $mel.add(new ArticulatedSound(Pitch.REST)); }
    ;

//A rhythm char is a single char that is the irst letter of the beat duration it is
//describing. The supported durations are whole, half, quarter, eight, sizteenth and
//thirty-second notes.
rhythmChar
returns [Beat beat]
    : W {$beat = Beat.WHOLE;        }
    | H {$beat = Beat.HALF;         }
    | Q {$beat = Beat.QUARTER;      }
    | E {$beat = Beat.EIGHTH;       }
    | S {$beat = Beat.SIXTEENTH;    }
    | T {$beat = Beat.THIRTYSECOND; }
    ;

//A rhythmDef is the building block of a rhythm. It is a rhythm char followed by 0 or more `.`. Each
//dot extends the duration of the beat by half its value. This extension uses the previously
//added value in the calculation. Ex: `h..` is <sup>1</sup>&frasl;<sub>2</sub> + <sup>1</sup>&frasl;<sub>4</sub> + <sup>1</sup>&frasl;<sub>8</sub>
rhythmDef
returns [Beat beat]
    : rhythmChar ( DOT )* {$beat = $DOT == null ? $rhythmChar.beat : $rhythmChar.beat.dot($DOT.text.length());};

//`rhythm` is the top level rhythm rule. It is a list of `rhythmParam`'s seperated by a comma opening
//with a `<` and closed with `>`. By default the `rhythmParam`'s inside this definition is
//not slurred, hence slur=false is passed into the rule call.
rhythm
returns [Rhythm rhy = new Rhythm()]
    : P_BRACKET_OPEN rhythmParam[$rhy, false] ( COMMA rhythmParam[$rhy, false] )* P_BRACKET_CLOSE ;

//A slurred rhythm is a regular rhythm inside `(` and `)`. It makes the melody glide over
//the rhythm without as distinct of a break as regular note performances. It looks the same as
//`rhythm` passing slur=true into the `rhythmParam` rule.
slurredRhythm[Rhythm rhy]
    : PAREN_OPEN rhythmParam[$rhy, true] ( COMMA rhythmParam[$rhy, true] )* PAREN_CLOSE ;

//A tuplet is a duration modification. The common tuplet being a triplet. A quarter note triplet
//performs 3 quarter notes in the same time that normally takes 2. A `5:3` quarter note tuplet
//performs 5 quarter notes in the time it takes to perform 3. If the second number in the ratio
//is not given it is assumed to be 1 less than the first. As such a numerator of [0, 1] or a
//denominator of [0] do not make any sense in this context.

//To slur the notes in the tuplet the `rhythmDef` is wrapped in `(` and `)`. Each tuplet can only consist
//of beats of the same duration so there is no reason to write the beat out multiple times. It is
//therefore only written once but adds `num` beats to the rhythm.
tuplet[Rhythm rhy]
    : num=NUMBER ( COLON div=NUMBER )?
        ( PAREN_OPEN rhythmDef PAREN_CLOSE
        | rhythmDef
        )
        {
            //Check the preconditions on the num and div
            if ($num.int == 0)
                throw new ParseException($num, "Tuplet number must be greater than 0");
            if ($div != null && $div.int == 0)
                throw new ParseException($div, "Tuplet division cannot be 0");

            //If the PAREN_OPEN is present then the tuplet is slurred
            boolean slur = $PAREN_OPEN != null;
            //Expand the beat to the number of beats in the numerator
            for (int i = 0; i < $num.int; i++) {
                Beat beat;
                //If the division is present use it in the duration calculation
                if ($div != null)
                    beat = $rhythmDef.beat.tuplet($num.int, $div.int);
                //Otherwise use the default (num only) duration calculation
                else
                    beat = $rhythmDef.beat.tuplet($num.int);
                //Append the beat to the rhythm
                rhy.append(beat, slur);
            }
        }
    ;

//A `rhythmParam` takes any rhythm parameter and appends the appropriate beats to the rhythm it
//belongs to. The `slur` argument specifies if this parameter is slurred or not. Each option
//in this rule appends the appropriate beats to the rhythm.
rhythmParam[Rhythm rhy, boolean slur]
    :   rhythmDef     { $rhy.append($rhythmDef.beat, $slur); }
    |   IDENTIFIER    {
                        Rhythm value = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Rhythm.class);
                        $rhy.append(value, $slur);
                      }
    |   slurredRhythm[$rhy]
    |   tuplet[$rhy]
    ;

//A variable declaration maps an identifier to a primitive. These primitives include [Chord](../primitives/Chord.html)
//, [Melody](../primitives/Melody.html), [Rhythm](../primitives/Rhythm.html) and [Phrase](../primitives/Phrase.html).
//Each mapping is put into the compiler's symbol table. Adding a `*` after the assignment token
//parses the value as if it was inside a percussion block.
varDeclaration
locals [boolean wasPercussion]
@init  {$wasPercussion = this.globalOptions.isPercussion();}
    : id=IDENTIFIER ASSIGNMENT ( STAR {this.globalOptions.setPercussion(true);} )?
                                ( ref=IDENTIFIER                {
                                                                    Object val = this.symbolTable.getDeclarationValue($ref.text, Object.class);
                                                                    if (val != null)
                                                                        this.symbolTable.addDeclaration($id.text, val);
                                                                    else
                                                                        this.symbolTable.addDeclaration($id.text, lookupChord($ref, this.globalOptions.getOctave()));
                                                                }
                                | chord [this.globalOptions]    {this.symbolTable.addDeclaration($id.text, $chord.sound.getSound());}
                                | melody[this.globalOptions]    {this.symbolTable.addDeclaration($id.text, $melody.mel);}
                                | rhythm                        {this.symbolTable.addDeclaration($id.text, $rhythm.rhy);}
                                | phrase[this.globalOptions]    {this.symbolTable.addDeclaration($id.text, $phrase.phr);}
                                ) {this.globalOptions.setPercussion($wasPercussion);}
    ;

//Dynamics are what change the velocity of a note. Mellow D supports the main dynamic identifiers
//with `pppp` being the quietest and `ffff` being the loundest.
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

//Block options are the configuration for the block. They appear after a block identifier inbetween
//`[` and `]`. Each configuration option is either a property option or a flag option. The entire
//configuration is a comma seperated list of these options.
blockOptions[BlockOptions options]
    : BRACKET_OPEN ( ( propertyOption[$options] | flagOption[$options] ) ( COMMA ( propertyOption[$options] | flagOption[$options] ) )* )? BRACKET_CLOSE;

//The first option type is a property option. This option assigns a value to a key. The value can
//be an identifier or a number.
propertyOption[BlockOptions options]
    : key=IDENTIFIER ASSIGNMENT ( valueID=IDENTIFIER | valueNum=NUMBER )
        {   switch($key.text.toLowerCase()) {
                //An instrument name or MIDI number can be specified to change the instrument
                //for this block.
                case "instrument":
                    if ($valueID != null) {
                        $options.setInstrument($valueID);
                    } else {
                        int inst = $valueNum.int;
                        if (inst > 127)
                            throw new ParseException($valueNum, "Instrument code must be less than 128.");
                        $options.setInstrument(inst);
                    }
                    break;
                //A soundbank can only be given by MIDI number. This option is not used in the majority
                //of cases but allows support for custom synthesiser banks
                case "soundbank":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected a soundbank id but found "+$valueID.text);
                    $options.setSoundbank($valueNum.int);
                    break;
                //The octave is the base octave for the block. All declarations inside a block are
                //relative to this octave. As an octave is a number, an identifier is not an
                //acceptable.
                case "octave":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected octave number but found "+$valueID.text);
                    int octave = $valueNum.int;
                    if (octave > 10) throw new ParseException($valueNum, "Octave is too high. 10 is highest byt found "+octave);
                    $options.setOctave(octave);
                    break;
                //Loop or repeat specifies how many times to repeat the contents of the block
                //in the compiled song.
                case "loop":
                case "repeat":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected a loop count but found "+$valueID.text);
                    $options.setLoopCount($valueNum.int);
                    break;
                //onchannel, samechannelas, sharechannel are all ways to say that this block should
                //operate on the same channel as another block by the given name. As all block names
                //are identifiers it does not make sense for this value to be a number.
                case "onchannel":
                case "samechannelas":
                case "sharechannel":
                    if ($valueID == null)
                        throw new ParseException($valueNum, "Expected the name of the block that this channel should share a channel with"
                            + " but found " + $valueNum.text);
                    $options.setShareChannel($valueID.text);
                    break;
                //channel is another option that is uncommon. It allows direct specification of the MIDI
                //channel number that this block should operate on. As it is a number, an identifier does
                //not make sense in this context.
                case "channel":
                    if ($valueNum == null)
                        throw new ParseException($valueID, "Expected the number of the MIDI channel to requests but"
                            + " found " + $valueID.text);
                    $options.setChannel($valueNum.int);
                    break;
                //If the key was not caught by another case it is an unknown option.
                default:
                    throw new ParseException($key, "Unknow block option " + $key.text + ".");
            }
        }
    ;

//A flag option is an on/off flag. If the flag exists it is in the on state. If it is prefixed
//with `-` it is set to the off state.
flagOption[BlockOptions options]
    :   ( off=MINUS )? flag=IDENTIFIER
        {
            switch ($flag.text.toLowerCase()) {
                //percussion or drums sets the block in percussion mode where it can accept
                //percussion sound names to describe pitches.
                case "percussion":
                case "drums":
                    $options.setPercussion($off == null);
                    break;
                //If none of the cases catch the flag then the option is undefined.
                default:
                    throw new ParseException($flag, "Unknow block option " + $flag.text + ".");
            }
        }
    ;

//Phrases are the finished product for a sequence of sounds. A phrase may be a pointer to a phrase
//variable or a pitch definition `*` a rhythm. A pitch definition may be a melody, chord, or a pointer
//to a melody or chord. The rhythm may be a direct rhythm declaration or a pointer to a rhythm.
phrase[BlockOptions options]
returns [Phrase phr]
locals [Melody mel]
    :   (   ( melody[$options]  {$mel = $melody.mel;}
            | chord[$options]   {$mel = new Melody(Arrays.asList($chord.sound));}
            | IDENTIFIER articulation?
                            {
                                //If the identifier points to a melody use it as the melody
                                if (this.symbolTable.identifierTypeIs($IDENTIFIER.getText(), Melody.class)) {
                                    $mel = this.symbolTable.getDeclarationValue($IDENTIFIER.getText(), Melody.class).shiftOctave(this.getOctaveShiftRequired($options.getOctave()));
                                    //A melody cannot be articulated so throw an exception if an articulation
                                    //was given.
                                    if ($articulation.ctx != null)
                                        throw new IncorrectTypeException($IDENTIFIER, "Cannot articulate a melody.");
                                //Otherwise attempt to resolve a chord. This resolution will throw an exception
                                //if it can't be resolved.
                                } else {
                                    Chord chord = lookupChord($IDENTIFIER, $options.getOctave());
                                    //Build a melody with a single element, the chord.
                                    List<ArticulatedSound> notes = Arrays.asList(new ArticulatedSound(chord, $articulation.ctx == null ? Articulation.NONE : $articulation.art));
                                    $mel = new Melody(notes);
                                }
                            }
            )
            STAR
            ( rhythm {$phr = $rhythm.rhy.createPhrase($mel);}
            //Try to resolve the identifier as a rhythm and create a phrase from it
            | IDENTIFIER {$phr = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Rhythm.class).createPhrase($mel);}
            )
        )
        |
        //The other option is to specify a variable name that points to a phrase
        ( IDENTIFIER {$phr = this.symbolTable.getDeclarationValueOrThrow($IDENTIFIER, Phrase.class).shiftOctave(this.getOctaveShiftRequired($options.getOctave()));} )
    ;

//A block is a collection of phrases and dynamic declarations.
block
locals [Block b, BlockOptions options]
    //The first step is to lookup the block with the given name ($IDENTIFIER). If the block doesn't exist
    //yet the block options used will be a copy of the global options. Otherwise the options will be
    //a copy of the block's options.
    :   IDENTIFIER {$b = this.trackManager.getBlock($IDENTIFIER.text); $options  = new BlockOptions($b == null ? this.globalOptions : $b.getOptions());} blockOptions[$options]? BRACE_OPEN
            {
                //If the block is null it must be created now with the parsed block options.
                if ($b == null)
                    $b = this.trackManager.createBlock($IDENTIFIER, this.timingEnv, this.midiSequence.createTrack(), $options);
                //Then enter the block
                try {
                    $b.enterBlock($IDENTIFIER, $options);
                } catch (InvalidMidiDataException e) {
                    throw new ParseException($blockOptions.start, e.getMessage());
                }
            }
        //Now that we are inside the block we expect to see `dynamicDeclaration`s or a `phrase`s.
        (
            //A dynamic declaration sets the dynaic for the block it is currently inside. If it is
            //directly followed by a crescendo or decrescendo token then the block dynamic
            //is notified that the change to the next token shuld be gradual
            dynamicDeclaration  {
                                    try {
                                        $b.setDynamic($dynamicDeclaration.dynamic);
                                    } catch (InvalidMidiDataException e) {
                                        throw new ParseException($dynamicDeclaration.start, e.getMessage());
                                    }
                                }
            ( DYNAMIC_CRES      {$b.crescendo($DYNAMIC_CRES);}
            | DYNAMIC_DECRES    {$b.decrescendo($DYNAMIC_DECRES);}
            )?
            //If a phrase is encountered it is added to the block which appends it to the track.
        | phrase[$options]    {
                                            try {
                                                $b.addPhrase($phrase.phr);
                                            } catch (InvalidMidiDataException e) {
                                                throw new ParseException($dynamicDeclaration.start, e.getMessage());
                                            }
                                        }
        )*

        //When the block is closed with the closing `}` the block is notified that no more data will
        //be incoming in this fragment.
        BRACE_CLOSE {$b.leaveBlock();}
    ;

//A song is the top level rule, the entry point for the parser. At the top level only
//variable declarations or blocks can be defined. A song consists of any number of these
//declarations.
song
    :   ( varDeclaration
        | block
        )*
        EOF {this.trackManager.finish();}
    ;