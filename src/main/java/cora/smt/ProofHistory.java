package cora.smt;

import cora.interfaces.smt.History;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;

public class ProofHistory implements History {
    private final Term _left, _right, _constraint;
    private final UserCommand _uCommand;

    public ProofHistory(Term left, Term right, Term constraint, UserCommand uCommand) {
        _left = left;
        _right = right;
        _constraint = constraint;
        _uCommand = uCommand;
    }

    public String toString() {
        return _left.toString() + ",\t" + _right.toString() + ",\t" +
                _constraint.toString() + "\t" + (_uCommand != null ? _uCommand.toString() : "");
    }

    public Term getLeft() {
        return _left;
    }

    public Term getRight() {
        return _right;
    }

    public Term getConstraint() {
        return _constraint;
    }

    public UserCommand getUserCommand() {
        return _uCommand;
    }
}
