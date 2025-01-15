package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidCompletenessApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
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
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (ps.getCompleteness()) throw new InvalidCompletenessApplicationException("Completeness flag is COMPLETE");
        for (ProofEquation eq : ps.getE()) {
            if (!ps.getCompletenessE().contains(eq)) throw new InvalidCompletenessApplicationException(eq + " not" +
                    " in completeness set");
        }
        ps.setCompleteness(true);
        ps.emptyCompletenessSet();
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
    public EquivalenceProof getProof() { return _proof; }

    @Override
    public String toString() {
        return "completeness";
    }
}
