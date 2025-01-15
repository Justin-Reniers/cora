package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.exceptions.invalidruleapplications.InvalidUndoApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.smt.ProofHistory;
import cora.terms.Var;

public class UndoCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public UndoCommand() {
        super();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        try {
            _proof.getLastCommand();
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidUndoApplicationException("No previous state to undo to");
        }
        ProofHistory ph = _proof.getPreviousState();

        ps = ph.getProofState();
        _proof.setBottom(ph.getBottom());
        _proof.setLcTrs(ph.getLcTrs());
        _proof.deleteLastState();
        return ps;
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        return null;
    }

    @Override
    public EquivalenceProof getProof() {
        return _proof;
    }

    @Override
    public String toString() {
        return "undo";
    }
}
