package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

public class CompletenessCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public CompletenessCommand() {
        super();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        for (Equation eq : _proof.getEquations()) {
            if (!_proof.getCompletenessEquationSet().contains(eq)) return false;
        }
        return !_proof.getCompleteness();
    }

    @Override
    public void apply() {
        _proof.setCompleteness(true);
        _proof.emptyCompletenessEquationSet();
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        return _proof.getFreshVar(expectedType);
    }

    @Override
    public EquivalenceProof getProof() { return _proof; }

    @Override
    public String toString() {
        return "completeness";
    }
}
