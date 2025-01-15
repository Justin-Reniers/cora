package cora.exceptions.invalidruleapplications;

public class InvalidEQDeleteApplicationException extends InvalidRuleApplicationException {
    public InvalidEQDeleteApplicationException(String msg) {
        super("Tried to apply eq-delete to proof where it is not applicable: " + msg);
    }
}
