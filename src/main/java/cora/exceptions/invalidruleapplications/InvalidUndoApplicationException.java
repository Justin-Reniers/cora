package cora.exceptions.invalidruleapplications;

public class InvalidUndoApplicationException extends InvalidRuleApplicationException {
        public InvalidUndoApplicationException(String msg) {
            super("Tried to apply undo to proof where it is not applicable: " + msg);
        }
}
