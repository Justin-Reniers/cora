/**************************************************************************************************
 Copyright 2023 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package cora.lib.rewriting;

import cora.lib.exceptions.IllegalRuleError;
import cora.lib.exceptions.NullInitialisationError;
import cora.lib.exceptions.TypingError;
import cora.lib.terms.Term;
import cora.lib.terms.Variable;
import cora.lib.terms.VariableList;

public class RuleFactory {
  /** Prints a rule with the given left and right hand side, for use in error messages. */
  private static String toString(Term left, Term right) {
    return left.toString() + " → " + right.toString();
  }

  /**
   * Does the checks needed for all kinds of rules, and throws an appropriate erorr if the checks
   * are not satisfied.
   */
  private static void doBasicChecks(Term left, Term right) {
    // neither term is null
    if (left == null) throw new NullInitialisationError("Rule", "left-hand side");
    if (right == null) throw new NullInitialisationError("Rule", "right-hand side");
    // both sides should have the same type
    if (!left.queryType().equals(right.queryType())) {
      throw new TypingError("Rule", "constructor", "right-hand side", right.queryType().toString(),
                            left.queryType().toString());
    }
    // no variables should occur on the right that don't also occur on the left
    VariableList lvars = left.vars();
    VariableList rvars = right.vars();
    for (Variable x : rvars) {
      if (!lvars.contains(x)) {
        throw new IllegalRuleError("Rule", "right-hand side of rule [" + toString(left, right) +
          "] contains variable " + x.toString() + " which does not occur on the left.");
      }
    }
    // both sides should be closed; this is automatic if left is, since all variables occurring
    // free on the right are also free on the left
    if (!left.isClosed()) {
      throw new IllegalRuleError("Rule", "left-hand side of rule [" + toString(left, right) +
        "] is not closed (that is, freely contains a binder-variable).");
    }
  }

  /**
   * Create a first-order rule.
   * If the rule is poorly formed or not first-order, an IllegalRuleError is thrown.
   * (Poorly formed: FV(r) ⊆ FV(l) and both sides have the same sort.)
   */
  public static Rule createFirstOrderRule(Term left, Term right) {
    // do the checks that apply to everything, not just first-order rules
    doBasicChecks(left, right);
    // both sides need to be first-order
    if (!left.isFirstOrder() || !right.isFirstOrder()) {
      throw new IllegalRuleError("RuleFactory::createFirstOrderRule", "terms in rule [" +
        toString(left, right) + "] are not first-order.");
    }
    // the left-hand side should have the form f(...)
    if (!left.isFunctionalTerm()) {
        throw new IllegalRuleError("RuleFactory::createFirstOrderRule", "illegal rule [" +
          toString(left, right) + "] with a variable as the left-hand side.");
    }
    return new Rule(left, right);
  }

  /**
   * Create an applicative higher-order rule.
   * If the rule is poorly formed or not applicative, an IllegalRuleError is thrown.
   * (Poorly formed: FV(r) ⊆ FV(l) and both sides have the same sort.)
   */
  public static Rule createApplicativeRule(Term left, Term right) {
    // do the checks that apply to everything, not just first-order rules
    doBasicChecks(left, right);
    // both sides need to be applicative
    if (!left.isApplicative() || !right.isApplicative()) {
      throw new IllegalRuleError("RuleFactory::createApplicativeRule", "terms in rule [" +
        toString(left, right) + " are not applicative.");
    }
    return new Rule(left, right);
  }

  /**
   * Creates an AMS rule without limitations other than well-formedness: FV(r) ⊆ FV(l), both sides
   * should have the same sort, and both sides should be closed.
   */
  public static Rule createRule(Term left, Term right) {
    doBasicChecks(left, right);
    return new Rule(left, right);
  }
}
