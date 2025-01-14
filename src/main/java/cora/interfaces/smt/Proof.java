package cora.interfaces.smt;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.ProofHistory;
import cora.terms.Var;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public interface Proof {

    TreeSet<Variable> getEquationVariables();
    boolean getBottom();

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

    void setCurrentEquation();

    void setCurrentEquation(Equation eq);

    ArrayList<Equation> getEquations();

    Var getFreshVar(Type expectedType);

    String toString();

    String currentState();

    TreeSet<Variable> getCurrentEqVariables();

    TreeSet<Variable> getRuleVariables(int ruleIndex);

    void applyNewUserCommand(String uCommand);

    void removeCurrentEquation();

    void clearEquations();

    void addEquations(ArrayList<Equation> eqs);

    void addEquation(Equation eq);

    boolean getCompleteness();

    void setCompleteness(boolean c);

    ArrayList<Equation> getCompletenessEquationSet();

    void setCompletenessEquationSet();

    void addCompletenessEquations(ArrayList<Equation> cEqs);

    void addCompletenessEquation(Equation eq);

    void emptyCompletenessEquationSet();

    void addRule(Rule r);

    void setBottom(boolean bottom);

    void setLcTrs(TRS lcTrs);

    ProofHistory getPreviousState();

    void deleteLastState();

    void recordHistory();

    int proofIsFinished();
}
