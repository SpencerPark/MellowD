/**
* Requires: type, name, expr, stmtList
* Defines: call, funcDecl, procDecl
*/
parser grammar MellowDAbstractionParser;

parameter [boolean percussion]
    : type?
      IDENTIFIER
      ( OPTIONAL ( ASSIGN_R expr )? )?
    ;

parameterList [boolean percussion]
    : parameter[$percussion] ( COMMA parameter[$percussion] )*
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
      parameterList[$KEYWORD_PERCUSSION != null]?
      INTO_R
      stmtList
    ;

procDecl
    : KEYWORD_PROCEDURE
      KEYWORD_PERCUSSION?
      parameterList[$KEYWORD_PERCUSSION != null]?
      INTO_R
      stmtList
    ;
