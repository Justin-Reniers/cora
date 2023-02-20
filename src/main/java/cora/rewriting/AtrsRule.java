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

package cora.rewriting;

import java.util.ArrayList;
import cora.exceptions.NullInitialisationError;
import cora.exceptions.TypingError;
import cora.interfaces.types.Type;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Substitution;
import cora.interfaces.rewriting.Rule;

/**
 * AtrsRules are rules of the form l -> r, without restrictions on term formation.
 * (Restrictions may be added later when lambda abstraction is introduced).
 */
public class AtrsRule extends RuleInherit implements Rule {
  /**
   * Creates a rule with the given left- and right-hand side.
   * If the types don't match, a TypingError is thrown.
   */
  public AtrsRule(Term left, Term right) {
    super(left, right);
  }

  /**
   * If left * X1 *** Xk has the same type as t, then this function returns k; if no such k exists
   * -1 is returned instead . */
  private int findHeadAdditions(Term t) {
    Type mytype = queryType();
    Type histype = t.queryType();
    int k = 0;
    for (; mytype.isArrowType() && !mytype.equals(histype); k++) {
      mytype = mytype.queryArrowOutputType();
    }
    if (mytype.equals(histype)) return k;
    return -1;
  }

  /** Returns whether the left-hand side is a pattern. */
  public boolean isPatternRule() {
    return _left.isPattern();
  }

  /** A rule is applicable to a term if the left-hand side matches its head. */
  public boolean applicable(Term t) {
    int n = t.numberImmediateSubterms();
    int k = findHeadAdditions(t);
    if (k == -1) return false;
    Term head = t.queryImmediateHeadSubterm(n-k);
    return _left.match(head) != null;
  }

  public Term apply(Term t) {
    int n = t.numberImmediateSubterms();
    int k = findHeadAdditions(t);
    if (k == -1) return null;
    Term head = t.queryImmediateHeadSubterm(n-k);
    Substitution subst = _left.match(head);
    if (subst == null) return null;
    ArrayList<Term> args = new ArrayList<Term>();
    for (int i = n-k+1; i <= n; i++) args.add(t.queryImmediateSubterm(i));
    Term righthead = _right.substitute(subst);
    return righthead.apply(args);
  }

  /** Atrs Rule does not have constraints */
  @Override
  public Term queryConstraint() {
    return null;
  }

  public String toString() {
    return _left.toString() + " → " + _right.toString();
  }
}

