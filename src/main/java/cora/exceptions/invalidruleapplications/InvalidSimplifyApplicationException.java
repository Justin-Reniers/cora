package cora.exceptions.invalidruleapplications;

public class InvalidSimplifyApplicationException extends InvalidRuleApplicationException {
    public InvalidSimplifyApplicationException(String msg) {
        super("Tried to apply simplify to proof where it is not applicable: " + msg);
    }
}
