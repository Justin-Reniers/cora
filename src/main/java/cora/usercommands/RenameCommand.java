package cora.usercommands;

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
    public boolean applicable() {
        if (_old == null || _new == null) return false;
        TreeSet<Variable> env = new TreeSet<>();
        env.addAll(_proof.getLeft().vars().getVars());
        env.addAll(_proof.getRight().vars().getVars());
        env.addAll(_proof.getConstraint().vars().getVars());
        return !env.contains(_new);
    }

    @Override
    public void apply() {
        _s.extend(_old, _new);
        _proof.setLeft(_proof.getLeft().substitute(_s));
        _proof.setRight(_proof.getRight().substitute(_s));
        _proof.setConstraint(_proof.getConstraint().substitute(_s));
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
