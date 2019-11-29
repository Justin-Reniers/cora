package cora.provingstrategies;

import cora.exceptions.NullCallError;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.rewriting.FirstOrderRule;
import cora.terms.Subst;
import cora.terms.Var;
import java.util.ArrayList;
import java.util.List;

public abstract class StrategyInherit {

    /**
     * Tries to find overlap between terms by trying to match them. If the terms share a root symbol we attempt to
     * match the complete terms. Else we try to match t2 with a non-variable subterm of t1. No matching results in
     * it returning null, as there is no overlap.
     */
    private Substitution overlap(Term t1, Term t2, boolean same_rule) {
        if (t1.isFunctionalTerm() && t2.isFunctionalTerm() && t1.queryRoot().equals(t2.queryRoot()) && !same_rule) {
            System.out.println("Compared terms; \t" + t1 + "\t" + t2);
            return t1.match(t2);
        }
        List<Position> positions = t1.queryAllPositions();
        for (Position pos : positions) {
            if (!t1.querySubterm(pos).isVariable()) {
                try {
                    System.out.println("Compared terms; \t" + t1.querySubterm(pos).toString() + "\t" + t2);
                    return t1.querySubterm(pos).match(t2);
                } catch (NullCallError e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return null;
    }

    private Substitution freshVariables(FirstOrderRule r) {
        Subst s = new Subst();
        int var_counter = 0;
        for (Variable v : r.queryLeftSide().vars()) {
            Var fresh = new Var("x" + var_counter);
            s.extend(v, fresh);
            var_counter++;
        }
        return s;
    }

    /**
     * Attempts to find a critical pair between the two rules of trs at rule index 1 and rule index 2. Pairs are
     * found by matching the left term of rule 2 with a non-variable subterm of t1 or t1 in whole. If no matching
     * is found or whenever a critical pair is trivial null is returned.
     */
    private List<Term> criticalPair(TRS trs, int r1index, int r2index) {
        FirstOrderRule temp = new FirstOrderRule(trs.queryRule(r1index).queryLeftSide(), trs.queryRule(r1index).queryRightSide());
        Substitution fresh_vars = freshVariables(temp);
        Substitution s = overlap(trs.queryRule(r1index).queryLeftSide().substitute(fresh_vars), trs.queryRule(r2index).queryLeftSide(), r1index == r2index);
        Term left_r1_og = trs.queryRule(r1index).queryLeftSide();
        if (s != null) {
            Substitution sigma = left_r1_og.match(trs.queryRule(r1index).queryLeftSide());
            Term left = trs.queryRule(r1index).queryRightSide().substitute(fresh_vars).substitute(s).substitute(sigma);
            List<Position> positions = trs.queryRule(r1index).queryLeftSide().queryAllPositions();
            Term right = null;
            for (Position pos : positions) {
                if (left_r1_og.substitute(fresh_vars).substitute(s).querySubterm(pos).equals(trs.queryRule(r2index).queryLeftSide()))
                    right = left_r1_og.substitute(s).replaceSubterm(pos, trs.queryRule(r2index).queryRightSide());
            }
            System.out.println("pair:\t " + left + "\t" + right);
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
    protected List<List<Term>> criticalPairs(TRS trs) {
        List<List<Term>> pairs = new ArrayList<>();
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            for (int j = 0; j < trs.queryRuleCount(); j++) {
                System.out.println("\n\nComparing rule " + i + " with rule " + j);
                List<Term> pair = criticalPair(trs, i, j);
                if (pair != null) pairs.add(pair);
            }
        }
        System.out.println("Critical pairs: " + pairs.toString() + "\n\n");
        return pairs;
    }

}
