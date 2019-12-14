package cora.provingstrategies;

import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ExhaustiveConvergence extends StrategyInherit implements Strategy {

    public ExhaustiveConvergence(TRS trs, List<List<Term>> criticalPairs) {
        super(trs, criticalPairs);
    }

    private List<List<Term>> exhaustiveConverge(List<Term> pair) {
        List<List<Term>> steps = new ArrayList<>();
        steps.add(pair);
        ListIterator<List<Term>> iterator = steps.listIterator();
        while (iterator.hasNext()) {
            List<Term> terms = iterator.next();
            System.out.println("Current term: " + terms);
            Term left_reduce = trs.leftmostInnermostReduce(terms.get(0));
            if (left_reduce != null) {
                List<Term> new_pair = new ArrayList<>();
                new_pair.add(left_reduce);
                new_pair.add(terms.get(1));
                iterator.remove();
                iterator.add(new_pair);
                iterator.previous();
            } else {
                Term right_reduce = trs.leftmostInnermostReduce(terms.get(1));
                if (right_reduce != null) {
                    List<Term> new_pair = new ArrayList<>();
                    new_pair.add(terms.get(0));
                    new_pair.add(right_reduce);
                    iterator.remove();
                    iterator.add(new_pair);
                    iterator.previous();
                }
            }
            System.out.println("Pairs in steps: " + steps);
        }
        return steps;
    }

    @Override
    public RESULT apply() {
        System.out.println("Exhaustive Convergence");
        boolean converges = true;
        for (List<Term> pair : this.criticalPairs) {
            //System.out.println("\n" + pair);
            List<List<Term>> pairs = exhaustiveConverge(pair);
            if (!pairs.get(0).get(0).equals(pairs.get(0).get(1))) converges = false;
        }
        boolean timeout = false;
        if (!converges && timeout) {
            System.out.println(RESULT.MAYBE);
            return RESULT.MAYBE;
        }
        if (!converges) {
            System.out.println(RESULT.NON_CONFLUENT);
            return RESULT.NON_CONFLUENT;
        }
        System.out.println(RESULT.CONFLUENT);
        return RESULT.CONFLUENT;
    }

    @Override
    public void run() {

    }
}
