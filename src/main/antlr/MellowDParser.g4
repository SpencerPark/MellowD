//Mellow D Parser
//==============

parser grammar MellowDParser;

//Import the token vocabulary described by the [*MellowD Lexer*](MellowDLexer.html)
options {
	tokenVocab = MellowDLexer;
}

//Declare all of the imports needed. These go in the class header
@header {
import io.github.spencerpark.mellowd.primitives.*;
import io.github.spencerpark.mellowd.intermediate.functions.operations.Comparable;
import java.util.LinkedList;
}

//Begin defining the parser rules.

//An identifier is a fully qualified name for an object
identifier
    :   IDENTIFIER ( DOT IDENTIFIER )*
    ;

directedNumber
returns [int amt]
    : ( PLUS | minus = MINUS ) NUMBER { $amt = $minus!= null ? -$NUMBER.int : $NUMBER.int; }
    ;

number
returns [int amt]
    : ( PLUS | minus = MINUS )? NUMBER { $amt = $minus != null ? -$NUMBER.int : $NUMBER.int; }
    ;

numberOrId : number | identifier ;

range
    : lower = numberOrId ( TILDA upper = numberOrId )?
    ;

index
    : ( COLON range )+
    ;

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
pitchRoot
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
note
    :   pitchRoot
        (   SHARP
        |   FLAT
        )?
        ( octaveShift = directedNumber )?
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
    :   note
    |   chord
    |   ( identifier | CHORD_IDENTIFIER ) index?
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
    :   (   note
        |   chord
        |   ( identifier | CHORD_IDENTIFIER ) index?
        ) articulation?
    |   STAR
    ;

//A rhythm char is a single char that is the first letter of the beat duration it is
//describing. The supported durations are whole, half, quarter, eight, sizteenth and
//thirty-second notes.
//A rhythmDef is the building block of a rhythm. It is a rhythm char followed by 0 or more `.`. Each
//dot extends the duration of the beat by half its value. This extension uses the previously
//added value in the calculation. Ex: `h..` is <sup>1</sup>&frasl;<sub>2</sub> + <sup>1</sup>&frasl;<sub>4</sub> + <sup>1</sup>&frasl;<sub>8</sub>
beat
returns [Beat b]
    :   (   W {$b = Beat.WHOLE();        }
        |   H {$b = Beat.HALF();         }
        |   Q {$b = Beat.QUARTER();      }
        |   E {$b = Beat.EIGHTH();       }
        |   S {$b = Beat.SIXTEENTH();    }
        |   T {$b = Beat.THIRTYSECOND(); }
        )
        ( dots += DOT )* { $b = $dots.isEmpty() ? $b : $b.dot($dots.size()); }
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
    :   num = NUMBER ( COLON div = NUMBER )?
        (   singleDivision = beat
        |   BRACKET_OPEN complexDivision += beat ( COMMA complexDivision += beat )* BRACKET_CLOSE
        )
    ;

//A `rhythmParam` takes any rhythm parameter and appends the appropriate beats to the rhythm it
//belongs to. The `slur` argument specifies if this parameter is slurred or not. Each option
//in this rule appends the appropriate beats to the rhythm.
rhythmParam[int slurDepth]
    :   beat
    |   slurredRhythm[$slurDepth]
    |   tuplet[$slurDepth]
    |   identifier index?
    ;

comparisonOperator
returns[Comparable.Operator op]
    :   KEYWORD_LT      {$op = Comparable.Operator.LT; }
    |   KEYWORD_LEQ     {$op = Comparable.Operator.LEQ;}
    |   KEYWORD_GT      {$op = Comparable.Operator.GT; }
    |   KEYWORD_GEQ     {$op = Comparable.Operator.GEQ;}
    |   KEYWORD_EQ      {$op = Comparable.Operator.EQ; }
    |   KEYWORD_NEQ     {$op = Comparable.Operator.NEQ;}
    ;

disjunction
    :   conjunction ( KEYWORD_OR conjunction )*
    ;

conjunction
    :   comparison ( KEYWORD_AND comparison )*
    ;

comparison
locals[List<Comparable.Operator> operators = new LinkedList<>()]
    :   value ( comparisonOperator value )*
    ;

value
    :   ( identifier | CHORD_IDENTIFIER ) index?
    |   chord
    |   melody
    |   rhythm[0]
    |   note
    |   beat
    |   number
    |   STRING
    |   KEYWORD_TRUE
    |   KEYWORD_FALSE
    |   KEYWORD_NOT? BRACE_OPEN disjunction BRACE_CLOSE
    |   KEYWORD_NOT value
    ;

ifStatement
    :   KEYWORD_DO codeBlock KEYWORD_IF disjunction
        ( KEYWORD_ELSE KEYWORD_DO codeBlock
            ( KEYWORD_IF disjunction KEYWORD_ELSE KEYWORD_DO codeBlock )*
        )?
    ;

//A variable declaration maps an identifier to a primitive. These primitives include [Chord](../primitives/Chord.html)
//, [Melody](../primitives/Melody.html), [Rhythm](../primitives/Rhythm.html) and [Phrase](../primitives/Phrase.html).
//Each mapping is put into the compiler's symbol table. Adding a `*` after the assignment token
//parses the value as if it was inside a percussion block.
varDeclaration
    :   KEYWORD_DEF? identifier ASSIGNMENT STAR? value
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
        ( ARROWS_LEFT | ARROWS_RIGHT )?
    ;

//Phrases are the finished product for a sequence of sounds. A phrase may be a pointer to a phrase
//variable or a pitch definition `*` a rhythm. A pitch definition may be a melody, chord, or a pointer
//to a melody or chord. The rhythm may be a direct rhythm declaration or a pointer to a rhythm.
phrase
    :   ( melody | melodyRef = identifier )
        STAR
        ( rhythm[0] | rhythmRef = identifier )
    ;

blockConfiguration
locals [Object configVal]
    :   IDENTIFIER COLON
        (   number          { $configVal = $number.amt; }
        |   STRING          { $configVal = $STRING.text.substring(1, $STRING.text.length()-1); }
        |   KEYWORD_TRUE    { $configVal = true; }
        |   KEYWORD_FALSE   { $configVal = false; }
        )
    ;

blockDeclaration
    :   KEYWORD_DEF KEYWORD_PERCUSSION? KEYWORD_BLOCK IDENTIFIER ( COMMA IDENTIFIER )*
        (   BRACE_OPEN
            blockConfiguration*
            BRACE_CLOSE
        )?
    ;

statement
    :   dynamicDeclaration
    |   phrase
    |   varDeclaration
    |   functionCall
    |   ifStatement
    |   ( NUMBER | identifier ) STAR codeBlock
    ;

importStatement
locals [
    List<String> funcNames = new LinkedList<>(),
    List<String> path = new LinkedList<>(),
    List<String> as = new LinkedList<>()
]
    :   KEYWORD_IMPORT
        (   IDENTIFIER              { $funcNames.add($IDENTIFIER.text); }
            (   COMMA IDENTIFIER    { $funcNames.add($IDENTIFIER.text); } )*
        |   STAR
        )

        KEYWORD_FROM
        IDENTIFIER          { $path.add($IDENTIFIER.text); }
        (   DOT IDENTIFIER  { $path.add($IDENTIFIER.text); } )*

        (   KEYWORD_AS
            IDENTIFIER          { $as.add($IDENTIFIER.text); }
            (   DOT IDENTIFIER  { $as.add($IDENTIFIER.text); } )*
        )?
    ;

//A block is a collection of phrases and dynamic declarations.
block
    :   IDENTIFIER ( COMMA IDENTIFIER )* codeBlock
    ;

codeBlock
    :   BRACE_OPEN
        statement*
        BRACE_CLOSE
    ;

argument
    :   ( IDENTIFIER COLON )? value?
    ;

functionCall
    :   KEYWORD_SAVE?
        BRACE_OPEN
        argument ( COMMA argument )*
        BRACE_CLOSE INTO identifier
    ;

parameter [boolean percussion]
returns [Primitives type]
    :   (   KEYWORD_CHORD     { $type = Primitives.CHORD; }
        |   KEYWORD_RHYTHM    { $type = Primitives.RHYTHM; }
        |   KEYWORD_MELODY    { $type = Primitives.MELODY; }
        |   KEYWORD_PHRASE    { $type = Primitives.PHRASE; }
        )?
        IDENTIFIER
        ( OPTIONAL ( ASSIGNMENT value )? )?
    ;

parameters [boolean percussion]
    :   parameter[$percussion] ( COMMA parameter[$percussion] )*
    ;

functionDefinition
    :   KEYWORD_DEF KEYWORD_PERCUSSION? KEYWORD_FUNCTION IDENTIFIER INTO parameters[$KEYWORD_PERCUSSION != null]? codeBlock
    ;

//A song is the top level rule, the entry point for the parser. At the top level only
//variable declarations or blocks can be defined. A song consists of any number of these
//declarations.
song
    :   importStatement*
        blockDeclaration*
        (   varDeclaration
        |   block
        |   functionDefinition
        )*
        EOF
    ;