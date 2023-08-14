package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;
import cora.interfaces.provingstrategies.Strategy;
import cora.interfaces.rewriting.TRS;

import java.util.List;
import java.util.concurrent.*;

/**
 * StrategyInherit describes all default functionality for instances of Strategy.
 *
 */
public abstract class StrategyInherit implements Strategy {

    public final List<CriticalPair> criticalPairs;
    protected final TRS trs;

    public abstract Result apply();

    /**
     * Constructor for a Strategy. Always takes a TRS and computes the
     * Critical Pairs associated with it.
     */
    StrategyInherit(TRS trs) {
        this.trs = trs;
        this.criticalPairs = this.getCriticalPairs();
    }

    /** Gets the list of Critical Pairs for the current TRS. */
    private List<CriticalPair> getCriticalPairs() {
        CriticalPairs cp = new CriticalPairs(trs);
        return cp.getCriticalPairs();
    }

    /**
     * Creates a thread that applies the method of the instantiated Strategy.
     * Waits for a result and the time.
     * If it is within the timeout, the Result type is given. Otherwise, a
     * timeout is called.
     */
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
