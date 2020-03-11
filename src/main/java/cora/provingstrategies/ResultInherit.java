package cora.provingstrategies;

import cora.interfaces.provingstrategies.Result;

public class ResultInherit implements Result {

    private RESULT res;
    private long timeout;

    public ResultInherit(RESULT res, long timeout) {
        this.res = res;
        this.timeout = timeout;
    }

    public ResultInherit(RESULT res) {
        this.res = res;
        this.timeout = 0;
    }

    @Override
    public RESULT getResult() {
        return res;
    }

    @Override
    public void setTime(long time) {
        this.timeout = time;
    }

    @Override
    public long getTime() {
        return timeout;
    }
}
