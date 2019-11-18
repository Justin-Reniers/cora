package cora.provingstrategies;

import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.terms.Subst;
import cora.terms.Var;
import java.util.HashSet;

public class Orthogonality extends StrategyInherit implements Strategy {

    /**
     * Checks the left-linearity of a Term. A term is left-linear if variables occur
     * more than once in the recursive Term.
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

    /**
     * lemma: Two reduction rules overlap iff there is a non-variable subterm of l0 that can be matched with a p1-redex
     * (or vice-versa)
     */
    private boolean overlap(Term t1, Term t2, Substitution s) {
        return false;
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

        Substitution s = new Subst();
        System.out.println(overlap(trs.queryRule(1).queryLeftSide(), trs.queryRule(0).queryLeftSide(), s));
        //System.out.println(s.domain());

        return RESULT.MAYBE;
    }
}
