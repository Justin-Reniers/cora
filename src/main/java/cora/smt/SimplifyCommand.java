package cora.smt;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;

public class SimplifyCommand extends UserCommandInherit implements UserCommand {

    private Position _pos;
    private int _ruleIndex;

    public SimplifyCommand(Position pos, int ruleIndex) {
        _pos = pos;
        _ruleIndex = ruleIndex;
    }

    @Override
    public Position queryPosition() {
        if (_pos != null) return _pos;
        return null;
    }

    @Override
    public boolean applicable(TRS lcTrs, Term t) {
        Term subTerm;
        if (_pos == null) return false;
        subTerm = t.querySubterm(_pos);
        if (!lcTrs.queryRule(_ruleIndex).applicable(subTerm)) return false;
        FunctionSymbol fSymbol = lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot();
        if (!fSymbol.queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) return false;
        return true;
    }

    @Override
    public Term apply(TRS lcTrs, Term t) {
        //Case 1: Unconstrained Rule
        if (lcTrs.queryRule(_ruleIndex).queryConstraint().queryRoot().equals(lcTrs.lookupSymbol("TRUE"))) {
            return applyUnconstrainedRule(lcTrs, t);
        }
        return null;
    }

    private Term applyUnconstrainedRule(TRS lcTrs, Term t) {
        Term subTerm = t.querySubterm(_pos);
        lcTrs.queryRule(_ruleIndex).apply(subTerm);
        t.replaceSubterm(_pos, subTerm);
        return t;
    }

    @Override
    public String toString() {
        return "simplify " + _pos.toString() + " " + _ruleIndex;
    }
}
