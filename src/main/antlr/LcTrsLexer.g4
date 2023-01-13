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
import LexerFragments;

@header {
package cora.parsers;
}

/* Lexer */

WHITESPACE          : [ \t\r\n]+ -> skip ;

ARROW               : '-' '>' ;

EQUALITY            : '=' '=' ;

COMMA               : ',' ;



/* Logical Operators */

NEGATION            : '~' ;

CONJUNCTION         : '/' '\\' ;

DISJUNCTION         : '\\' '/' ;

CONDITIONAL         : '-' '-' '>' ;

BICONDITIONAL       : '<' '-' '-' '>' ;

BRACEOPEN           : '{' ;

BRACECLOSE          : '}' ;

BRACKETOPEN         : '(' ;

BRACKETCLOSE        : ')' ;

SQUAREOPEN          : '[' ;

SQUARECLOSE         : ']' ;

VARDECSTART         : '(' 'V' 'A' 'R' ;

SIGSTART            : '(' 'S' 'I' 'G' ;

RULEDECSTART       : '(' 'R' 'U' 'L' 'E' 'S' ;

/* Rewriting Induction Rules */

SIMPLIFICATION      : S I M P L I F Y;

EXPANSION           : E X P A N D;

DELETION            : D E L E T E;

POSTULATE           : P O S T U L A T E;

GENERALIZATION      : G E N E R A L I Z E;

GQDELETION          : G Q D E L E T E;

CONSTRUCTOR         : C O N S T R U C T O R;

DISPROVE            : D I S P R O V E;

COMPLETENESS        : C O M P L E T E N E S S;

CLEAR               : C L E A R;

IDENTIFIER          : ( (~ ([[ \t\n\r\\()"|{}\],] | '=') ) |
                        ('-' {_input.LA(1) != '>'}?) |
                        ('=' {_input.LA(1) != '='}?)
                      )+ ;
