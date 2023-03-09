package cora.smt;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.terms.Var;

/**
 * Swap is a user command that switches the places of the left hand side and
 * right hand side arguments of an equivalence proof.
 */
public class SwapCommand extends UserCommandInherit implements UserCommand {

    private EquivalenceProof _proof;

    public SwapCommand() {
        super();
    };

    /**
     * Swap command has no position argument.
     */
    @Override
    public Position queryPosition() {
        return null;
    }

    /**
     * Swapping places is always a valid user command.
     */
    @Override
    public boolean applicable() {
        return true;
    }

    /**
     * Swaps the places of the left hand side term and right side term
     * of the equivalence proof.
     */
    @Override
    public void apply() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        _proof.setLeft(r);
        _proof.setRight(l);
    }

    /**
     * Sets the equivalence proof on which this user command should act.
     */
    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    /**
     * Swap does not need fresh variables.
     */
    @Override
    public Var getFreshVar(Type expectedType) {
        return null;
    }

    /**
     * This function is a string representation of the user command "swap".
     */
    @Override
    public String toString() {
        return "swap";
    }
}
