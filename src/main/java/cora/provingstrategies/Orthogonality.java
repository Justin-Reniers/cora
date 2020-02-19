package cora.provingstrategies;

import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.loggers.Logger;
import cora.terms.Var;

import java.util.HashSet;
import java.util.List;

public class Orthogonality extends StrategyInherit implements Strategy{

    public Orthogonality(TRS trs, List<CriticalPair> criticalPairs) {
        super(trs, criticalPairs);
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
    public RESULT apply() {
        Logger.log("Weak Orthogonality");
        boolean left_linear = true;
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            HashSet<Var> used_vars = new HashSet<>();
            if (!leftLinearTerm(trs.queryRule(i).queryLeftSide(), used_vars)) {
                left_linear = false;
                break;
            }
        }
        Logger.log("The system is left linear:" + left_linear);
        if (left_linear && criticalPairs.isEmpty()) {
            Logger.log(RESULT.CONFLUENT.toString() + "\n");
            return RESULT.CONFLUENT;
        }
        Logger.log(RESULT.MAYBE.toString() + "\n");
        return RESULT.MAYBE;
    }

    @Override
    public void run() {

    }
}
