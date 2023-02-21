package cora.smt;

import cora.interfaces.terms.Term;

public class ProofHistory {
    private Term _left, _right;
    private UserCommandInherit _uCommand;

    public ProofHistory(Term left, Term right, UserCommandInherit uCommand) {
        _left = left;
        _right = right;
        _uCommand = uCommand;
    }

    public String toString() {
        return _left.toString() + ",\t" + _right.toString() + ",\t" + _uCommand.toString();
    }
}
