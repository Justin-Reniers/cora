package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

import java.util.ArrayList;

public class ConstructorCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public ConstructorCommand() {
        super();

    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        return (_proof.getLeft().queryRoot().equals(_proof.getRight().queryRoot()) &&
                isConstructorTerm(_proof.getLeft(), _proof));
    }

    @Override
    public void apply() {
        ArrayList<Equation> eqs = new ArrayList<>();
        for (int i = 1; i <= _proof.getLeft().numberImmediateSubterms(); i++) {
            Term li = _proof.getLeft().queryImmediateSubterm(i);
            Term ri = _proof.getRight().queryImmediateSubterm(i);
            eqs.add(new Equation(li, ri, _proof.getConstraint()));
        }
        _proof.removeCurrentEquation();
        _proof.addEquations(eqs);
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
    public EquivalenceProof getProof() {
        return _proof;
    }
}
