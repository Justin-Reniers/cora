package cora.smt;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.z3.Z3TermDeconstructor;

public class SimplifyCommand extends UserCommandInherit implements UserCommand {

    private Position _pos;
    private int _ruleIndex;
    private boolean _noArgs;

    public SimplifyCommand(Position pos, int ruleIndex) {
        if (pos != null && ruleIndex >= 0) {
            _pos = pos;
            _ruleIndex = ruleIndex;
            _noArgs = false;
        } else {
            _pos = null;
            _ruleIndex = -1;
            _noArgs = true;
        }
    }

    @Override
    public Position queryPosition() {
        if (_pos != null) return _pos;
        return null;
    }

    @Override
    public boolean applicable(TRS lcTrs, Term t, Term constraint) {
        Term subTerm;
        if (_pos == null) return false;
        subTerm = t.querySubterm(_pos);
        if (!lcTrs.queryRule(_ruleIndex).applicable(subTerm)) {
            return false;
        }
        FunctionSymbol fSymbol = lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot();
        if (fSymbol.queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) return true;
        if (!fSymbol.queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) {

        }
        return true;
    }

    @Override
    public Term apply(TRS lcTrs, Term t, Term constraint) {
        //Case 1: Unconstrained Rule
        if (lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) {
            return applyUnconstrainedRule(lcTrs, t);
        }
        //Case 2: Constraint met
        else if (!lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("FALSE"))) {
            Z3TermDeconstructor dec = new Z3TermDeconstructor();
            dec.deconstruct(constraint);
            if (dec.getModel() != null) {
                return applyRule(lcTrs, t);
            }
        }
        //Case 3: Calculation rules
        return null;
    }

    private Term applyUnconstrainedRule(TRS lcTrs, Term t) {
        Term subTerm = t.querySubterm(_pos);
        subTerm = lcTrs.queryRule(_ruleIndex).apply(subTerm);
        Term ret =  t.replaceSubterm(_pos, subTerm);
        return ret;
    }

    private Term applyRule(TRS lcTrs, Term t) {
        Term subTerm = t.querySubterm(_pos);
        subTerm = lcTrs.queryRule(_ruleIndex).apply(subTerm);
        Term ret = t.replaceSubterm(_pos, subTerm);
        return ret;
    }

    @Override
    public String toString() {
        return "simplify " + _pos.toString() + " " + _ruleIndex;
    }
}
