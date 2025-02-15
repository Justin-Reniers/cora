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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import cora.exceptions.IndexingError;
import cora.exceptions.NullInitialisationError;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Position;
import cora.interfaces.rewriting.Alphabet;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;

/**
 * In the literature, an abstract rewriting system is a pair (A,→), where A is a set of terms and
 * → a (possibly non-deterministic) reduction relation on A; a TermRewritingSystem is an abstract
 * rewriting system of the form (Terms(F,V), →_R), where F is a set of typed function symbols, V a
 * (typically infinite) set of variables, and R a set of rewrite rules.
 */
public class TermRewritingSystem implements TRS {
  private Alphabet _alphabet;
  private List<Rule> _rules;
  private List<FunctionSymbol> _theorySymbols;

  /** Create an TermRewritingSystem with the given alphabet and rules. */
  public TermRewritingSystem(Alphabet alphabet, List<Rule> rules) {
    if (alphabet == null) throw new NullInitialisationError("TermRewritingSystem", "alphabet");
    if (rules == null) throw new NullInitialisationError("TermRewritingSystem", "rules set");
    for (int i = 0; i < rules.size(); i++) {
      if (rules.get(i) == null) {
        throw new NullInitialisationError("TermRewritingSystem", "rule " + i);
      }
    }

    _alphabet = alphabet.copy();
    _rules = new ArrayList<Rule>(rules);
    _theorySymbols = new ArrayList<>();
    for (FunctionSymbol f : alphabet.queryAlphabetSymbols()) {
      if (f.isTheorySymbol()) _theorySymbols.add(f);
    }
  }

  public TermRewritingSystem(TRS trs) {
    _alphabet = new UserDefinedAlphabet(trs.querySymbols());
    _rules = new ArrayList<>();
    for (int i = 0; i < trs.queryRuleCount(); i++) {
      _rules.add(trs.queryRule(i));
    }
    _theorySymbols = new ArrayList<>();
  }

  public TermRewritingSystem(TRS trs, ArrayList<FunctionSymbol> tSymbs) {
    _alphabet = new UserDefinedAlphabet(trs.querySymbols());
    _rules = new ArrayList<>();
    for (int i = 0; i < trs.queryRuleCount(); i++) {
      _rules.add(trs.queryRule(i));
    }
    _theorySymbols = new ArrayList<>();
    _theorySymbols.addAll(tSymbs);
  }

  /** Gives a human-readable representation of the term rewriting system. */
  public String toString() {
    String ret = _alphabet.toString() + "\n";
    for (int i = 0; i < _rules.size(); i++) {
      ret += _rules.get(i).toString() + "\n";
    }
    return ret;
  }

  /** Returns the number of rules in the system. */
  public int queryRuleCount() {
    return _rules.size();
  }

  /** For 0 <= index < queryRuleCount(), this returns one of the rules in the system. */
  public Rule queryRule(int index) {
    if (index < 0 || index >= queryRuleCount()) {
      throw new IndexingError("TermRewritingSystem", "queryRule", index, 0, queryRuleCount()-1);
    }
    return _rules.get(index);
  }

  /** Returns the corresponding symbol in the underlying alphabet (if any). */
  public FunctionSymbol lookupSymbol(String name) {
    return _alphabet.lookup(name);
  }

  public boolean isInfix(String name) {
    return _alphabet.infix(name);
  }

  /**
   * Returns the leftmost, innermost position where a rule may be applied, or null if no such
   * position exists.
   */
  public Position leftmostInnermostRedexPosition(Term s) {
    List<Position> positions = s.queryAllPositions();
    for (int i = 0; i < positions.size(); i++) {
      Position pos = positions.get(i);
      Term sub = s.querySubterm(pos);
      for (int j = 0; j < _rules.size(); j++) {
        if (_rules.get(j).applicable(sub)) return pos;
      }
    }
    return null;
  }

  /** 
   * Reduces the given term at the leftmost, innermost redex position, and returns the result;
   * if no such position exists, null is returned instead.
   * If multiple rules match, an arbitrary one is chosen.
   */
  public Term leftmostInnermostReduce(Term s) {
    List<Rule> tmp = new ArrayList<Rule>(_rules);
    Collections.shuffle(tmp);
    Position pos = leftmostInnermostRedexPosition(s);
    if (pos == null) return null;
    Term subterm = s.querySubterm(pos);
    for (int j = 0; j < tmp.size(); j++) {
      Term result = tmp.get(j).apply(subterm);
      if (result != null) return s.replaceSubterm(pos, result);
    }
    return null;
  }

  /**
   * Reduces the given term at all possible redex positions, and returns all results in a list;
   * if no such positions exist returns the empty list.
   */
  public List<Term> breadthFirstReduce(Term s) {
    List<Term> reductions = new ArrayList<>();
    List<Position> positions = s.queryAllPositions();
    for (int i = 0; i < positions.size(); i++) {
      Position pos = positions.get(i);
      Term sub = s.querySubterm(pos);
      List<Rule> tmp = new ArrayList<Rule>(_rules);
      for (int j = 0; j < tmp.size(); j++) {
        Term result = tmp.get(j).apply(sub);
        if (result != null) {
          reductions.add(s.replaceSubterm(pos, result));
        }
      }
    }
    return reductions;
  }

    @Override
    public void addRule(Rule r) {
        _rules.add(r);
    }

    @Override
    public List<FunctionSymbol> querySymbols() {
        return _alphabet.queryAlphabetSymbols();
    }

    @Override
    public List<FunctionSymbol> queryTheorySymbols() {return _theorySymbols;}
}

