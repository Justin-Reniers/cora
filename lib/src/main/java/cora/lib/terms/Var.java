/**************************************************************************************************
 Copyright 2019, 2022, 2023 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package cora.lib.terms;

import java.util.List;
import java.util.Map;
import java.util.Set;
import cora.lib.exceptions.InappropriatePatternDataError;
import cora.lib.exceptions.NullCallError;
import cora.lib.exceptions.NullInitialisationError;
import cora.lib.types.Type;
import cora.lib.types.TypeFactory;

/**
 * Variables are both used as parts of constraints, as generic expressions in terms, as binders
 * in an abstraction and as open spots for matching in rules; this class represents all those kinds
 * of variables.
 * Variables have a name for printing purposes, but are not uniquely defined by it (distinct
 * variables may have the same name and type, although this will typically be avoided within a
 * single term).  Rather, variables are uniquely identified by an internally kept index.  By
 * construction, no variables with an index greater than COUNTER can exist in the program.
 */
class Var extends LeafTermInherit implements Variable {
  private static int COUNTER = 0;
  private String _name;
  private int _index;
  private boolean _binder;

  /** Create a variable with the given name and type, and which is in Vbinder if binder is true. */
  Var(String name, Type type, boolean binder) {
    super(type);
    _name = name;
    _binder = binder;
    _index = COUNTER;
    COUNTER++;
    if (name == null) throw new NullInitialisationError("Var", "name");
    setVariables(new VarList(this));
  }

  /**
   * Create a variable without a name; a name will be automatically generated.
   * If binder is true, this variable will be marked as being in Vbinder; otherfise in Vnonb.
   */
  Var(Type type, boolean binder) {
    super(type);
    _name = "x[" + COUNTER + "]";
    _binder = binder;
    _index = COUNTER;
    COUNTER++;
  }

  /** @return true */
  public boolean isVariable() { return true; }

  /** @return true */
  public boolean isVarTerm() { return true; }

  /** @return false */
  public boolean isConstant() { return false; }

  /** @return false */
  public boolean isFunctionalTerm() { return false; }

  /** @return whether or not this variable is in Vbinder */
  public boolean isBinderVariable() {
    return _binder;
  }

  /** @return true if this is not a binder variable */
  public boolean isApplicative() {
    return !_binder;
  }

  /** @return true if the type is base and the variable is not in Vbinder */
  public boolean isFirstOrder() {
    return queryType().isBaseType() && !_binder;
  }

  /** Returns the name this variable was set up with, or renamed to. */
  public String queryName() {
    return _name;
  }

  /** @return an integer uniquely identifying this variable */
  public int queryVariableIndex() {
    return _index;
  }

  /** Appends the name of te variable to the builder. */
  public void addToString(StringBuilder builder, Map<Variable,String> renaming, Set<String> avoid) {
    if (renaming == null || !renaming.containsKey(this)) builder.append(_name);
    else builder.append(renaming.get(this));
  }

  /** @return this */
  public Variable queryVariable() {
    return this;
  }

  /** @throws InappropriatePatternDataError, as a variable does not have a function symbol root */
  public FunctionSymbol queryRoot() {
    throw new InappropriatePatternDataError("Var", "queryRoot", "functional terms");
  }

  /** @return gamma(x) if the current variable is x and x in dom(gamma), otherwise just x */
  public Term substitute(Substitution gamma) {
    if (gamma == null) throw new NullCallError("Var", "substitute", "substitution gamma");
    return gamma.getReplacement(this);
  }

  /**
   * This method updates gamma by adding the extension from x to the given other term, if x is not
   * yet mapped to anything.
   * If this works, then null is returned.
   * If x is already mapped to the given other term, then nothing is done but null is returned.
   * If x is mapped to a different term, then an explanation of the match failure is returned.
   * If other or gamma is null, then a NullCallError is thrown instead.
   */
  public String match(Term other, Substitution gamma) {
    if (other == null) throw new NullCallError("Var", "match", "other (matched term)");
    if (gamma == null) throw new NullCallError("Var", "match", "gamma (matching substitution");

    Term previous = gamma.get(this);

    if (previous == null) {
      if (!other.queryType().equals(queryType())) {
        return "Variable " + _name + " has a different type from " + other.toString() + ".";
      }
      gamma.extend(this, other);
      return null;
    }
    else if (previous.equals(other)) return null;
    else return "Variable " + _name + " mapped both to " + previous.toString() + " and to " +
      other.toString() + ".";
  }

  /**
   * Two variables are equal if and only if they share an index, and both binder or non-binder,
   * and have the same type.
   * Currently, this can only occur if they are the same object, but this may change in the future.
   */
  public boolean equals(Variable other) {
    return other.queryVariableIndex() == _index && queryType().equals(other.queryType()) &&
           other.isBinderVariable() == _binder;
  }

  /**
   * Alpha-equality of a variable to another variable holds if either mu[this] = xi[that], or both
   * mu[this] and xi[that] are undefined and they are the same Variable.
   */
  public boolean alphaEquals(Term term, Map<Variable,Integer> mu, Map<Variable,Integer> xi, int k) {
    if (!term.isVariable()) return false;
    if (mu.containsKey(this)) return mu.get(this).equals(xi.get(term.queryVariable()));
    else if (xi.containsKey(term.queryVariable())) return false;
    return equals(term.queryVariable());
  }

  /** Implements a total ordering on variables using the index. */
  public int compareTo(Variable other) {
    if (_index < other.queryVariableIndex()) return -1;
    if (_index > other.queryVariableIndex()) return 1;
    if (_binder && !other.isBinderVariable()) return -1;
    if (!_binder && other.isBinderVariable()) return 1;
    return queryType().toString().compareTo(other.queryType().toString());
  }
}
