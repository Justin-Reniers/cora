package cora.interfaces.smt;

import cora.interfaces.terms.Term;

public interface History {
    public String toString();

    public Term getLeft();

    public Term getRight();

    public Term getConstraint();

    public UserCommand getUserCommand();
}
