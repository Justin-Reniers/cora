package cora.provingstrategies;

import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;

import java.util.*;

public class LocalConfluence extends StrategyInherit implements Strategy{

    public LocalConfluence(TRS trs, List<CriticalPair> criticalPairs) {
        super(trs, criticalPairs);
    }


    public CriticalPair localConvergence(CriticalPair pair) {
        Queue<CriticalPair> q = new LinkedList<>();
        HashSet<CriticalPair> previous_pairs = new HashSet<>();
        previous_pairs.add(pair);
        q.add(pair);
        while (!q.isEmpty()) {
            CriticalPair terms = q.poll();
            List<Term> left_reductions = trs.breadthFirstReduce(terms.getLeft());
            List<Term> right_reductions = trs.breadthFirstReduce(terms.getRight());
            List<CriticalPair> reduce_left = cartesian(terms.getRight(), left_reductions);
            List<CriticalPair> reduce_right = cartesian(terms.getLeft(), right_reductions);
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

    private List<CriticalPair> cartesian(Term unreduced, List<Term> reduced) {
        List<CriticalPair> reductions = new ArrayList<>();
        for (Term t : reduced) {
            reductions.add(new CriticalPair(unreduced, t));
        }
        return reductions;
    }

    @Override
    public Strategy.RESULT apply() {
        Logger.log("Local Convergence");
        boolean converges = true;
        for (CriticalPair pair : this.criticalPairs) {
            CriticalPair local_convergence = localConvergence(pair);
            if (local_convergence.getLeft() == null || local_convergence.getRight() == null) converges = false;
        }
        if (converges) {
            Logger.log(RESULT.LOCALLY_CONFLUENT.toString() + "\n");
            return RESULT.LOCALLY_CONFLUENT;
        }
        Logger.log(RESULT.NON_CONFLUENT.toString() + "\n");
        return RESULT.NON_CONFLUENT;
    }

    @Override
    public void run() {

    }
}
