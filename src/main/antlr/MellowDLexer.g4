//MellowD Lexer
//=============

lexer grammar MellowDLexer;

channels {
    COMMENTS
}

KEYWORD_PERCUSSION : 'percussion' ;
KEYWORD_DEF : 'def' ;
KEYWORD_BLOCK : 'block' ;
KEYWORD_FUNCTION : 'func' ;
KEYWORD_PROCEDURE : 'proc' ;
KEYWORD_SAVE : 'save' ;
KEYWORD_IMPORT : 'import' ;
KEYWORD_FROM : 'from' ;
KEYWORD_AS : 'as' ;
KEYWORD_CHORD : 'chord' ;
KEYWORD_RHYTHM : 'rhythm' ;
KEYWORD_MELODY : 'melody' ;
KEYWORD_PHRASE : 'phrase' ;
KEYWORD_DO : 'do' ;
KEYWORD_ONCE : 'once' ;
KEYWORD_IF : 'if' ;
KEYWORD_ELSE : 'else' ;
KEYWORD_TRUE : 'true' | 'on' | 'yes' ;
KEYWORD_FALSE : 'false' | 'off' | 'no' ;

KEYWORD_LT : 'lt' ;
KEYWORD_LEQ : 'leq' ;
KEYWORD_GT : 'gt' ;
KEYWORD_GEQ : 'geq' ;
KEYWORD_EQ : 'eq' ;
KEYWORD_NEQ : 'neq' ;

KEYWORD_AND : 'and' ;
KEYWORD_OR : 'or' ;
KEYWORD_NOT : 'not' ;

//**Dynamics**: Volume control.
//The following rules are volume modifiers that when encountered change
//the loudness of the sound. This transition can become more gradual with
//the `DYNAMIC_CRES` and `DYNAMIC_DECRES`.
PPPP : 'pppp';
PPP  : 'ppp';
PP   : 'pp';
P    : 'p';
MP   : 'mp';
MF   : 'mf';
F    : 'f';
FF   : 'ff';
FFF  : 'fff';
FFFF : 'ffff';

//**Cresendo**: Gradually increase hte volume until at the volume of the next dynamic
ARROWS_L : '<<';

//**Decresendo**: Gradually decrease the volume until at the volume of the next dynamic
ARROWS_R : '>>';

//**Accents**: These characters apply *feel* or *style* to a note.
//`DOT` also has another meaning in a rhythm context which is to extend a
//rhythm element by 1/2 it's length.
DOT  : '.';
EXCLAMATION : '!';
HAT : '^';
BACK_TICK  : '`';
USCORE  : '_';
TILDA : '~';

//**Identifiers**: There are various single letter keywords in Mellow D for
//describing notes and beats. Notes are the standard a, b, c, d, e, f, and g strictly
//in lowercase. Rhythm units consist of w (whole), h (half), e (eighth), q (quarter),
//s (sixteenth) and t (thirtysecond) strictly in lowercase. They represent the standard
//beat durations.
//
//To eliminate the ambiguity in the `IDENTIFIER` the definition enforces that if the identifier
//starts with a note or whythm char, it is followed by at least one identifier char to distinguish
//it from the keyword. Similarily `ppp`, `pp`, `p`, `mp`, `mf`, `f`, `ff`, `fff` must also be
//distinguished from the `DYNAMIC_*` definitions by being followed by atleast one identifier char.
//
//These tokens must be described individually and placed in a parser rule due to the context

A : 'a';
B : 'b';
C : 'c';
D : 'd';
E : 'e';
/*F : 'f'; is already defined above with the dynamics*/
G : 'g';
H : 'h';
Q : 'q';
S : 's';
T : 't';
W : 'w';

NUMBER : [0-9]+;
CHORD_IDENTIFIER
    :   [A-G]
        ( '#' | '$' )?
        ( 'm'   ( 'aj'  ( '7'
                            (   ( 's'
                                | 'b'
                                ) '5'?
                            )?
                        | '6'
                        )?
                | 'in'
                    ( '6'
                    | '7'
                    | 'maj7'
                    )?
                )?
        | 'aug' '7'?
        | 'dim' '7'?
        | 'dom7'
        | '7'
        | '6'
        )?
        (
            ( '+' | '-' ) [0-9]+
        )?
    ;
IDENTIFIER
    :   ( [_i-lnoruvx-zA-Z]
        |   ( [a-hqstw]
            | ( 'm' [pf]? )
            | ( 'p' 'p'? 'p'? 'p'? )
            | ( 'f' 'f'? 'f'? 'f'? )
            ) ([a-zA-Z0-9] | '_' [a-zA-Z0-9])
        ) ([a-zA-Z0-9] | '_' [a-zA-Z0-9])*
    ;

PERCENT : '%';
PIPE : '|';

//Octave shift up and down respectively.
PLUS : '+';
MINUS : '-';

//`STAR` is an operator that distributes a melody over a rhythm.
STAR : '*';
//`COMMA` is a list seperation token
COMMA : ',';
//`COLON` is used for indexing
COLON : ':';
//`SHARP` and `FLAT` shift a note up and down by a semi-tone respectively
SHARP : '#';
FLAT : '$';
//`ASSIGNMENT` maps an identifier to a musical descriptor
ASSIGN_R : '->';
INTO_L : '<=' ;
INTO_R : '=>' ;
OPTIONAL : '?' ;

//**Definition Boundaries**: The following definitions are for tokens that mark the
//beginning and end of various definitions. They each have their respective match.
BRACKET_OPEN : '[';
BRACKET_CLOSE : ']';
PAREN_OPEN : '(';
PAREN_CLOSE : ')';
P_BRACKET_OPEN : '<';
P_BRACKET_CLOSE : '>';
BRACE_OPEN : '{';
BRACE_CLOSE : '}';

STRING : '"' ~('\r' | '\n' | '"')* '"' ;

//Skip comments. Line comments comment out all input untin the first matched
//newline. Mellow D also supports multi-line comments like java comments. `/*`
//opens the comment and `*/` closes the comment.
LINE_COMMENT : '//' ~[\r\n]* '\r'? '\n' -> channel(COMMENTS) ;
MULTI_LINE_COMMENT : '/*' ( MULTI_LINE_COMMENT | . )*? '*/' -> channel(COMMENTS) ;

//Ignore whitespace and pipes (|) as they can be used by the developer to format
//their source however they like.
SRC_SUGAR : [ \n\r\t] -> channel(HIDDEN);