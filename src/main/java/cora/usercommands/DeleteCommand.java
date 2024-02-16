package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Var;
import cora.z3.Z3TermHandler;

public class DeleteCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public DeleteCommand() {
        super();
    }
    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        if (_proof.getLeft().equals(_proof.getRight())) return true;
        Z3TermHandler z3 = new Z3TermHandler(_proof.getLcTrs());
        return !z3.satisfiable(_proof.getConstraint());
    }

    @Override
    public void apply() {
        _proof.removeCurrentEquation();
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
        return "delete";
    }
}
