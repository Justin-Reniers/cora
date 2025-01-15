package cora.exceptions.invalidruleapplications;

public class InvalidCompletenessApplicationException extends InvalidRuleApplicationException {
    public InvalidCompletenessApplicationException(String msg) {
        super("Tried to apply completeness to proof where it is not applicable: " + msg);
    }
}