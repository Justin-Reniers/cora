package cora.interfaces.smt;

import cora.smt.Equation;

import java.util.ArrayList;

public interface History {
    String toString();

    ArrayList<Equation> getEquations();

    UserCommand getUserCommand();
}
