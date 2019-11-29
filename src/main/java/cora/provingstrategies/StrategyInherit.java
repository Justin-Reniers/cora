package cora.provingstrategies;

import cora.interfaces.terms.Term;
import java.util.List;

public abstract class StrategyInherit {

    public final List<List<Term>> criticalPairs;

    protected StrategyInherit(List<List<Term>> criticalPairs) {
        this.criticalPairs = criticalPairs;
    }
}
