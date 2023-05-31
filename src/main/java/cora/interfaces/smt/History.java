package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;
import cora.smt.Equation;

import java.util.ArrayList;

public interface History {
    String toString();

    ArrayList<Equation> getEquations();

    ArrayList<Equation> getCompletenessEquations();

    boolean getCompleteness();

    UserCommand getUserCommand();

    boolean getBottom();

    TRS getLcTrs();
}
