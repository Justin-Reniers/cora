package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.TreeSet;

import static cora.types.Sort.boolSort;
import static cora.types.Sort.intSort;

public class EQDeleteCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public EQDeleteCommand() {
        super();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        return l.queryRoot().equals(r.queryRoot());
    }

    @Override
    public void apply() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        ArrayList<Term> ineqs = new ArrayList<>();
        TreeSet<Variable> vars = _proof.getConstraint().vars().getVars();
        compareTerms(l, r, ineqs, vars);
        Term ct = constructConstraintAddition(ineqs);
        _proof.setConstraint(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"),
                _proof.getConstraint(), ct));
    }

    private void compareTerms(Term l, Term r, ArrayList<Term> ineqs, TreeSet<Variable> vars) {
        for (int i = 1; i <= l.numberImmediateSubterms() || i <= r.numberImmediateSubterms(); i++) {
            Term lt = l.queryImmediateSubterm(i);
            Term rt = r.queryImmediateSubterm(i);
            if (lt.equals(rt)) continue;
            else if (lt.isVariable() && rt.isVariable() && vars.contains((Variable) lt) &&
                    vars.contains((Variable) rt) && !lt.equals(rt)) {
                if (lt.queryType().equals(intSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), lt, rt));
                } else if (lt.queryType().equals(boolSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==b"), lt, rt));
                }
            } else if (lt.isVariable() && vars.contains((Variable) lt)) {
                if (lt.queryType().equals(intSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), lt, rt));
                } else if (lt.queryType().equals(boolSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==b"), lt, rt));
                }
            } else if (rt.isVariable() && vars.contains((Variable) rt)) {
                if (lt.queryType().equals(intSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), rt, lt));
                } else if (lt.queryType().equals(boolSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==b"), rt, lt));
                }
            } else if (_proof.getLcTrs().lookupSymbol(lt.queryRoot().queryName()) != null &&
                    _proof.getLcTrs().lookupSymbol(rt.queryRoot().queryName()) != null &&
                    lt.queryRoot().equals(rt.queryRoot())) {
                for (int j = 1; j <= lt.numberImmediateSubterms(); j++) compareTerms(lt, rt, ineqs, vars);
            } else {
                if (lt.queryType().equals(intSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), lt, rt));
                } else if (lt.queryType().equals(boolSort)) {
                    ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==b"), lt, rt));
                }
            }
        }
    }

    private Term constructConstraintAddition(ArrayList<Term> ineqs) {
        if (ineqs.isEmpty()) return null;
        Term c = ineqs.get(0);
        for (int i = 1; i < ineqs.size(); i++) {
            Term t = ineqs.get(i);
            c = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, t);
        }
        return new FunctionalTerm(_proof.getLcTrs().lookupSymbol("~"), c);
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
        return "eq-delete";
    }
}
