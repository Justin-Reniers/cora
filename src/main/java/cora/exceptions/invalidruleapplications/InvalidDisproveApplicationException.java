package cora.exceptions.invalidruleapplications;

public class InvalidDisproveApplicationException extends InvalidRuleApplicationException {
    public InvalidDisproveApplicationException(String msg) {
        super("Tried to apply disprove to proof where it is not applicable: " + msg);
    }
}
