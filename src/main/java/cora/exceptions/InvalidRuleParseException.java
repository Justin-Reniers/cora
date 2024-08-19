package cora.exceptions;

public class InvalidRuleParseException extends Error {
    public InvalidRuleParseException(String uCommand) {
        super("Tried to parse " + uCommand + " but could not");
    }
}
