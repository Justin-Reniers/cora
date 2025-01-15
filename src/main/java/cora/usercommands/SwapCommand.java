package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.exceptions.invalidruleapplications.InvalidSwapApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
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
    private Integer _eq1, _eq2;
    private boolean _stSwap;

    public SwapCommand() {
        super();
        _eq1 = null;
        _eq2 = null;
        _stSwap = true;
    };

    public SwapCommand(int eq1, int eq2) {
        super();
        _eq1 = eq1 - 1;
        _eq2 = eq2 - 1;
        _stSwap = false;
    }

    /**
     * Swap command has no position argument.
     */
    @Override
    public Position queryPosition() {
        return null;
    }

    /**
     * Swaps the places of the left hand side term and right side term
     * of the equivalence proof.
     *
     * @return
     */
    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (_stSwap && ps.getCurrentEquation() == null) {
            throw new InvalidSwapApplicationException("No equation to swap s and t");
        } else if (!_stSwap && (_eq1 < 0 || _eq2 < 0 || _eq1 >= ps.getE().size() || _eq2 >= ps.getE().size())) {
            throw new InvalidSwapApplicationException("Equation indices out of bounds");
        } else if (_eq1 == null && _eq2 == null) {
            ProofEquation eq = new Equation(ps.getT(), ps.getS(), ps.getC());
            ps.getE().set(0, eq);
        } else if (_eq1 < ps.getE().size() && _eq2 < ps.getE().size()) {
            Collections.swap(ps.getE(), _eq1, _eq2);
        }
        return ps;
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
        if (_eq1 == null || _eq2 == null) return "swap";
        else return "swap " + (_eq1 + 1) + " " + (_eq2 + 1);
    }
}
