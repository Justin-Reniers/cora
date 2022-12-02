/**************************************************************************************************
 Copyright 2019 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

/**
 * This is a lexer for files in the standard human-readable format of the termination or confluence
 * competitions, limited to unsorted and sorted first-order TRSs (.trs and .mstrs files).
 */
lexer grammar LcTrsLexer;

@header {
package cora.parsers;
}

/* Lexer */

WHITESPACE          : [ \t\r\n]+ -> skip ;

ARROW               : '-' '>' ;

EQUALITY            : '=' '=' ;

COMMA               : ',' ;

fragment A          : ('A' | 'a');
fragment B          : ('B' | 'b');
fragment C          : ('C' | 'c');
fragment D          : ('D' | 'd');
fragment E          : ('E' | 'e');
fragment F          : ('F' | 'f');
fragment G          : ('G' | 'g');
fragment H          : ('H' | 'h');
fragment I          : ('I' | 'i');
fragment J          : ('J' | 'j');
fragment K          : ('K' | 'k');
fragment L          : ('L' | 'l');
fragment M          : ('M' | 'm');
fragment N          : ('N' | 'n');
fragment O          : ('O' | 'o');
fragment P          : ('P' | 'p');
fragment Q          : ('Q' | 'q');
fragment R          : ('R' | 'r');
fragment S          : ('S' | 's');
fragment T          : ('T' | 't');
fragment U          : ('U' | 'u');
fragment V          : ('V' | 'v');
fragment W          : ('W' | 'w');
fragment X          : ('X' | 'x');
fragment Y          : ('Y' | 'y');
fragment Z          : ('Z' | 'z');

SIMPLIFICATION      : S I M P L I F I C A T I O N;

EXPANSION           : E X P A N S I O N;

DELETION            : D E L E T I O N;

POSTULATE           : P O S T U L A T E;

GENERALIZATION      : G E N E R A L I Z A T I O N;

GQDELETION          : G Q D E L E T I O N;

CONSTRUCTOR         : C O N S T R U C T O R;

DISPROVE            : D I S P R O V E;

COMPLETENESS        : C O M P L E T E N E S S;

// identifiers may not contain -> or ==, which we handle using lookaheads
IDENTIFIER          : ( (~ ([ \t\n\r\\()"|,-] | '=') ) |
                        ('-' {_input.LA(1) != '>'}?) |
                        ('=' {_input.LA(1) != '='}?)
                      )+ ;

BRACEOPEN           : '{' ;

BRACECLOSE          : '}' ;

BRACKETOPEN         : '(' ;

BRACKETCLOSE        : ')' ;

SQUAREOPEN          : '[' ;

SQUARECLOSE         : ']' ;


