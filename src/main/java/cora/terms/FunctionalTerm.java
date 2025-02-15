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
import cora.exceptions.IndexingError;
import cora.exceptions.NullCallError;
import cora.interfaces.types.Type;
import cora.interfaces.terms.*;

/**
 * FunctionalTerms are terms of the form f(s1,...,sn) where s1,...,sn are all terms and f is a
 * function symbol.
 * Here, n may be 0, but both n and the types of s1,...,sn are restricted by the type of f. 
 * Application of functions must take typing into account.
 */
public class FunctionalTerm extends ApplicativeTermInherit implements Term {
  private FunctionSymbol _f;

  /**
   * This constructor is used to create a term which takes one argument.
   * Throws an error if the constant is null or does not have arity 1, or the argument is null.
   */
  public FunctionalTerm(FunctionSymbol f, Term arg) {
    super(f, arg);
    _f = f;
  }

  /**
   * This constructor is used to create a term which takes two arguments.
   * Throws an error if the constant does not have arity 2, or one of the arguments is null.
   */
  public FunctionalTerm(FunctionSymbol f, Term arg1, Term arg2) {
    super(f, arg1, arg2);
    _f = f;
  }

  /**
   * This constructor is used to create a term f(s1,...,sn) with n >= 0.
   * Throws an error if n does not match the arity of f, or if the types of the arguments are not
   * the expected input types of f.
   */
  public FunctionalTerm(FunctionSymbol f, List<Term> args) {
    super(f, args);
    _f = f;
  }

  /**
   * This constructor is used to create a term f(s1,...,sn) with n >= 0 and the given output type,
   * without any checks of for instance type correctness being done.
   * Moreover, the array list args becomes the property of the new term, and may not be modified
   * afterwards.
   * Only for features like substitution or simplifications. Be very careful using this!
   */
  private FunctionalTerm(List<Term> args, FunctionSymbol f, Type outputType) {
    _f = f;
    _args = args;
    _outputType = outputType;
  }

  private FunctionalTerm(FunctionalTerm f) {
    _f = f.queryRoot();
    for (int i = 0; i < f.numberImmediateSubterms(); i++) {
      if (f.queryImmediateSubterm(i).isVariable()) _args.add(new Var((Var) f.queryImmediateSubterm(i)));
      else if (f.queryImmediateSubterm(i).isFunctionalTerm()) {
        _args.add(new FunctionalTerm((FunctionalTerm) f.queryImmediateSubterm(i)));
      } else if (f.queryImmediateSubterm(i).isConstant()) {
        _args.add(new Constant((Constant) f.queryImmediateSubterm(i)));
      }
    }
    _outputType = f.queryType();
  }

  /** This method is called by inherited functions, and calls the private constructor. */
  protected FunctionalTerm reconstruct(List<Term> args) {
    return new FunctionalTerm(args, _f, _outputType);
  }

  /** @return fakse */
  public boolean isVariable() {
    return false;
  }

  /** @return true iff the number of arguments is 0 */
  public boolean isConstant() {
    return _args.size() == 0;
  }

  /** @return false */
  public boolean isVarTerm() {
    return false;
  }

  /** @return true */
  public boolean isFunctionalTerm() {
    return true;
  }

  /** For a term f(s1,...,sn), this returns f. */
  public FunctionSymbol queryRoot() {
    return _f;
  }

  /** Throws an error, because a functional term is not associated with a variable. */
  public Variable queryVariable() {
    throw new InappropriatePatternDataError("FunctionalTerm", "queryVariable",
                                            "variables or lambda-expressions");
  }

  /** For a term f(s1,...,sn), this returns f(s1,...,si). */
  public Term queryImmediateHeadSubterm(int i) {
    if (i < 0 || i > _args.size()) {
      throw new IndexingError("FunctionalTerm", "queryImmediateHeadSubterm", i, 0, _args.size());
    }   
    if (i == 0) return _f;
    List<Term> newargs = new ArrayList<Term>();
    for (int j = 0; j < i; j++) newargs.add(_args.get(j));
    return new FunctionalTerm(_f, newargs);
  }

  /** Returns whether (a) this term has base type, and (b) all its arguments are first-order. */
  public boolean isFirstOrder() {
    if (!_outputType.isBaseType()) return false;
    for (int i = 0; i < _args.size(); i++) {
      if (!_args.get(i).isFirstOrder()) return false;
    }
    return true;
  }

  /** Returns whether all immediate arguments are patterns. */
  public boolean isPattern() {
    for (int i = 0; i < _args.size(); i++) {
      if (!_args.get(i).isPattern()) return false;
    }
    return true;
  }

  /** This adds the variables that occur freely in the current term into env. */
  public void updateVars(Environment env) {
    for (int i = 0; i < _args.size(); i++) {
      _args.get(i).updateVars(env);
    }
  }

  /** If this term is f(s1,...,sn) and extra = [t1,...,tm], this returns f(s1,...,sn,t1,...,tm). */
  public Term apply(List<Term> extra) {
    List<Term> newargs = new ArrayList<Term>(_args);
    newargs.addAll(extra);
    return new FunctionalTerm(_f, newargs);
  }

  /** This function applies gamma recursively on the arguments and returns the result. */
  public Term substitute(Substitution gamma) {
    List<Term> newargs = substituteArgs(gamma);
    return reconstruct(newargs);
  }

  /** 
   * This method checks that other has the same root symbol as we do, and if so, that all the
   * parameters match (updating the substitution as we go along).
   * If everything matches, null is returned; otherwise a description of the instantiation failure.
   */
  public String match(Term other, Substitution gamma) {
    if (other == null) throw new NullCallError("FunctionalTerm", "match", "argument term (other)");
    if (!other.isFunctionalTerm() || !_f.equals(other.queryRoot()) ||
        _args.size() != other.numberImmediateSubterms()) {
      return "functional term " + toString() + " is not instantiated by " + other.toString() + ".";
    }   
    for (int i = 0; i < _args.size(); i++) {
      String warning = _args.get(i).match(other.queryImmediateSubterm(i+1), gamma);
      if (warning != null) return warning;
    }
    return null;
  }

  /** This method gives a string representation of the term. */
  public String toString() {
    String ret = "";
    if (_args.size() == 0) return _f.toString();
    else if (_args.size() == 1 && _f.isInfix() && _args.get(0).numberImmediateSubterms() <= 1) {
      ret += _f.toString() + _args.get(0);
    }
    else if (_args.size() == 2 && _f.isInfix()) {
      if (_f.isInfix()) {
        Term l = _args.get(0);
        Term r = _args.get(1);
        if (l.isFunctionalTerm() && _f.precedence() < l.queryRoot().precedence()) {
          ret += "(" + l + ")";
        } else ret += l;
        ret += _f.toString();
        if (r.isFunctionalTerm() && _f.precedence() < r.queryRoot().precedence()) {
          ret += "(" + r + ")";
        } else ret += r;
      }
    }
    else {
        ret += _f.toString() + "(" + _args.get(0).toString();
        for (int i = 1; i < _args.size(); i++) ret += ", " + _args.get(i).toString();
        ret += ")";
    }
    return ret;
  }

  /** This method verifies equality to another Term. */
  public boolean equals(Term term) {
    if (term == null) return false;
    if (!term.isFunctionalTerm()) return false;
    if (!_f.equals(term.queryRoot())) return false;
    if (_args.size() != term.numberImmediateSubterms()) return false;
    for (int i = 0; i < _args.size(); i++) {
      if (!_args.get(i).equals(term.queryImmediateSubterm(i+1))) return false;
    }
    return true;
  }

  /**
   *
   */
  public Substitution unify(Term other) {
    if (other.isVariable()) {
      if (this.vars().contains(other.queryVariable())) return null;
      return other.unify(this);
    }
    if (_f.equals(other.queryRoot())) {
      Subst sigma = new Subst();
      for (int i = 0; i < _args.size(); i++) {
        Substitution sigma_i = _args.get(i).substitute(sigma).unify(other.queryImmediateSubterm(i+1));
        if (sigma_i == null) return null;
        for (Variable x : sigma_i.domain()) {
          Term sigma_i_x = sigma_i.get(x).substitute(sigma);
          if (!sigma.extend(x, sigma_i_x) && sigma.get(x) != null && !sigma.get(x).equals(sigma_i_x)) return null;
          other = other.substitute(sigma);
          for (Variable v : new HashSet<>(sigma.domain())) {
            if (sigma.getReplacement(v).vars().contains(x)) {
              Term replacement = sigma.getReplacement(v);
              sigma.delete(v);
              sigma.extend(v, replacement.substitute(new Subst(x, sigma_i_x)));
            }
          }
        }
      }
      return sigma;
    }
    return null;
  }

    @Override
    public String toHTMLString() {
      String ret = "";
      if (_args.size() == 0) return _f.toHTMLString();
      else if (_args.size() == 1 && _f.isInfix() && _args.get(0).numberImmediateSubterms() <= 1) {
        ret += _f.toHTMLString() + _args.get(0).toHTMLString();
      }
      else if (_args.size() == 2 && _f.isInfix()) {
        if (_f.isInfix()) {
          Term l = _args.get(0);
          Term r = _args.get(1);
          if (l.isFunctionalTerm() && _f.precedence() < l.queryRoot().precedence()) {
            ret += "(" + l.toHTMLString() + ")";
          } else ret += l.toHTMLString();
          if (_f.queryRoot().toString().equals("+") && r.isFunctionalTerm() && r.queryRoot().toString().equals("-")) {
            if (r.isFunctionalTerm() && _f.precedence() < r.queryRoot().precedence()) {
              ret += "(" + r.toHTMLString() + ")";
            } else ret += r.toHTMLString();
          } else {
            ret += _f.toHTMLString();
            if (r.isFunctionalTerm() && _f.precedence() < r.queryRoot().precedence()) {
              ret += "(" + r.toHTMLString() + ")";
            } else ret += r.toHTMLString();
          }
        }
      }
      else {
        ret += _f.toHTMLString() + "(" + _args.get(0).toHTMLString();
        for (int i = 1; i < _args.size(); i++) ret += ", " + _args.get(i).toHTMLString();
        ret += ")";
      }
      return ret;
    }

  @Override
  public boolean isTheoryTerm(Term constraint) {
    if (this.isFunctionalTerm() && !this._f.isTheoryTerm(constraint)) return false;
    else if (this.isFunctionalTerm() && this.isConstant() && !this._f.isTheoryTerm(constraint)) return false;
    else {
      for (int i = 1; i <= this.numberImmediateSubterms(); i++) {
        if (!this.queryImmediateSubterm(i).isTheoryTerm(constraint)) return false;
      }
    }
    return true;
  }

  @Override
  public TreeSet<Variable> getVars() {
    TreeSet<Variable> vars = new TreeSet<>();
    for (int i = 1; i <= this.numberImmediateSubterms(); i++) {
      vars.addAll(this.queryImmediateSubterm(i).getVars());
    }
    return vars;
  }

    /** Hashcode for FunctionalTerm based on its root symbol and its arguments */
  @Override
  public int hashCode() {
    return Objects.hash(_f, _args);
  }
}

