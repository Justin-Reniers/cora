package cora.smt;

import cora.interfaces.smt.History;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;

/**
 * A Proof History is an object that contains a single state of an equivalence proof.
 * It serves as a record type for
 */
public class ProofHistory implements History {
    private final Equation _eq;
    private final UserCommand _uCommand;

    /**
     * This constructor is used to create a Proof History record.
     */
    public ProofHistory(Equation eq, UserCommand uCommand) {
        _eq = eq;
        _uCommand = uCommand;
    }

    /**
     * This function gives a String representation of a proof history record.
     */
    public String toString() {
        return _eq.getLeft().toString() + ",\t" + _eq.getRight().toString() + ",\t" +
                _eq.getConstraint().toString() + (_uCommand != null ? "\n" + _uCommand.toString() : "");
    }

    /**
     * Returns the left argument of the equivalence proof at this proof state.
     */
    public Term getLeft() { return _eq.getLeft(); }

    /**
     * Returns the right argument of the equivalence proof at this proof state.
     */
    public Term getRight() {
        return _eq.getRight();
    }

    /**
     * Returns the constraint of the equivalence proof at this proof state.
     */
    public Term getConstraint() {
        return _eq.getConstraint();
    }

    /**
     * Returns the user command to be applied to the equivalence proof at this proof state.
     */
    public UserCommand getUserCommand() {
        return _uCommand;
    }
}
