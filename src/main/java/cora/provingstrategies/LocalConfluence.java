package cora.provingstrategies;

import com.google.common.collect.Lists;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

public class LocalConfluence extends StrategyInherit implements Strategy{

    public LocalConfluence(TRS trs, List<List<Term>> criticalPairs) {
        super(trs, criticalPairs);
    }


    public boolean localConvergence(List<Term> pair) {
        List<List<Term>> steps = new ArrayList<>();
        HashSet<List<Term>> previous_pairs = new HashSet<>();
        previous_pairs.add(pair);
        steps.add(pair);
        ListIterator<List<Term>> iterator = steps.listIterator();
        while (iterator.hasNext()) {
            List<Term> terms = iterator.next();
            System.out.println("Current terms: " + terms.toString());
            List<Term> left_reductions = trs.breadthFirstReduce(terms.get(0));
            List<Term> right_reductions = trs.breadthFirstReduce(terms.get(1));
            left_reductions.add(terms.get(0));
            right_reductions.add(terms.get(1));
            List<List<Term>> cartesian = Lists.cartesianProduct(left_reductions, right_reductions);
            System.out.println("Cartesian product after breadth first reduce: " + cartesian);
            iterator.remove();
            for (List<Term> new_pair : cartesian) {
                System.out.println(new_pair);
                if (new_pair.get(0).equals(new_pair.get(1))) return true;
                if (previous_pairs.add(new_pair)) {
                    iterator.add(new_pair);
                }
            }
            System.out.println("here");
            if (iterator.hasPrevious()) iterator.previous();
            System.out.println("All unique terms: " + previous_pairs);
        }
        System.out.println("out of the loop");
        return false;
    }

    @Override
    public Strategy.RESULT apply() {
        System.out.println("Local Convergence");
        boolean converges = true;
        for (List<Term> pair : this.criticalPairs) {
            System.out.println("\n" + pair);
            boolean local_convergence = localConvergence(pair);
            converges = converges && local_convergence;
        }
        if (converges) {
            System.out.println(RESULT.LOCALLY_CONFLUENT);
            return RESULT.LOCALLY_CONFLUENT;
        }
        System.out.println(RESULT.MAYBE);
        return RESULT.MAYBE;
    }

    @Override
    public void run() {

    }
}
