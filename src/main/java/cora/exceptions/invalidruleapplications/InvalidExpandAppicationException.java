package cora.exceptions.invalidruleapplications;

public class InvalidExpandAppicationException extends InvalidRuleApplicationException {
    public InvalidExpandAppicationException(String msg) {
        super("Tried to apply expand to proof where it is not applicable: " + msg);
    }
}
