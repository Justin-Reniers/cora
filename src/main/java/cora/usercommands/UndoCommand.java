package cora.usercommands;

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
    public boolean applicable() {
        return _proof.getLastCommand() != null;
    }

    @Override
    public void apply() {
        ProofHistory ph = _proof.getPreviousState();
        _proof.clearEquations();
        for (Equation eq : ph.getEquations()) _proof.addEquation(new Equation(eq));
        if (_proof.getEquations().size() > 0) _proof.setCurrentEquation();
        _proof.emptyCompletenessEquationSet();
        for (Equation ceq : ph.getCompletenessEquations()) _proof.addCompletenessEquation(new Equation(ceq));
        _proof.setCompleteness(ph.getCompleteness());
        _proof.setBottom(ph.getBottom());
        _proof.setLcTrs(ph.getLcTrs());
        _proof.deletePreviousState();
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
}
