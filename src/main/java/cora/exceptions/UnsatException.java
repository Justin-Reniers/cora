package cora.exceptions;

import cora.interfaces.terms.Term;

public class UnsatException extends Error {
    public UnsatException(String s) {
        super("Constraint " + s + " is not satisfiable");
    }
}
