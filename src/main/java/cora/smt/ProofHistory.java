package cora.smt;

import cora.interfaces.smt.History;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;

import java.util.ArrayList;

/**
 * A Proof History is an object that contains a single state of an equivalence proof.
 * It serves as a record type for
 */
public class ProofHistory implements History {
    private final ArrayList<Equation> _eqs;
    private final boolean _completeness;

    private final UserCommand _uCommand;

    /**
     * This constructor is used to create a Proof History record.
     */
    public ProofHistory(ArrayList<Equation> eqs, UserCommand uCommand, boolean completeness) {
        _eqs = eqs;
        _uCommand = uCommand;
        _completeness = completeness;
    }

    /**
     * This function gives a String representation of a proof history record.
     */
    public String toString() {
        return _eqs.toString() + "\tCompleteness: " + _completeness +
                (_uCommand != null ? "\n" + _uCommand.toString() : "");
    }

    @Override
    public ArrayList<Equation> getEquations() {
        return _eqs;
    }

    /**
     * Returns the user command to be applied to the equivalence proof at this proof state.
     */
    public UserCommand getUserCommand() {
        return _uCommand;
    }
}
