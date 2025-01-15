package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;

import java.util.ArrayList;

public interface History {
    String toString();

    IProofState getProofState();

    ArrayList<ProofEquation> getEquations();

    ArrayList<ProofEquation> getCompletenessEquations();

    boolean getCompleteness();

    UserCommand getUserCommand();

    boolean getBottom();

    TRS getLcTrs();
}
