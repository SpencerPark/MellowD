parser grammar MellowDParser;

import MellowDExpressionParser;

options {
	tokenVocab = MellowDLexer;
}

@header {
import org.mellowd.primitives.*;
import org.mellowd.intermediate.functions.operations.Comparable;
import java.util.LinkedList;
}

type
returns [Primitives primType]
    : KEYWORD_CHORD  { $primType = Primitives.CHORD; }
    | KEYWORD_RHYTHM { $primType = Primitives.RHYTHM; }
    | KEYWORD_MELODY { $primType = Primitives.MELODY; }
    | KEYWORD_PHRASE { $primType = Primitives.PHRASE; }
    ;

ifStmt
    : KEYWORD_IF expr KEYWORD_DO stmtList
      ( KEYWORD_ELSE
        ( KEYWORD_IF expr KEYWORD_ELSE KEYWORD_DO stmtList )*
        KEYWORD_DO stmtList
      )?
    ;

assignStmt
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
      ( ARROWS_LEFT | ARROWS_RIGHT )?
    ;

performStmt
    : KEYWORD_PERFORM BRACE_OPEN call BRACE_CLOSE
    | ( melody | melodyRef = name )
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

stmt
    : dynamicChangeStmt
    | performStmt
    | assignStmt
    | ifStmt
    | ( NUMBER | name ) STAR stmtList
    ;

importStmt
locals [
    List<String> funcNames = new LinkedList<>(),
    List<String> path = new LinkedList<>(),
    List<String> as = new LinkedList<>()
]
    : KEYWORD_IMPORT
      ( IDENTIFIER { $funcNames.add($IDENTIFIER.text); }
        ( COMMA IDENTIFIER { $funcNames.add($IDENTIFIER.text); } )*
      | STAR
      )

      KEYWORD_FROM
      IDENTIFIER { $path.add($IDENTIFIER.text); }
      ( DOT IDENTIFIER { $path.add($IDENTIFIER.text); } )*

      ( KEYWORD_AS
        IDENTIFIER { $as.add($IDENTIFIER.text); }
        ( DOT IDENTIFIER { $as.add($IDENTIFIER.text); } )*
      )?
    ;

block
    : IDENTIFIER ( COMMA IDENTIFIER )* stmtList
    ;

stmtList
    : BRACE_OPEN
      stmt*
      BRACE_CLOSE
    ;

song
    : importStmt*
      blockDeclStmt*
      ( assignStmt
      | block
      )*
      EOF
    ;