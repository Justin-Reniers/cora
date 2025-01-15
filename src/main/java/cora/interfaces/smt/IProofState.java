package cora.interfaces.smt;

import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.rewriting.FirstOrderRule;

import java.util.ArrayList;
import java.util.TreeSet;

public interface IProofState {
    ArrayList<ProofEquation> getE();
    
    Term getS();
    
    Term getT();
    
    Term getC();

   ArrayList<ProofEquation> getCompletenessE();

   void setCompletenessE(ArrayList<ProofEquation> eqs);
   
   void emptyCompletenessSet();

    ArrayList<FirstOrderRule> getH();

    ProofEquation getCurrentEquation();

    TreeSet<Variable> getCurrentEqVariables();

    boolean getCompleteness();
    
    void setCompleteness(boolean completeness);

    void removeCurrentEquation();

    void addEquations(ArrayList<ProofEquation> eqs);

    void setC(Term c);

    void setT(Term t);

    void setS(Term s);
}
