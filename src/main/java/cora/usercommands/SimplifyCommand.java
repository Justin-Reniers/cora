package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidConstraintRewritingException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.exceptions.invalidruleapplications.InvalidSimplifyApplicationException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

import java.util.ArrayList;
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
     * Applies the simplify command. If no arguments are given, simplify is applied to all terms and
     * the constraint, and applies calc-rewriting rules to all terms and the constraint. Also
     * substitutes assignments in equivalence proof terms with fresh variables if variable assignment
     * does not yet exist in constraint, then adds it as a constraint. If assignment exists in
     * constraint and a term matches, substitutes term with said assigned variable in term.
     * If unconstrained rule is chosen with rule index, rule is applied to term at position of simplify
     * command. If constrained rule is chosen with rule index, rule is applied in the same way, as
     * applicability should have been checked already.
     *
     * @return
     */
    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        //Case: Calculation rules
        if (_noArgs) {
            ps = rewriteConstraintCalc(ps, _proof);
            return ps;
        }
        else if (_pos == null) {
            throw new InvalidSimplifyApplicationException("No position given for rewrite rule application");
        }

        TRS lcTrs = _proof.getLcTrs();
        Term s = ps.getS();
        Term sp = s.querySubterm(_pos);
        if (_gamma != null && _ruleIndex >= 0) {
            TreeSet<Variable> lvar = LVar(lcTrs.queryRule(_ruleIndex).queryLeftSide(),
                    lcTrs.queryRule(_ruleIndex).queryRightSide(), lcTrs.queryRule(_ruleIndex).queryConstraint());
            for (Variable v : _gamma.domain()) {
                if (!lvar.contains(v)) {
                    throw new InvalidSimplifyApplicationException("Variable " + v + " not in LVar(" +
                            lcTrs.queryRule(_ruleIndex) + ")");
                }
            }
        }
        if (!lcTrs.queryRule(_ruleIndex).applicable(sp)) {
            throw new InvalidSimplifyApplicationException("Rule " + lcTrs.queryRule(_ruleIndex) + " not applicable to "
                    + sp);
        }
        try {
            _gamma = rewrittenConstraintValid(ps, lcTrs, _ruleIndex, _pos, _gamma);
            if (_gamma == null) throw new InvalidSimplifyApplicationException("No gamma found");
        } catch (InvalidConstraintRewritingException e) {

        }

        //Case 2: Constraint met
       if (_pos != null && _ruleIndex >= 0) {
            ps = rewriteConstraintConstrainedRule(ps, _proof, _pos, _ruleIndex, _gamma);
        }
        return ps;
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
