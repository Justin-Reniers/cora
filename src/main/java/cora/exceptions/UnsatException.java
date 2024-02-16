package cora.exceptions;

import cora.interfaces.terms.Term;

public class UnsatException extends Error {
    public UnsatException(String s, String t, String f) {
        super("Constraint " + s + " " + f + " " + t + " is not satisfiable");
    }

    public UnsatException(String s) {
        super("Constraint " + s + " is not satisfiable");
    }
}
