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

varlist             : VARDECSTART IDENTIFIER* BRACKETCLOSE ;

siglist             : SIGSTART fundec* BRACKETCLOSE ;

ruleslist           : RULEDECSTART trsrule* BRACKETCLOSE ;

typeorarity         : IDENTIFIER | IDENTIFIER* ARROW IDENTIFIER ;

fundec              : BRACKETOPEN IDENTIFIER typeorarity BRACKETCLOSE ;

trsrule             : term ARROW term logicalconstraint? ;

term                : IDENTIFIER
                    | IDENTIFIER BRACKETOPEN BRACKETCLOSE
                    | IDENTIFIER BRACKETOPEN termlist BRACKETCLOSE
                    ;

termlist            : term
                    | term COMMA termlist
                    ;

logicalconstraint   : SQUAREOPEN logicalterm SQUARECLOSE ;

logicalterm         : term
                    | NEGATION logicalterm
                    | logicalterm CONJUNCTION logicalterm
                    | logicalterm DISJUNCTION logicalterm
                    |<assoc=right> logicalterm CONDITIONAL logicalterm
                    | logicalterm BICONDITIONAL logicalterm
                    ;

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
                    ;

