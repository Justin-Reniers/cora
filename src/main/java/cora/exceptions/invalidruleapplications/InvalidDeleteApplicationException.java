package cora.exceptions.invalidruleapplications;

public class InvalidDeleteApplicationException extends InvalidRuleApplicationException {
    public InvalidDeleteApplicationException(String msg) {
        super("Tried to apply delete to proof where it is not applicable: " + msg);
    }
}
