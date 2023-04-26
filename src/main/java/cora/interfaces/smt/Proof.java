package cora.interfaces.smt;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.terms.Var;

import java.io.IOException;
import java.util.ArrayList;

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

    ArrayList<Equation> getEquations();

    Var getFreshVar(Type expectedType);

    String toString();

    String currentState();

    boolean applyNewUserCommand(String uCommand);

    void removeCurrentEquation();

    void addEquations(ArrayList<Equation> eqs);

    void addRule(Rule r);
}
