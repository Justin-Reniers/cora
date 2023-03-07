package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.*;
import cora.loggers.Logger;
import cora.terms.Var;
import cora.z3.Z3TermHandler;

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
    private EquivalenceProof _proof;

    public SimplifyCommand(Position pos, int ruleIndex) {
        super();
        _pos = pos;
        _ruleIndex = ruleIndex;
        _noArgs = false;

    }

    public SimplifyCommand() {
        super();
        _pos = null;
        _ruleIndex = -1;
        _noArgs = true;
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
     * TODO other cases, comparison of vars between proof constraint and rule constraint
     */
    @Override
    public boolean applicable() {
        TRS lcTrs = _proof.getLcTrs();
        Term t = _proof.getLeft();
        if (_noArgs) return true;
        Term subTerm;
        if (_pos == null) return false;
        subTerm = t.querySubterm(_pos);
        if (!lcTrs.queryRule(_ruleIndex).applicable(subTerm)) {
            return false;
        }
        FunctionSymbol fSymbol = lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot();
        if (fSymbol.queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) return true;
        if (fSymbol.queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) return false;
        else {
            Z3TermHandler z3 = new Z3TermHandler();
            Term ruleLeft = lcTrs.queryRule(_ruleIndex).queryLeftSide();
            Substitution s = ruleLeft.unify(t);
            z3.constraintCheck(lcTrs.queryRule(_ruleIndex).queryConstraint(), _proof.getConstraint(), s);
            return z3.getModel() != null;
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
        //TODO If constraint of rule unifies with constraint of proof or
        //TODO subconstraint of proof, apply rule.
        TRS lcTrs = _proof.getLcTrs();
        Term t = _proof.getLeft();
        Term c = _proof.getConstraint();
        //Case 3: Calculation rules
        if (_noArgs) {
            if (containsEq(c)) Logger.log("equality in constraint");
            Z3TermHandler handler = new Z3TermHandler();
            Substitution c_sub = handler.getSubstitutions(c);
            Term temp = t.unsubstitute(c_sub);
            Substitution f_vars = handler.getSubstitutionsFreshVars(temp);
            temp = temp.substitute(f_vars);
            temp = handler.simplify(temp);
            _proof.setLeft(temp);
            Logger.log(c_sub.toString());
        }
        //Case 1: Unconstrained Rule
        else if (lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) {
            _proof.setLeft(applyRule(lcTrs, t));
        }
        //Case 2: Constraint met
        else if (!lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) {
            Z3TermHandler dec = new Z3TermHandler();
            Substitution s = lcTrs.queryRule(_ruleIndex).queryConstraint().unify(c);
            dec.deconstruct(c);
            if (dec.getModel() != null) {
                _proof.setLeft(applyRule(lcTrs, t));
            }
        }
    }

    /**
     * Applies the rewriting rule from the given lctrs to the given term at the rule index of
     * this simplify command.
     */
    private Term applyRule(TRS lcTrs, Term t) {
        Term subTerm = t.querySubterm(_pos);
        subTerm = lcTrs.queryRule(_ruleIndex).apply(subTerm);
        Term ret = t.replaceSubterm(_pos, subTerm);
        return ret;
    }

    private boolean containsEq(Term t) {
        if (t.isFunctionalTerm()) {
            if (t.queryRoot().toString().equals("==")) return true;
            for (int i = 1; i < t.numberImmediateSubterms() + 1; i++) {
                Term sub = t.queryImmediateSubterm(i);
                containsEq(sub);
            }
        }
        return false;
    }

    /**
     * This function is a string representation of the user command "simplify" and its given arguments.
     */
    @Override
    public String toString() {
        if (_noArgs) return "simplify";
        return "simplify " + _pos.toString() + " " + _ruleIndex;
    }

    /**
     * Sets the equivalence proof on which this user command should act.
     */
    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }
}
