package cora.exceptions.invalidruleapplications;

public class InvalidConstructorApplicationException extends InvalidRuleApplicationException {
    public InvalidConstructorApplicationException(String msg) {
        super("Tried to apply constructor to proof where it is not applicable: " + msg);
    }
}
