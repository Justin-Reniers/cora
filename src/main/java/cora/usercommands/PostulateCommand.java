package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.Var;

public class PostulateCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Term _l, _r, _c;

    public PostulateCommand(Term l, Term r, Term c) {
        super();
        _l = l;
        _r = r;
        _c = c;
    }

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
        _proof.addEquation(new Equation(_l, _r, _c));
        _proof.setCompleteness(false);
        _proof.setCompletenessEquationSet();
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
