package cora.usercommands;

import cora.exceptions.UnsatException;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Subst;
import cora.terms.Var;
import cora.z3.Z3TermHandler;

import java.util.ArrayList;

public class RewriteConstraintCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Term _old, _new, _newConstraint;
    private ArrayList<Term> _proofComponents, _oldConstraintComponents;
    private Substitution _s;
    private boolean _completeness;

    public RewriteConstraintCommand(Term old, Term n) {
        super();
        _old = old;
        _new = n;
        _proofComponents = new ArrayList<>();
        _oldConstraintComponents = new ArrayList<>();
        _s = new Subst();
        _newConstraint = null;
        _completeness = true;
    }
    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        listifyConstraint(_proofComponents, _proof.getConstraint());
        listifyConstraint(_oldConstraintComponents, _old);
        for (Term t : _oldConstraintComponents) {
            boolean inProof = false;
            for (Term l : _proofComponents) {
                Substitution s = t.unify(l);
                if (s != null) {
                    for (Variable v : s.domain()) {
                        if (!(s.getReplacement(v) instanceof Var)) {
                            return false;
                        }
                        _s.extend(v, s.getReplacement(v));
                    }
                }
                if (t.substitute(_s).equals(l)) inProof = true;
            }
            if (!inProof) return false;
        }
        Term c = _proof.getConstraint();

        _oldConstraintComponents.replaceAll(term -> term.substitute(_s));
        _proofComponents.removeAll(_oldConstraintComponents);
        _proofComponents.add(_new.substitute(_s));
        Term nc = reconstructConstraint(_proofComponents);
        Z3TermHandler z3 = new Z3TermHandler(_proof.getLcTrs());
        if (!z3.validity(c, nc, _proof.getLcTrs().lookupSymbol("<-->"))) {
            if (!z3.validity(c, nc, _proof.getLcTrs().lookupSymbol("-->"))) {
                throw new UnsatException(c.toString(), nc.toString(), "<-->");
            }
            _completeness = false;
        }
        _newConstraint = nc;
        return true;
    }

    @Override
    public void apply() {
        _proof.setConstraint(_newConstraint);
        if (!_completeness) _proof.setCompleteness(_completeness);
        /**_oldConstraintComponents.replaceAll(term -> term.substitute(_s));
        _proofComponents.removeAll(_oldConstraintComponents);
        _proofComponents.add(_new.substitute(_s));
        _proof.setConstraint(reconstructConstraint(_proofComponents));**/
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
        return "rewrite " + _old + " " + _new;
    }

    private void listifyConstraint(ArrayList<Term> components, Term c) {
        if (c.isFunctionalTerm() && c.queryRoot().queryName().equals("/\\")) {
            for (int i = 0; i < c.numberImmediateSubterms(); i++) {
                listifyConstraint(components, c.queryImmediateSubterm(1));
                listifyConstraint(components, c.queryImmediateSubterm(2));
            }
        } else if (!components.contains(c)) components.add(c);
    }

    private Term reconstructConstraint(ArrayList<Term> components) {
        Term c = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            c = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, components.get(i));
        }
        return c;
    }
}
