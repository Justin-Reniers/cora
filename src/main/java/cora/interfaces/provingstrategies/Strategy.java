package cora.interfaces.provingstrategies;

import cora.interfaces.rewriting.TRS;

public interface Strategy {

    enum RESULT {CONFLUENT, NON_CONFLUENT, MAYBE, TIMEOUT}

    RESULT apply(TRS trs);

}
