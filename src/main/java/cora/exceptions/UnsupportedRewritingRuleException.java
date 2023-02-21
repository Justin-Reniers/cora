package cora.exceptions;

import cora.interfaces.smt.UserCommand;

public class UnsupportedRewritingRuleException extends UnsupportedOperationException {
    private UserCommand _uCommand;
    public UnsupportedRewritingRuleException(UserCommand uCommand, String message) {
        super(message);
        _uCommand = uCommand;
    }

    public UnsupportedRewritingRuleException(String message) {
        super(message);
    }
}
