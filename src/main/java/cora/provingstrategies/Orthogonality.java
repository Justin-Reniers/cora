package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.terms.Var;

import java.util.HashSet;

public class Orthogonality extends StrategyInherit implements Strategy{

    public Orthogonality(TRS trs) {
        super(trs);
    }

    /**
     * Checks the left-linearity of a Term. A term is left-linear if variables don't occur
     * more than once in the recursive term.
     */
    private boolean leftLinearTerm(Term t, HashSet<Var> vars) {
        if (t.isConstant()) return true;
        if (t.isVariable()) {
            Var v = (Var) t;
            return vars.add(v);
        }
        if (t.isFunctionalTerm()) {
            for (int i = 1; i <= t.numberImmediateSubterms(); i++)
                if (!leftLinearTerm(t.queryImmediateSubterm(i), vars)) return false;
        }
        return true;
    }

    @Override
    public Result apply() {
        boolean left_linear = true;
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            HashSet<Var> used_vars = new HashSet<>();
            if (!leftLinearTerm(trs.queryRule(i).queryLeftSide(), used_vars)) {
                left_linear = false;
                break;
            }
        }
        if (left_linear && criticalPairs.isEmpty()) {
            return new ResultInherit(Result.RESULT.CONFLUENT);
        }
        return new ResultInherit(Result.RESULT.MAYBE);
    }
}
