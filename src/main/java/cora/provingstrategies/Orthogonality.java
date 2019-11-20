package cora.provingstrategies;

import cora.exceptions.NullCallError;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
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

    /**
     * Tries to find overlap between terms by trying to match them. If the terms share a root symbol we attempt to
     * match the complete terms. Else we try to match t2 with a non-variable subterm of t1. No matching results in
     * it returning null, as there is no overlap.
     */
    private Substitution overlap(Term t1, Term t2) {
        if (t1.isFunctionalTerm() && t2.isFunctionalTerm() && t1.queryRoot().equals(t2.queryRoot()))
            try {
                return t1.match(t2);
            } catch (NullCallError e) {
                e.getMessage();
            }
        for (int i = 1; i <= t1.numberImmediateSubterms(); i++) {
            if (!t1.queryImmediateSubterm(i).isVariable())
                try {
                    return (Subst) t1.queryImmediateSubterm(i).match(t2);
                } catch (NullCallError e) {
                    e.getMessage();
                }
        }
        return null;
    }

    /**
     * Attempts to find a critical pair between the two rules of trs at rule index 1 and rule index 2. Pairs are
     * found by matching the left term of rule 2 with a non-variable subterm of t1 or t1 in whole. If no matching
     * is found or whenever a critical pair is trivial null is returned.
     */
    private List<Term> criticalPair(TRS trs, int r1index, int r2index) {
        Substitution s = overlap(trs.queryRule(r1index).queryLeftSide(), trs.queryRule(r2index).queryLeftSide());
        if (s != null) {
            Substitution sigma = trs.queryRule(r1index).queryLeftSide().match(trs.queryRule(r1index).queryLeftSide());
            Term left = trs.queryRule(r1index).queryRightSide().substitute(s).substitute(sigma);
            List<Position> positions = trs.queryRule(r1index).queryLeftSide().queryAllPositions();
            Term right = null;
            for (Position pos : positions) {
                if (trs.queryRule(r1index).queryLeftSide().substitute(s).querySubterm(pos).equals(trs.queryRule(r2index).queryLeftSide()))
                    right = trs.queryRule(r1index).queryLeftSide().substitute(s).replaceSubterm(pos, trs.queryRule(r2index).queryRightSide());
            }
            if (left.equals(right)) return null;
            List<Term> pair = new ArrayList<>();
            pair.add(left);
            pair.add(right);
            return pair;
        }
        return null;
    }

    /**
     * Finds a list of all the non-trivial critical pairs (t is not equivalent to s).
     */
    private List<List<Term>> criticalPairs(TRS trs) {
        List<List<Term>> pairs = new ArrayList<>();
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            for (int j = 0; j < trs.queryRuleCount(); j++) {
                List<Term> pair = criticalPair(trs, i, j);
                if (pair != null) pairs.add(pair);
            }
        }
        return pairs;
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

        System.out.println(criticalPairs(trs));
        if (criticalPairs(trs).isEmpty()) {
            System.out.println(RESULT.CONFLUENT);
            return RESULT.CONFLUENT;
        }
        System.out.println(RESULT.MAYBE);
        return RESULT.MAYBE;
    }
}
