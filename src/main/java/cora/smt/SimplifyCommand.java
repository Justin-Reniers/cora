package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.z3.Z3Helper;
import cora.z3.Z3TermHandler;

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

    @Override
    public Position queryPosition() {
        if (_pos != null) return _pos;
        return null;
    }

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

    @Override
    public void apply() {
        TRS lcTrs = _proof.getLcTrs();
        Term t = _proof.getLeft();
        Term c = _proof.getConstraint();
        //Case 3: Calculation rules
        if (_noArgs) {
            if (containsEq(c)) Logger.log("equality in constraint");
            Z3TermHandler handler = new Z3TermHandler();
            Substitution s = handler.getSubstitutions(c);
            Logger.log(s.toString());
            Term temp = handler.simplify(c).substitute(s);
            _proof.setConstraint(temp);
            temp = handler.simplify(_proof.getRight()).substitute(s);
            _proof.setRight(temp);
            temp = handler.simplify(t).substitute(s);
            _proof.setLeft(temp);
        }
        //Case 1: Unconstrained Rule
        else if (lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) {
            _proof.setLeft(applyRule(lcTrs, t));
        }
        //Case 2: Constraint met
        else if (!lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) {
            Z3TermHandler dec = new Z3TermHandler();
            dec.deconstruct(c);
            if (dec.getModel() != null) {
                _proof.setLeft(applyRule(lcTrs, t));
            }
        }
    }

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

    @Override
    public String toString() {
        if (_noArgs) return "simplify";
        return "simplify " + _pos.toString() + " " + _ruleIndex;
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }
}
