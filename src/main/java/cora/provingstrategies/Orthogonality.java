package cora.provingstrategies;

import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import java.util.HashSet;
import java.util.function.Function;

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
            for (int i = 1; i <= t.numberImmediateSubterms(); i++) {
                if (!leftLinearTerm(t.queryImmediateSubterm(i), vars)) return false;
            }
        }
        return true;
    }


    private boolean sharedSymbols(Term t, HashSet<FunctionSymbol> fs) {
        if (t.isConstant()) return false;
        if (t.isVariable()) return false;
        if (t.isFunctionalTerm()) {
            FunctionalTerm ft = (FunctionalTerm) t;
            if (!fs.add(ft.queryRoot())) return true;
            for (int i = 1; i <= t.numberImmediateSubterms(); i++)
                if (sharedSymbols(t.queryImmediateSubterm(i), fs)) return true;
        }
        return false;
    }


    @Override
    public void apply(TRS trs) {
        boolean left_linear = true;
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            HashSet<Var> used_vars = new HashSet<>();
            left_linear = left_linear && leftLinearTerm(trs.queryRule(i).queryLeftSide(), used_vars);
            if (!left_linear) break;
        }
        System.out.println("The system is left linear:" + left_linear);

        for (int i = 0; i < trs.queryRuleCount(); i++) {
            HashSet<FunctionSymbol> fs = new HashSet<>();
            System.out.println(sharedSymbols(trs.queryRule(i).queryLeftSide(), fs));
        }
    }
}
