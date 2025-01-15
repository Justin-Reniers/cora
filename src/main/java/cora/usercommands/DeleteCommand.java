package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidDeleteApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Var;
import cora.z3.SatisfiabilityEnum;
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
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (ps.getS().equals(ps.getT())) {
            ps.removeCurrentEquation();
            return ps;
        }
        Z3TermHandler z3 = new Z3TermHandler(_proof.getLcTrs());
        if (z3.satisfiable(_proof.getConstraint()) == SatisfiabilityEnum.UNSAT) ps.removeCurrentEquation();
        else throw new InvalidDeleteApplicationException("No delete cases apply");
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
        return "delete";
    }
}
