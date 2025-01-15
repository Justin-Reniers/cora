package cora.exceptions.invalidruleapplications;

public class InvalidSwapApplicationException extends InvalidRuleApplicationException {
    public InvalidSwapApplicationException(String msg) {
        super("Tried to apply swap to proof where it is not applicable: " + msg);
    }
}
