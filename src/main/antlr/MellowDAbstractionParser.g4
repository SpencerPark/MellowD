/**
* Requires: type, name, expr, stmtList
* Defines: call, funcDecl, procDecl
*/
parser grammar MellowDAbstractionParser;

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
