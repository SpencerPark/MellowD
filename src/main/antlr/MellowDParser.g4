parser grammar MellowDParser;

options {
	tokenVocab = MellowDLexer;
}

@header {
import org.mellowd.primitives.*;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.functions.operations.Comparable;
import java.util.LinkedList;
}

// A name is a (possibly) fully qualified identifier
name
    : IDENTIFIER ( DOT IDENTIFIER )*
    ;

// A directedNumber must start with a "direction" (PLUS or MINUS) where as a number
// may optionally contain that prefix.
directedNumber
returns [int amt]
    : ( PLUS | MINUS ) NUMBER { $amt = Integer.parseInt($text); }
    ;

number
returns [int amt]
    : ( PLUS | MINUS )? NUMBER { $amt = Integer.parseInt($text); }
    ;

numberOrId
    : number
    | name
    ;

range
    : lower = numberOrId ( TILDA upper = numberOrId )?
    ;

index
    : ( COLON range )+
    ;

articulation
returns [Articulation art]
    : DOT         { $art = Articulation.STACCATO;      }
    | EXCLAMATION { $art = Articulation.STACCATISSIMO; }
    | HAT         { $art = Articulation.MARCATO;       }
    | BACK_TICK   { $art = Articulation.ACCENT;        }
    | USCORE      { $art = Articulation.TENUTO;        }
    | TILDA       { $art = Articulation.GLISCANDO;     }
    ;

pitchRoot
returns [Pitch pitch]
    : A { $pitch = Pitch.A; }
    | B { $pitch = Pitch.B; }
    | C { $pitch = Pitch.C; }
    | D { $pitch = Pitch.D; }
    | E { $pitch = Pitch.E; }
    | F { $pitch = Pitch.F; }
    | G { $pitch = Pitch.G; }
    ;

note
    : pitchRoot
      ( SHARP | FLAT )?
      ( octaveShift = directedNumber )?
    ;

beat
returns [Beat b]
    : ( W { $b = Beat.WHOLE();        }
      | H { $b = Beat.HALF();         }
      | Q { $b = Beat.QUARTER();      }
      | E { $b = Beat.EIGHTH();       }
      | S { $b = Beat.SIXTEENTH();    }
      | T { $b = Beat.THIRTYSECOND(); }
      )
      ( dots += DOT )* { $b = $dots.isEmpty() ? $b : $b.dot($dots.size()); }
    ;


type
returns [Primitives primType]
    : KEYWORD_CHORD  { $primType = Primitives.CHORD; }
    | KEYWORD_RHYTHM { $primType = Primitives.RHYTHM; }
    | KEYWORD_MELODY { $primType = Primitives.MELODY; }
    | KEYWORD_PHRASE { $primType = Primitives.PHRASE; }
    ;

///////////////////////////////////////////////////////////////////////////////
// Chord literals
///////////////////////////////////////////////////////////////////////////////

//A `chord` definition is one or more `chordParam`s between `(` and `)` seperated by commas. A chord
//can be articulated which is the equivalent of articulating each pitch in the chord with the articulation.
//No individual note articulation is accepted.
chord
    : PAREN_OPEN
      ( params += chordParam ( COMMA params += chordParam )*
      | call
      )
      PAREN_CLOSE
    ;

//A chord param can be a note or a pointer to another chord that is optionally indexed.
//As this param may consist of multiple pitches the rule returns a list of
//pitches. Chord may be indexed for their individual pitches so the order of the pitches is important
//and the list is the collection required to accomplish this.
chordParam
    : note
    | ( name | CHORD_IDENTIFIER ) index?
    | chord
    ;

///////////////////////////////////////////////////////////////////////////////
// Melody literals
///////////////////////////////////////////////////////////////////////////////

//A `melody` is made up of 1 or more `melodyParam`s seperated by a comma. Each melodyParam is
//responsible for appending itself to the melody. The melody definition begins with a `[`
//and ends with a `]`.
melody
    : BRACKET_OPEN
      ( params += melodyParam ( COMMA params += melodyParam )*
      | call
      )
      BRACKET_CLOSE
    ;

//Each melody parameter is an articulated note, a chord, a pointer to a melody or chord,
//or a STAR for a rest. Depending on the option matched this rule may add one or many sounds to the
//melody. The `*` star character representing a rest.
melodyParam
    : ( note
      | chord
      | ( name | CHORD_IDENTIFIER ) index?
      ) articulation?
    | melody
    | STAR
    ;

///////////////////////////////////////////////////////////////////////////////
// Rhythm literals
///////////////////////////////////////////////////////////////////////////////

rhythm
    : P_BRACKET_OPEN
      ( params += rhythmParam ( COMMA params += rhythmParam )*
      | call
      )
      P_BRACKET_CLOSE
    ;

slurredRhythm
    : PAREN_OPEN params += rhythmParam ( COMMA params += rhythmParam )* PAREN_CLOSE
    ;

//A tuplet is a duration modification. The common tuplet being a triplet. A quarter note triplet
//performs 3 quarter notes in the same time that normally takes 2. A `5:3` quarter note tuplet
//performs 5 quarter notes in the time it takes to perform 3. If the second number in the ratio
//is not given it is assumed to be 1 less than the first. As such a numerator of [0, 1] or a
//denominator of [0] do not make any sense in this context.

//To slur the notes in the tuplet the `rhythmDef` is wrapped in `(` and `)`. Each tuplet can only consist
//of beats of the same duration so there is no reason to write the beat out multiple times. It is
//therefore only written once but adds `num` beats to the rhythm.
tuplet
    : num = NUMBER ( COLON div = NUMBER )?
      ( singleDivision = beat
      | BRACKET_OPEN complexDivision += beat ( COMMA complexDivision += beat )* BRACKET_CLOSE
      )
    ;

//A `rhythmParam` takes any rhythm parameter and appends the appropriate beats to the rhythm it
//belongs to. The `slur` argument specifies if this parameter is slurred or not. Each option
//in this rule appends the appropriate beats to the rhythm.
rhythmParam
    : beat
    | tuplet
    | rhythm
    | slurredRhythm
    | name index?
    ;

///////////////////////////////////////////////////////////////////////////////
// General expression
///////////////////////////////////////////////////////////////////////////////

comparisonOperator
returns[Comparable.Operator op]
    : KEYWORD_LT  { $op = Comparable.Operator.LT;  }
    | KEYWORD_LEQ { $op = Comparable.Operator.LEQ; }
    | KEYWORD_GT  { $op = Comparable.Operator.GT;  }
    | KEYWORD_GEQ { $op = Comparable.Operator.GEQ; }
    | KEYWORD_EQ  { $op = Comparable.Operator.EQ;  }
    | KEYWORD_NEQ { $op = Comparable.Operator.NEQ; }
    ;

atom
    : KEYWORD_TRUE
    | KEYWORD_FALSE
    | STRING
    | note
    | beat
    | number
    | chord
    | melody
    | rhythm
    | ( name | CHORD_IDENTIFIER ) index?
    | funcDecl
    | procDecl
    | BRACE_OPEN ( expr | call ) BRACE_CLOSE
    ;

// In order of highest precedence
expr
    : KEYWORD_NOT atom
    | atom ( comparisonOperator atom )+
    | expr KEYWORD_AND expr
    | expr KEYWORD_OR expr
    | atom
    ;

///////////////////////////////////////////////////////////////////////////////
// Abstractions
///////////////////////////////////////////////////////////////////////////////

parameter
    : type?
      IDENTIFIER
      ( OPTIONAL ( ASSIGN_R expr )? )?
    ;

parameterList
    : parameter ( COMMA parameter )*
    ;

argument
    : ( IDENTIFIER COLON )? expr?
    ;

argumentList
    : argument ( COMMA argument )*
    ;

call
    : name INTO_L argumentList
    | argumentList INTO_R name
    ;

funcDecl
    : KEYWORD_FUNCTION
      KEYWORD_PERCUSSION?
      parameterList?
      INTO_R
      stmtList
    ;

procDecl
    : KEYWORD_PROCEDURE
      KEYWORD_PERCUSSION?
      parameterList?
      INTO_R
      stmtList
    ;

///////////////////////////////////////////////////////////////////////////////
// Statements
///////////////////////////////////////////////////////////////////////////////

ifStmt
    : KEYWORD_IF expr doStmt
      ( KEYWORD_ELSE
        ( KEYWORD_IF expr KEYWORD_ELSE doStmt )*
        doStmt
      )?
    ;

// TODO perform  this type assertion
assignStmt
locals [QualifiedName id]
    : KEYWORD_DEF? type? name ASSIGN_R STAR? expr
    ;

dynamicChangeStmt
locals [Dynamic dynamic]
    : ( PPPP { $dynamic = Dynamic.pppp; }
      | PPP  { $dynamic = Dynamic.ppp;  }
      | PP   { $dynamic = Dynamic.pp;   }
      | P    { $dynamic = Dynamic.p;    }
      | MP   { $dynamic = Dynamic.mp;   }
      | MF   { $dynamic = Dynamic.mf;   }
      | F    { $dynamic = Dynamic.f;    }
      | FF   { $dynamic = Dynamic.ff;   }
      | FFF  { $dynamic = Dynamic.fff;  }
      | FFFF { $dynamic = Dynamic.ffff; }
      )
      ( ARROWS_L | ARROWS_R )?
    ;

performStmt
    : ( melody | melodyRef = name )
      STAR
      ( rhythm | rhythmRef = name )
    ;

blockConfigField
locals [Object configVal]
    : IDENTIFIER COLON
      ( number        { $configVal = $number.amt; }
      | STRING        { $configVal = $STRING.text.substring(1, $STRING.text.length()-1); }
      | KEYWORD_TRUE  { $configVal = true; }
      | KEYWORD_FALSE { $configVal = false; }
      )
    ;

blockDeclStmt
    : KEYWORD_DEF KEYWORD_PERCUSSION? KEYWORD_BLOCK IDENTIFIER ( COMMA IDENTIFIER )*
      ( BRACE_OPEN
        blockConfigField*
        BRACE_CLOSE
      )?
    ;

doStmt
    : KEYWORD_DO
      ( BRACE_OPEN call BRACE_CLOSE
      | stmtList
      | stmt
      )
    ;

onceStmt
    : KEYWORD_ONCE
      ( stmtList
      | stmt
      )
    ;

stmt
    : dynamicChangeStmt
    | performStmt
    | assignStmt
    | ifStmt
    | ( NUMBER | name ) STAR stmtList
    | doStmt
    | onceStmt
    ;

importStmt
    : KEYWORD_IMPORT
      ( funcs += name ( COMMA funcs += name )*
      | STAR
      )

      KEYWORD_FROM path = name

      ( KEYWORD_AS as = name )?
    ;

block
    : IDENTIFIER ( COMMA IDENTIFIER )* stmtList schedDirs?
    ;

// TODO get rid of the "sync link". That was an awful model for synchronization
schedDir
    : PERCENT ( ( size=number ( ARROWS_R offset=number )? ) | IDENTIFIER ) #SchedQuantize // Quantize on number of bars or come in with the block
    | ARROWS_R ( rhythm | beat )? PIPE #SchedAlign // align right
    | PIPE ( rhythm | beat )? ARROWS_L #SchedAlign
    | STAR number #SchedFinite // run number times
    | HAT number schedDirs? #SchedFinite  // run number times and resume infinite afterwards with potentially new scheduler directives
    ;

schedDirs
    : schedDir ( COMMA schedDir )*
    ;

stmtList
    : BRACE_OPEN
      stmt*
      BRACE_CLOSE
    ;

topLevelStmt
    : assignStmt
    | doStmt
    | block
    ;

song
    : importStmt*
      blockDeclStmt*
      topLevelStmt*
      EOF
    ;