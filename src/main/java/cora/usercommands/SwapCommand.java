package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

import java.util.Collections;

/**
 * Swap is a user command that switches the places of the left hand side and
 * right hand side arguments of an equivalence proof.
 */
public class SwapCommand extends UserCommandInherit implements UserCommand {

    private EquivalenceProof _proof;
    private int _eq1, _eq2;

    public SwapCommand() {
        super();
        _eq1 = -1;
        _eq2 = -1;
    };

    public SwapCommand(int eq1, int eq2) {
        super();
        _eq1 = eq1 - 1;
        _eq2 = eq2 - 1;
    }

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
        if (_eq1 < 0 || _eq2 < 0) {
            Equation eq = new Equation(_proof.getRight(), _proof.getLeft(), _proof.getConstraint());
            _proof.setCurrentEquation(eq);
        } else if (_eq1 < _proof.getEquations().size() && _eq2 < _proof.getEquations().size()) {
            Collections.swap(_proof.getEquations(), _eq1, _eq2);
            _proof.setCurrentEquation();
        }
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

    @Override
    public EquivalenceProof getProof() {
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
