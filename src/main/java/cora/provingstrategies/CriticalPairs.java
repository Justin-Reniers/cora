package cora.provingstrategies;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.rewriting.FirstOrderRule;
import cora.terms.Subst;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CriticalPairs {

    private final List<CriticalPair> criticalPairs;

    public CriticalPairs(TRS trs) {
        this.criticalPairs = this.criticalPairs(trs);
    }

    /**
     * Tries to find overlap between terms by trying to match them. If the terms share a root symbol we attempt to
     * match the complete terms. Else we try to match t2 with a non-variable subterm of t1. No matching results in
     * it returning null, as there is no overlap.
     */
    private List<Substitution> overlap(Term t1, Term t2) {
        List<Substitution> substitutions = new ArrayList<>();
        List<Position> positions = t1.queryAllPositions();
        for (Position pos : positions) {
            if (!t1.querySubterm(pos).isVariable()) {
                Substitution s = t1.querySubterm(pos).unify(t2);
                if (s != null) {
                    substitutions.add(t1.querySubterm(pos).unify(t2));
                }
            }
        }
        return substitutions;
    }

    /**
     * Replaces all variables in rule r with fresh variables.
     */
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
     * Finds all the critical pairs for the two given rules. Leaves out trivial critical pairs (l = r),
     * since these don't influence the results of the proving strategies.
     */
    private List<CriticalPair> criticalPair(TRS trs, int r1index, int r2index) {
        List<CriticalPair> pairs = new ArrayList<>();
        FirstOrderRule temp = new FirstOrderRule(trs.queryRule(r1index).queryLeftSide(), trs.queryRule(r1index).queryRightSide());
        Substitution fresh_vars = freshVariables(temp);
        List<Substitution> substitutions = overlap(trs.queryRule(r1index).queryLeftSide().substitute(fresh_vars), trs.queryRule(r2index).queryLeftSide());
        for (Substitution s : substitutions) {
            Term t1 = (trs.queryRule(r1index).queryLeftSide().substitute(fresh_vars).substitute(s));
            Term t2 = (trs.queryRule(r2index).queryLeftSide()).substitute(s);
            Term left = null;
            Term right = trs.queryRule(r1index).queryRightSide().substitute(fresh_vars).substitute(s);
            for (Position pos : t1.queryAllPositions()) {
                if (t1.querySubterm(pos).equals(t2))
                    left = t1.replaceSubterm(pos, trs.queryRule(r2index).queryRightSide().substitute(s));
            }
            if (left != null && !left.equals(right)) {
                pairs.add(new CriticalPair(left, right));
            }
        }
        return pairs;
    }

    /**
     * Finds a list of all the non-trivial critical pairs (t is not equivalent to s).
     */
    private List<CriticalPair> criticalPairs(TRS trs) {
        List<CriticalPair> pairs = new ArrayList<>();
        for (int i = 0; i < trs.queryRuleCount(); i++) {
            for (int j = 0; j < trs.queryRuleCount(); j++) {
                List<CriticalPair> rule_pairs = criticalPair(trs, i, j);
                pairs.addAll(rule_pairs);
            }
        }
        return pairs;
    }

    /**
     * Returns a new list instance of criticalPairs to ensure private criticalPairs is never altered.
     */
    public List<CriticalPair> getCriticalPairs() {
        return Collections.unmodifiableList(criticalPairs);
    }
}
