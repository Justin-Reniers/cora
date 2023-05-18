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

import cora.exceptions.IllegalRuleError;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Environment;
import cora.interfaces.terms.Variable;
import cora.interfaces.rewriting.Rule;

/**
 * A FirstOrderRule is a rule l -> r where l and r are first-order terms of the same sort, l is not a
 * variable, and vars(r) ⊆ vars(l).
 */
public class FirstOrderRule extends RuleInherit implements Rule {
  /**
   * Creates a rule with the given left- and right-hand side.
   * If the types don't match, a TypingError is thrown.
   */
  private Term _constraint;

  public FirstOrderRule(Term left, Term right) {
    super(left, right);
    // both sides need to be first-order
    if (!left.isFirstOrder() || !right.isFirstOrder()) {
      throw new IllegalRuleError("FirstOrderRule", "terms in rule [" + left.toString() + " → " +
        right.toString() + "] are not first-order.");
    }
    // the right-hand side is not allowed to create new variables
    Environment lvars = left.vars();
    Environment rvars = right.vars();
    for (Variable x : rvars) {
      if (!lvars.contains(x)) {
        throw new IllegalRuleError("FirstOrderRule", "right-hand side of rule [" + left.toString() +
          " → " + right.toString() + "] contains variable " + x.toString() + " which does not " +
          "occur on the left.");
      }
    }
    // the left-hand side should have the form f(...)
    if (!left.isFunctionalTerm()) {
        throw new IllegalRuleError("FirstOrderRule", "illegal rule [" + left.toString() + " → " +
          right.toString() + "] with a variable as the left-hand side.");
    }
  }

  @Override
  public Term queryConstraint() {
    if (_constraint != null) return _constraint;
    return null;
  }

  public boolean applicable(Term t) {
    //TODO if constraint, throw error
    //if (_constraint != null) return false;
    return _left.match(t) != null;
  }

  public boolean applicable(Term t, Term c, Substitution y) {
    if (_constraint.queryRoot().queryName().equals("TRUE")) return true;
    if (_constraint.queryRoot().queryName().equals("FALSE")) return false;
    return _left.match(t) != null;
    //y = rewriteConstraint(_proof, _pos, _ruleIndex);
    //return y != null;
  }

  public Term apply(Term t) {
    Substitution subst = _left.match(t);
    if (subst == null) return null;
    return _right.substitute(subst);
  }

  public FirstOrderRule(Term left, Term right, Term _constraint) {
    super(left, right);
    this._constraint = _constraint;
    if (!left.isFirstOrder() || !right.isFirstOrder()) {
      throw new IllegalRuleError("FirstOrderRule", "terms in rule [" + left.toString() + " → " +
              right.toString() + "] {" + _constraint.toString()+ "} are not first-order." );
    }
    Environment lvars = left.vars();
    Environment rvars = right.vars();
    Environment cvars = _constraint.vars();
    for (Variable x : rvars) {
      if (!lvars.contains(x)) {
        throw new IllegalRuleError("FirstOrderRule", "");
      }
    }
    if (!left.isFunctionalTerm()) {
      throw new IllegalRuleError("FirstOrderRule", "");
    }
  }

  public String toString() {
    if (_constraint != null) {
      return _left.toString() + " → " + _right.toString() + " [" + _constraint.toString() +"]";
    }
    return _left.toString() + " → " + _right.toString();
  }

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof FirstOrderRule)) return false;
    FirstOrderRule r = (FirstOrderRule) o;
    return this._left.equals(r._left) && this._right.equals(r._right) && this._constraint.equals(r._constraint);
  }
}

