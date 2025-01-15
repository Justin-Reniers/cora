package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidRenameApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Subst;
import cora.terms.Var;

import java.util.TreeSet;

public class RenameCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Variable _old, _new;
    private Substitution _s;

    public RenameCommand() {
        _old = _new = null;
        _s = new Subst();
    }

    public RenameCommand(Term old, Term n) {
        if (old instanceof Var) _old = (Var) old;
        if (n instanceof Var) _new = (Var) n;
        _s = new Subst();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (_old == null || _new == null) throw new InvalidRenameApplicationException("Old or new variable is null");
        if (ps.getCurrentEqVariables().contains(_new)) {
            throw new InvalidRenameApplicationException(_new + " already exists");
        }
        _s.extend(_old, _new);
        ps.setS(ps.getS().substitute(_s));
        ps.setT(ps.getT().substitute(_s));
        ps.setC(ps.getC().substitute(_s));
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
        return "rename " + _old + " " + _new;
    }
}
