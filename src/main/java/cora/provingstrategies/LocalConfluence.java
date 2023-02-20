package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

import java.util.*;

public class LocalConfluence extends StrategyInherit implements Strategy{

    protected boolean terminating;

    public LocalConfluence(TRS trs, boolean terminating) {
        super(trs);
        this.terminating = terminating;
    }


    private CriticalPair localConvergence(CriticalPair pair) {
        Queue<CriticalPair> q = new LinkedList<>();
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
                q.add(new_pair);
            }
        }
        return new CriticalPair(null, null);
    }

    protected List<CriticalPair> cartesian(Term unreduced, List<Term> reduced) {
        List<CriticalPair> reductions = new ArrayList<>();
        for (Term t : reduced) {
            reductions.add(new CriticalPair(unreduced, t));
        }
        return reductions;
    }

    @Override
    public Result apply() {
        boolean converges = true;
        for (CriticalPair pair : super.criticalPairs) {
            CriticalPair local_convergence = localConvergence(pair);
            if (local_convergence.getLeft() == null || local_convergence.getRight() == null) converges = false;
        }
        if (converges) {
            if (terminating)
                return new ResultInherit(Result.RESULT.CONFLUENT);
            return new ResultInherit(Result.RESULT.LOCALLY_CONFLUENT);
        }
        return new ResultInherit(Result.RESULT.NON_CONFLUENT);
    }

}
