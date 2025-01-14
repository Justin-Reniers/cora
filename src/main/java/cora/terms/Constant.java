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

package cora.terms;

import java.util.*;

import cora.exceptions.InappropriatePatternDataError;
import cora.exceptions.NullInitialisationError;
import cora.exceptions.NullCallError;
import cora.interfaces.types.Type;
import cora.interfaces.terms.*;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Constants are the default kind of FunctionSymbol.
 * They are called Constants because they can also be seen as constant terms.
 */
public class Constant extends LeafTermInherit implements FunctionSymbol {
  private String _name;
  private boolean _infix, _theory;
  private int _precedence;

  /**
   * A constant is always identified by the combination of its name and its type.
   * Throws an error if the name or type is null, or if name is the empty string.
   */
  public Constant(String name, Type type) {
    super(type);
    _name = name;
    if (name == null) throw new NullInitialisationError("Constant", "name");
    if (name.equals("")) throw new Error("Function Symbol created with empty name.");
    _infix = false;
    _theory = false;
  }

  public Constant(Constant c) {
    super(c.queryType());
    _name = c.queryName();
  }

  public Constant(String name, Type type, boolean theory, boolean infix, int precedence) {
    super(type);
    _name = name;
    if (name == null) throw new NullInitialisationError("Constant", "name");
    if (name.equals("")) throw new Error("Function Symbol created with empty name.");
    _infix = infix;
    _precedence = precedence;
    _theory = theory;
  }

  public Constant(String name, Type type, boolean theory, boolean infix) {
    super(type);
    _name = name;
    if (name == null) throw new NullInitialisationError("Constant", "name");
    if (name.equals("")) throw new Error("Function Symbol created with empty name.");
    _infix = infix;
    _theory = theory;
  }
  public Constant(String name, Type type, boolean infix) {
    super(type);
    _name = name;
    if (name == null) throw new NullInitialisationError("Constant", "name");
    if (name.equals("")) throw new Error("Function Symbol created with empty name.");
    _infix = infix;
  }

  public Constant(String name, Type type, boolean infix, int precedence) {
    super(type);
    _name = name;
    if (name == null) throw new NullInitialisationError("Constant", "name");
    if (name.equals("")) throw new Error("Function Symbol created with empty name.");
    _infix = infix;
    _precedence = precedence;
  }

  /** Returns the name of the current user-defined symbol. */
  public String queryName() {
    return _name;
  }

  /** Returns a string that describes the function symbol; the type is not indicated. */
  public String toString() {
    return _name;
  }

  /** Returns the number of terms this constant may (at most) be applied to. */
  public int queryArity() {
    return queryType().queryArity();
  }

  /**
   * Returns a string that uniquely identifies the function symbol (by combining its name and
   * type).
   */
  public String toUniqueString() {
    return _name + "{" + queryType().toString() + "}";
  }

  public boolean equals(FunctionSymbol symbol) {
    if (symbol == null) return false;
    if (!_name.equals(symbol.queryName())) return false;
    return queryType().equals(symbol.queryType());
  }

    @Override
    public boolean isTheorySymbol() {
        return _theory;
    }

    @Override
  public boolean isInfix() {
    return _infix;
  }

  @Override
  public int precedence() {
    return _precedence;
  }

  /** @return false */
  public boolean isVariable() { return false; }

  /** @return false */
  public boolean isVarTerm() { return false; }

  /** @return true */
  public boolean isConstant() { return true; }

  /** @return true */
  public boolean isFunctionalTerm() { return true; }

  /** Returns the current symbol f, which is the root of the corresponding term f(). */
  public FunctionSymbol queryRoot() {
    return this;
  }

  /** Throws an error, because a constant is not a variale (or associated with one). */
  public Variable queryVariable() {
    throw new InappropriatePatternDataError("Constant", "queryVariable",
                                            "variables or lambda-expressions");
  }

  /** Does nothing, since a function symbol does not use any variables. */
  public void updateVars(Environment env) {}

  /** Returns the FunctionalTerm f(args). */
  public Term apply(List<Term> args) {
    return new FunctionalTerm(this, args);
  }

  /** Returns the current constant unmodified (there is nothing to substitute in a constant). */
  public Term substitute(Substitution gamma) {
    return this;
  }

  /**
   * This method checks that other is the same constant. If so, null is returned, otherwise a
   * description of the instantiation failure.
   */
  public String match(Term other, Substitution gamma) {
    if (other == null) throw new NullCallError("Constant", "match", "other term");
    if (equals(other)) return null;
    return "constant " + _name + " is not instantiated by " + other.toString() + ".";
  }

  /** This method verifies equality to another Term. */
  public boolean equals(Term term) {
    if (term == null) return false;
    if (!term.isConstant()) return false;
    return equals(term.queryRoot());
  }

  /** Hashcode for Varterms based on the Variable */
  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  /** Applies the unification algorithm between Variable and another term */
  public Substitution unify(Term other) {
    if (other.isConstant() && !this.equals(other)) return null; // Conflict
    if (this.equals(other)) return new Subst(); // Delete
    if (other.isVariable()) { //Swap
      return other.unify(this);
    }
    return null;
  }

  public static boolean isNumeric(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch(NumberFormatException e){
      return false;
    }
  }

    @Override
    public String toHTMLString() {
      if (this._theory || isNumeric(this._name.toString())) {
        return "<font color=blue>" + StringEscapeUtils.escapeHtml4(_name) + "</font>";
      }
      //if (this.isConstant()) return "<font color=blue>" + StringEscapeUtils.escapeHtml4(_name) + "</font>";
      return "<font color=red>" + StringEscapeUtils.escapeHtml4(_name) + "</font>";
    }

  @Override
  public boolean isTheoryTerm(Term constraint) {
    return this._theory;
  }

  @Override
  public TreeSet<Variable> getVars() {
    return new TreeSet<>();
  }
}

