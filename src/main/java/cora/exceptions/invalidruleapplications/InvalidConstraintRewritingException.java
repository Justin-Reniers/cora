package cora.exceptions.invalidruleapplications;

public class InvalidConstraintRewritingException extends Exception {
    public InvalidConstraintRewritingException(String msg) {
        super("Tried to apply rewrite to proof where it is not applicable: " + msg);
    }
}
