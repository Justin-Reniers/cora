package cora.smt;

import cora.interfaces.smt.History;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;

/**
 * A Proof History is an object that contains a single state of an equivalence proof.
 * It serves as a record type for
 */
public class ProofHistory implements History {
    private final Term _left, _right, _constraint;
    private final UserCommand _uCommand;

    /**
     * This constructor is used to create a Proof History record.
     */
    public ProofHistory(Term left, Term right, Term constraint, UserCommand uCommand) {
        _left = left;
        _right = right;
        _constraint = constraint;
        _uCommand = uCommand;
    }

    /**
     * This function gives a String representation of a proof history record.
     */
    public String toString() {
        return _left.toString() + ",\t" + _right.toString() + ",\t" +
                _constraint.toString() + (_uCommand != null ? "\n" + _uCommand.toString() : "");
    }

    /**
     * Returns the left argument of the equivalence proof at this proof state.
     */
    public Term getLeft() {
        return _left;
    }

    /**
     * Returns the right argument of the equivalence proof at this proof state.
     */
    public Term getRight() {
        return _right;
    }

    /**
     * Returns the constraint of the equivalence proof at this proof state.
     */
    public Term getConstraint() {
        return _constraint;
    }

    /**
     * Returns the user command to be applied to the equivalence proof at this proof state.
     */
    public UserCommand getUserCommand() {
        return _uCommand;
    }
}
