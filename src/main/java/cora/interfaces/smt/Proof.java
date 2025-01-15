package cora.interfaces.smt;

import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
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

    ProofEquation getCurrentEquation();

    ArrayList<ProofEquation> getEquations();

    Var getFreshVar(Type expectedType);

    String toString();

    TreeSet<Variable> getCurrentEqVariables();

    TreeSet<Variable> getRuleVariables(int ruleIndex);

    void applyNewUserCommand(String uCommand) throws InvalidRuleApplicationException;

    void addEquation(ProofEquation eq);

    boolean getCompleteness();

    void setCompleteness(boolean c);

    void addRule(Rule r);

    void setBottom(boolean bottom);

    void setLcTrs(TRS lcTrs);

    ProofHistory getPreviousState();

    void deleteLastState();

    int proofIsFinished();

    IProofState getProofState();
}
