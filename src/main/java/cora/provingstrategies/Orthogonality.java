package cora.provingstrategies;

import cora.exceptions.NullCallError;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.rewriting.FirstOrderRule;
import cora.terms.Subst;
import cora.terms.Var;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Orthogonality extends StrategyInherit implements Strategy {

    /**
     * Checks the left-linearity of a Term. A term is left-linear if variables occur
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
    public RESULT apply(TRS trs) {
        boolean left_linear = true;
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            HashSet<Var> used_vars = new HashSet<>();
            left_linear = left_linear && leftLinearTerm(trs.queryRule(i).queryLeftSide(), used_vars);
            if (!left_linear) break;
        }
        System.out.println("The system is left linear:" + left_linear);

        for (int i = 0; i < trs.queryRuleCount(); i++)
            System.out.println(trs.queryRule(i));

        //System.out.println(criticalPairs(trs));
        if (criticalPairs(trs).isEmpty()) {
            System.out.println(RESULT.CONFLUENT);
            return RESULT.CONFLUENT;
        }
        System.out.println(RESULT.MAYBE);
        return RESULT.MAYBE;
    }
}
