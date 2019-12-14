package cora.interfaces.provingstrategies;

import cora.interfaces.rewriting.TRS;

public interface Strategy {

    enum RESULT {LOCALLY_CONFLUENT, CONFLUENT, NON_CONFLUENT, MAYBE, TIMEOUT}

    RESULT apply();

}
