package cora.usercommands;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Simplify is a user command that tries to simplify terms on the left hand side of
 * an equivalence proof by reducing said terms with rewriting rules from the
 * corresponding LCTRS, or by simplifying terms of the left hand side, right hand side
 * or constraint to easier to read terms or substituting known assignments of variables
 * in the constraint (e.g. 1+1 -> 2 or replacing all occurrences of x+1 in lhs / rhs of
 * if constraint contains term a = x+1).
 */
public class SimplifyCommand extends UserCommandInherit implements UserCommand {

    private Position _pos;
    private int _ruleIndex;
    private boolean _noArgs;

    private Substitution _gamma;
    private EquivalenceProof _proof;

    public SimplifyCommand(Position pos, int ruleIndex, Substitution gamma) {
        super();
        _pos = pos;
        _ruleIndex = ruleIndex - 1;
        _noArgs = false;
        _gamma = gamma;
    }

    public SimplifyCommand(Position pos, int ruleIndex) {
        super();
        _pos = pos;
        _ruleIndex = ruleIndex - 1;
        _noArgs = false;
        _gamma = null;
    }

    public SimplifyCommand() {
        super();
        _pos = null;
        _ruleIndex = -1;
        _noArgs = true;
        _gamma = null;
    }

    /**
     * Returns the position at which the simplify command should simplify if the position is not
     * null. Returns null otherwise.
     */
    @Override
    public Position queryPosition() {
        if (_pos != null) return _pos;
        return null;
    }

    /**
     * Checks whether simplify command is applicable. If no arguments were given, simplify is always
     * a valid user command. If position is null but arguments were given, simplify cannot be applied.
     * If invalid rule index is given returns false. Returns true if rule constraint is met, returns
     * false otherwise.
     */
    @Override
    public boolean applicable() {
        TRS lcTrs = _proof.getLcTrs();
        Term t = _proof.getLeft();
        if (_noArgs) return true;
        Term subTerm;
        if (_pos == null) return false;
        subTerm = t.querySubterm(_pos);
        if (_gamma != null && _ruleIndex >= 0) {
            TreeSet<Variable> lvar = LVar(lcTrs.queryRule(_ruleIndex).queryLeftSide(),
                    lcTrs.queryRule(_ruleIndex).queryRightSide(), lcTrs.queryRule(_ruleIndex).queryConstraint());
            for (Variable v : _gamma.domain()) {
                if (!lvar.contains(v)) return false;
            }
        }
        if (!lcTrs.queryRule(_ruleIndex).applicable(subTerm)) {
            return false;
        }
        FunctionSymbol fSymbol = lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot();
        //if (fSymbol.queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) return true;
        if (fSymbol.queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) return false;
        else {
            _gamma = rewrittenConstraintValid(_proof, _ruleIndex, _pos, _gamma);
            return _gamma != null;
        }
    }

    /**
     * Applies the simplify command. If no arguments are given, simplify is applied to all terms and
     * the constraint, and applies calc-rewriting rules to all terms and the constraint. Also
     * substitutes assignments in equivalence proof terms with fresh variables if variable assignment
     * does not yet exist in constraint, then adds it as a constraint. If assignment exists in
     * constraint and a term matches, substitutes term with said assigned variable in term.
     * If unconstrained rule is chosen with rule index, rule is applied to term at position of simplify
     * command. If constrained rule is chosen with rule index, rule is applied in the same way, as
     * applicability should have been checked already.
     */
    @Override
    public void apply() {
        //Case 3: Calculation rules
        if (_noArgs) {
            rewriteConstraintCalc(_proof);
        }
        else if (_gamma != null && _pos != null && _ruleIndex >= 0 &&
                _proof.getLcTrs().queryRule(_ruleIndex).queryConstraint().queryRoot().queryName().equals("TRUE")) {
            rewriteConstraintConstrainedRule(_proof, _pos, _ruleIndex, _gamma);
        }
        //Case 1: Constraint TRUE
        else if (_pos != null && _ruleIndex >= 0 &&
                _proof.getLcTrs().queryRule(_ruleIndex).queryConstraint().queryRoot().queryName().equals("TRUE")) {
            Term temp = _proof.getLeft().querySubterm(_pos);
            temp = _proof.getLcTrs().queryRule(_ruleIndex).apply(temp);
            _proof.setLeft(_proof.getLeft().replaceSubterm(_pos, temp));
        }
        //Case 2: Constraint met
        else if (_pos != null && _ruleIndex >= 0) {
            rewriteConstraintConstrainedRule(_proof, _pos, _ruleIndex, _gamma);
        }
    }

    @Override
    protected void getEqualities(Term c, ArrayList<Term> equalities) {
        if (c.isFunctionalTerm() && c.queryRoot().queryName().equals("==i") && c.queryImmediateSubterm(1).isVariable()) {
            equalities.add(c);
        } else if (c.isFunctionalTerm() && c.queryRoot().queryName().equals("==b") && c.queryImmediateSubterm(1).isVariable()) {
            equalities.add(c);
        } else {
            for (int i = 1; i < c.numberImmediateSubterms() + 1; i++) {
                getEqualities(c.queryImmediateSubterm(i), equalities);
            }
        }
    }

    @Override
    protected Term replaceSubtermsInTerm(Term t, Term eql, Term eqr) {
        for (Position pos : t.queryAllPositions()) {
            Term sub = t.querySubterm(pos);
            if (sub.isFunctionalTerm() && sub.equals(eql)) t = t.replaceSubterm(pos, eqr);
            else if (sub.isFunctionalTerm() && sub.equals(eqr)) t = t.replaceSubterm(pos, eql);
        }
        return t;
    }

    /**
     * This function is a string representation of the user command "simplify" and its given arguments.
     */
    @Override
    public String toString() {
        if (_noArgs) return "simplify";
        return "simplify " + _pos.toString() + " " + (_ruleIndex + 1) + " "
                + (_gamma != null ? _gamma.toReplString() : "");
    }

    /**
     * Sets the equivalence proof on which this user command should act.
     */
    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        return _proof.getFreshVar(expectedType);
    }

    @Override
    public EquivalenceProof getProof() {return _proof;}
}
