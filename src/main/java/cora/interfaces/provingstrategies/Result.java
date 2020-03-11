package cora.interfaces.provingstrategies;

public interface Result {

    enum RESULT {LOCALLY_CONFLUENT, CONFLUENT, NON_CONFLUENT, MAYBE, TIMEOUT}

    RESULT getResult();

    long getTime();

    void setTime(long time);

}
