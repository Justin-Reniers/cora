package cora.interfaces.smt;

import cora.interfaces.terms.Term;

public interface History {
    String toString();

    Term getLeft();

    Term getRight();

    Term getConstraint();

    UserCommand getUserCommand();
}
