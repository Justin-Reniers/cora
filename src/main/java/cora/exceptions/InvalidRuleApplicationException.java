package cora.exceptions;

public class InvalidRuleApplicationException extends Error {
    public InvalidRuleApplicationException(String userCommand) {
        super("Tried to apply " + userCommand + " to proof where it is not applicable");
    }
}
