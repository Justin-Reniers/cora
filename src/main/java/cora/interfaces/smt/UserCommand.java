package cora.interfaces.smt;

import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.terms.Position;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

public interface UserCommand {

    /** For a user command, returns the position of the subterm to which the user command
        should be applied if the command has an argument position. */
    Position queryPosition();

    /**
     * Applies the user command to the given proof state, otherwise it throws an error.
     */
    IProofState apply(IProofState ps) throws InvalidRuleApplicationException;

    /** Gives a string representation of the current user command situation. */
    String toString();

    void setProof(EquivalenceProof proof);

    Var getFreshVar(Type expectedType);

    EquivalenceProof getProof();
}
