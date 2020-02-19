package cora.provingstrategies;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

import java.util.List;

public abstract class StrategyInherit implements Runnable {

    protected final List<CriticalPair> criticalPairs;
    protected final TRS trs;

    StrategyInherit(TRS trs, List<CriticalPair> criticalPairs) {
        this.trs = trs;
        this.criticalPairs = criticalPairs;
    }

}
