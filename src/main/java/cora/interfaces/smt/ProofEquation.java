package cora.interfaces.smt;

import cora.interfaces.terms.Term;
import cora.smt.Equation;

public interface ProofEquation {
    Term getLeft();

    Term getRight();

    Term getConstraint();

    String toString();

    void setLeft(Term l);

    void setRight(Term r);

    void setConstraint(Term c);
}
