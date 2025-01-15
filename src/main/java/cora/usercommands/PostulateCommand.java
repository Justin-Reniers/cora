package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.Collections;

public class PostulateCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Term _s, _t, _c;

    public PostulateCommand(Term s, Term t, Term c) {
        super();
        _s = s;
        _t = t;
        _c = c;
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        ps.addEquations(new ArrayList<>(Collections.singleton(new Equation(_s, _t, _c))));
        ps.setCompleteness(false);
        ps.setCompletenessE(ps.getE());
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
        return "postulate " + _s + " " + _t + " [" + _c + "]";
    }
}
