package cora.exceptions;

public class InvalidUserInputException extends Error {
    public InvalidUserInputException(String proof) {
        super("Given proof " + proof + " does not contain two terms and a constraint");
    }
}

