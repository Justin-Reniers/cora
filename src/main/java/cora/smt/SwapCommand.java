package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;

public class SwapCommand extends UserCommandInherit implements UserCommand {

    private EquivalenceProof _proof;

    public SwapCommand() {
        _proof = null;
    };

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        return true;
    }

    @Override
    public void apply() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        _proof.setLeft(r);
        _proof.setRight(l);
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public String toString() {
        return "swap";
    }
}
