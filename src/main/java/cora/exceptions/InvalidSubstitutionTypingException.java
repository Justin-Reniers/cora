package cora.exceptions;

import cora.interfaces.terms.Term;

public class InvalidSubstitutionTypingException extends Error {
    public InvalidSubstitutionTypingException(Term v, Term repl) {
        super("Unknown typing for replacement " + v.toString() + " := " + repl.toString());
    }
}
