package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidConstructorApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
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
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (!(_proof.getLeft().queryRoot().equals(_proof.getRight().queryRoot()))) {
            throw new InvalidConstructorApplicationException(ps.getS() + " and " + ps.getT() + " do not share root");
        } else if (!isConstructorTerm(ps.getS(), _proof)) {
            throw new InvalidConstructorApplicationException(ps.getS() + " is not a constructor term");
        }

        ArrayList<ProofEquation> eqs = new ArrayList<>();
        for (int i = 1; i <= ps.getS().numberImmediateSubterms(); i++) {
            Term li = ps.getS().queryImmediateSubterm(i);
            Term ri = ps.getT().queryImmediateSubterm(i);
            eqs.add(new Equation(li, ri, ps.getC()));
        }
        ps.removeCurrentEquation();
        ps.addEquations(eqs);
        return ps;
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

    @Override
    public String toString() {
        return "constructor";
    }
}
