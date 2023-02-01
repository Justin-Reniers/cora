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

/** This is a parser for the standard .trs and .mstrs formats of the termination competition. */
parser grammar LcTrsParser;

@header {
package cora.parsers;
}

options {
  tokenVocab = LcTrsLexer;
}

trs                 : varlist? siglist? ruleslist EOF
                    | rewritinginduction EOF
                    ;

varlist             : BRACKETOPEN VARDECSTART IDENTIFIER* BRACKETCLOSE ;

siglist             : BRACKETOPEN SIGSTART fundec* BRACKETCLOSE ;

ruleslist           : BRACKETOPEN RULEDECSTART trsrule* BRACKETCLOSE ;

typeorarity         : IDENTIFIER | IDENTIFIER* ARROW IDENTIFIER ;

fundec              : BRACKETOPEN IDENTIFIER typeorarity BRACKETCLOSE ;

trsrule             : term ARROW term logicalconstraint? ;

term                : IDENTIFIER
                    | IDENTIFIER BRACKETOPEN BRACKETCLOSE
                    | IDENTIFIER BRACKETOPEN termlist BRACKETCLOSE
                    | factor term
                    | term (CONJUNCTION | DISJUNCTION) term
                    | <assoc=right> term CONDITIONAL term
                    | term BICONDITIONAL term
                    | term (MULT | DIV | MOD) term
                    | term PLUS term
                    | term (LT | LTEQ | GT | GTEQ)*? term
                    | term (EQUALITY | NEQ) term
                    ;

factor              : NEGATION term
                    | MINUS term
                    ;

termlist            : term
                    | term COMMA termlist
                    ;

logicalconstraint   : SQUAREOPEN term SQUARECLOSE ;

rewritinginduction  : SIMPLIFICATION
                    | EXPANSION
                    | DELETION
                    | POSTULATE
                    | GENERALIZATION
                    | GQDELETION
                    | CONSTRUCTOR
                    | DISPROVE
                    | COMPLETENESS
                    | CLEAR
                    | SWAP
                    ;

