package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

import java.util.*;


public class LocalConfluenceExtended extends LocalConfluence implements Strategy{

    public LocalConfluenceExtended(TRS trs, boolean terminating) {
        super(trs, terminating);
    }

    private CriticalPair localConvergence(CriticalPair pair) {
        Queue<CriticalPair> q = new LinkedList<>();
        HashSet<CriticalPair> previous_pairs = new HashSet<>();
        previous_pairs.add(pair);
        q.add(pair);
        while (!q.isEmpty()) {
            CriticalPair terms = q.poll();
            List<Term> left_reductions = trs.breadthFirstReduce(terms.getLeft());
            List<Term> right_reductions = trs.breadthFirstReduce(terms.getRight());
            List<CriticalPair> reduce_left = super.cartesian(terms.getRight(), left_reductions);
            List<CriticalPair> reduce_right = super.cartesian(terms.getLeft(), right_reductions);
            List<CriticalPair> cartesian = new ArrayList<>();
            cartesian.addAll(reduce_right);
            cartesian.addAll(reduce_left);
            for (CriticalPair new_pair : cartesian) {
                if (new_pair.getLeft().equals(new_pair.getRight())) return new_pair;
                if (previous_pairs.add(new_pair)) {
                    q.add(new_pair);
                }
            }
        }
        return new CriticalPair(null, null);
    }

    @Override
    public Result apply() {
        boolean converges = true;
        for (CriticalPair pair : this.criticalPairs) {
            CriticalPair local_convergence = this.localConvergence(pair);
            if (local_convergence.getLeft() == null || local_convergence.getRight() == null) {
                converges = false;
                break;
            }
        }
        if (converges) {
            if (super.terminating) {
                return new ResultInherit(Result.RESULT.CONFLUENT);
            }
            return new ResultInherit(Result.RESULT.LOCALLY_CONFLUENT);
        }
        return new ResultInherit(Result.RESULT.NON_CONFLUENT);
    }
}
