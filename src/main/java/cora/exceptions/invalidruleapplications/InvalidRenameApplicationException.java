package cora.exceptions.invalidruleapplications;

public class InvalidRenameApplicationException extends InvalidRuleApplicationException {
    public InvalidRenameApplicationException(String msg) {
        super("Tried to apply rename to proof where it is not applicable: " + msg);
    }
}
