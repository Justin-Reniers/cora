package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

import java.util.List;
import java.util.concurrent.*;

public abstract class StrategyInherit implements Strategy {

    protected final List<CriticalPair> criticalPairs;
    protected final TRS trs;

    public abstract Result apply();

    StrategyInherit(TRS trs) {
        this.trs = trs;
        this.criticalPairs = this.getCriticalPairs();
    }

    private List<CriticalPair> getCriticalPairs() {
        CriticalPairs cp = new CriticalPairs(trs);
        return cp.getCriticalPairs();
    }

    public final Result apply (int timeout) throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Result> future = ex.submit((Callable<Result>) this::apply);

        try {
            long starttime = System.currentTimeMillis();
            Result r = future.get(timeout, TimeUnit.SECONDS);
            long time = System.currentTimeMillis() - starttime;
            r.setTime(time);
            return r;
        } catch (TimeoutException te) {
            future.cancel(true);
            return new ResultInherit(Result.RESULT.TIMEOUT, timeout*1000);
        } catch (InterruptedException | ExecutionException exc) {
            throw new Exception(exc.getMessage());
        } finally {
            ex.shutdown();
        }
    }

}
