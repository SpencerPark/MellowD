//Mellow D Parser
//==============

parser grammar MellowDParser;

//Import the token vocabulary described by the [*MellowD Lexer*](MellowDLexer.html)
options {
	tokenVocab = MellowDLexer;
}

//Declare all of the imports needed. These go in the class header
@header {
    import cas.cs4tb3.mellowd.primitives.*;
    import java.util.LinkedList;
}

//Begin defining the parser rules.
reference
    :   IDENTIFIER
    |   CHORD_IDENTIFIER
    ;

//An octaveShift moves the note up or down an number of octaves. It matches the
//pattern for a java integer so the string matched by this rule can be passed directly
//into `Integer.parseInt` and doesn't need to return anything.
octaveShift
returns [int amt]
    : ( PLUS | MINUS ) NUMBER { $amt = Integer.parseInt($ctx.getText()); };

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
noteChar
returns [Pitch pitch]
    : A     { $pitch = Pitch.A; }
    | B     { $pitch = Pitch.B; }
    | C     { $pitch = Pitch.C; }
    | D     { $pitch = Pitch.D; }
    | E     { $pitch = Pitch.E; }
    | F     { $pitch = Pitch.F; }
    | G     { $pitch = Pitch.G; }
    ;

//A noteDef fully describes a pitch. It consists of a `noteChar` optionally followed by a
//sharp or flat symbol and an optional `octaveShift`. This is the top level pitch resolution
//rule.
noteDef
    :   ( noteChar
            (   SHARP
            |   FLAT
            )?
            ( octaveShift )?
        )
        | reference COLON NUMBER
    ;

//A `chord` definition is one or more `chordParam`s between `(` and `)` seperated by commas. A chord
//can be articulated which is the equivalent of articulating each pitch in the chord with the articulation.
//No individual note articulation is accepted.
chord
    :   PAREN_OPEN params += chordParam ( COMMA params += chordParam )* PAREN_CLOSE
    ;

//A chord param can be a note or a pointer to another chord that is optionally indexed.
//As this param may consist of multiple pitches the rule returns a list of
//pitches. Chord may be indexed for their individual pitches so the order of the pitches is important
//and the list is the collection required to accomplish this.
chordParam
    :   noteDef
    |   reference ( COLON NUMBER )?
    ;

//A `melody` is made up of 1 or more `melodyParam`s seperated by a comma. Each melodyParam is
//responsible for appending itself to the melody. The melody definition begins with a `[`
//and ends with a `]`.
melody
    :   BRACKET_OPEN params += melodyParam ( COMMA params += melodyParam )* BRACKET_CLOSE
    ;

//Each melody parameter is an articulated note, a chord, a pointer to a melody or chord,
//or a STAR for a rest. Depending on the option matched this rule may add one or many sounds to the
//melody. The `*` star character representing a rest.
melodyParam
    :   (   noteDef
        |   chord
        |   reference
        ) articulation?
    |   STAR
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
    :   rhythmChar ( DOT )*
    ;

//`rhythm` is the top level rhythm rule. It is a list of `rhythmParam`'s seperated by a comma opening
//with a `<` and closed with `>`. By default the `rhythmParam`'s inside this definition is
//not slurred, hence slur=false is passed into the rule call.
rhythm[int slurDepth]
    : P_BRACKET_OPEN rhythmParam[$slurDepth] ( COMMA rhythmParam[$slurDepth] )* P_BRACKET_CLOSE ;

//A slurred rhythm is a regular rhythm inside `(` and `)`. It makes the melody glide over
//the rhythm without as distinct of a break as regular note performances. It looks the same as
//`rhythm` passing slur=true into the `rhythmParam` rule.
slurredRhythm[int slurDepth]
    : PAREN_OPEN rhythmParam[$slurDepth + 1] ( COMMA rhythmParam[$slurDepth + 1] )* PAREN_CLOSE ;

//A tuplet is a duration modification. The common tuplet being a triplet. A quarter note triplet
//performs 3 quarter notes in the same time that normally takes 2. A `5:3` quarter note tuplet
//performs 5 quarter notes in the time it takes to perform 3. If the second number in the ratio
//is not given it is assumed to be 1 less than the first. As such a numerator of [0, 1] or a
//denominator of [0] do not make any sense in this context.

//To slur the notes in the tuplet the `rhythmDef` is wrapped in `(` and `)`. Each tuplet can only consist
//of beats of the same duration so there is no reason to write the beat out multiple times. It is
//therefore only written once but adds `num` beats to the rhythm.
tuplet[int slurDepth]
locals [boolean parenPresent = false]
    :   num=NUMBER ( COLON div=NUMBER )?
        (   singleDivision = rhythmDef
        |   BRACKET_OPEN complexDivision += rhythmDef ( COMMA complexDivision += rhythmDef )* BRACKET_CLOSE
        )
    ;

//A `rhythmParam` takes any rhythm parameter and appends the appropriate beats to the rhythm it
//belongs to. The `slur` argument specifies if this parameter is slurred or not. Each option
//in this rule appends the appropriate beats to the rhythm.
rhythmParam[int slurDepth]
    :   rhythmDef
    |   reference
    |   slurredRhythm[$slurDepth]
    |   tuplet[$slurDepth]
    ;

value
    :   reference
    |   chord
    |   melody
    |   rhythm[0]
    |   phrase
    |   functionCall
    |   ( PLUS | MINUS )? NUMBER
    ;

//A variable declaration maps an identifier to a primitive. These primitives include [Chord](../primitives/Chord.html)
//, [Melody](../primitives/Melody.html), [Rhythm](../primitives/Rhythm.html) and [Phrase](../primitives/Phrase.html).
//Each mapping is put into the compiler's symbol table. Adding a `*` after the assignment token
//parses the value as if it was inside a percussion block.
varDeclaration
    :   IDENTIFIER ASSIGNMENT STAR? value
    ;

//Dynamics are what change the velocity of a note. Mellow D supports the main dynamic identifiers
//with `pppp` being the quietest and `ffff` being the loundest.

//A dynamic declaration sets the dynaic for the block it is currently inside. If it is
//directly followed by a crescendo or decrescendo token then the block dynamic
//is notified that the change to the next token shuld be gradual
dynamicDeclaration
locals [Dynamic dynamic]
    :   (   PPPP  { $dynamic = Dynamic.pppp; }
        |   PPP   { $dynamic = Dynamic.ppp;  }
        |   PP    { $dynamic = Dynamic.pp;   }
        |   P     { $dynamic = Dynamic.p;    }
        |   MP    { $dynamic = Dynamic.mp;   }
        |   MF    { $dynamic = Dynamic.mf;   }
        |   F     { $dynamic = Dynamic.f;    }
        |   FF    { $dynamic = Dynamic.ff;   }
        |   FFF   { $dynamic = Dynamic.fff;  }
        |   FFFF  { $dynamic = Dynamic.ffff; }
        )
        ( ARROWS_LEFT
        | ARROWS_RIGHT
        )?
    ;

//Phrases are the finished product for a sequence of sounds. A phrase may be a pointer to a phrase
//variable or a pitch definition `*` a rhythm. A pitch definition may be a melody, chord, or a pointer
//to a melody or chord. The rhythm may be a direct rhythm declaration or a pointer to a rhythm.
phrase
    :   (   (   melody
            |   (   chord
                |   reference
                )   art = articulation?
            )
            STAR
            (   rhythm[0]
            //Try to resolve the identifier as a rhythm and create a phrase from it
            |   rhythmRef = reference
            )
        )
    ;

blockDeclaration
    :   KEYWORD_DEF KEYWORD_PERCUSSION? IDENTIFIER
    ;

//A block is a collection of phrases and dynamic declarations.
block
locals [List<ParseTree> blockContents = new LinkedList<>()]
    :   IDENTIFIER ( COMMA IDENTIFIER )* BRACE_OPEN
        (   dynamicDeclaration  { $blockContents.add($dynamicDeclaration.ctx); }
        |   phrase              { $blockContents.add($phrase.ctx); }
        |   reference           { $blockContents.add($reference.ctx); }
        |   varDeclaration      { $blockContents.add($varDeclaration.ctx); }
        |   functionCall        { $blockContents.add($functionCall.ctx); }
        )*
        BRACE_CLOSE
    ;

functionCall
    :   IDENTIFIER BRACKET_OPEN value ( COMMA value )* BRACKET_CLOSE
    ;

//A song is the top level rule, the entry point for the parser. At the top level only
//variable declarations or blocks can be defined. A song consists of any number of these
//declarations.
song
    :   blockDeclaration*
        ( varDeclaration
        | block
        )*
        EOF
    ;