package cora.exceptions.invalidruleapplications;

public class InvalidRewriteApplicationException extends InvalidRuleApplicationException {
    public InvalidRewriteApplicationException(String msg) {
        super ("Tried to apply rewrite to proof where it is not applicable: " + msg);
    }
}
