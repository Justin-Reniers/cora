package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.terms.Var;

import java.io.IOException;

public interface Proof {
    public void saveStateToFile(String filePath) throws IOException;

    UserCommand getLastCommand();

    TRS getLcTrs();

    Term getLeft();

    void setLeft(Term t);

    Term getRight();

    void setRight(Term t);

    Term getConstraint();

    void setConstraint(Term t);

    Equation getCurrentEquation();

    Var getFreshVar(Type expectedType);

    String toString();

    String currentState();

    boolean applyNewUserCommand(String uCommand);

    void removeCurrentEquation();
}
