package cora.usercommands;

import com.microsoft.z3.IntSort;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
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
    private ArrayList<Term> _ineqs;

    public EQDeleteCommand() {
        super();
        _ineqs = new ArrayList<Term>();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        if (!l.queryRoot().equals(r.queryRoot())) return false;
        ArrayList<FunctionSymbol> tSymbs = (ArrayList<FunctionSymbol>) _proof.getLcTrs().queryTheorySymbols();
        TreeSet<Variable> vars = _proof.getConstraint().vars().getVars();
        _ineqs = new ArrayList<>();
        for (int i = 1; i <= l.numberImmediateSubterms(); i++) {
            Term li = l.queryImmediateSubterm(i);
            Term ri = r.queryImmediateSubterm(i);
            if (li.isVariable() && !vars.contains(li)) return false;
            if (ri.isVariable() && !vars.contains(ri)) return false;
            if ((li.isVariable() && vars.contains(li) && tSymbs.contains(ri.queryRoot())) ||
                    (ri.isVariable() && vars.contains(ri) && tSymbs.contains(li.queryRoot()))
                    && li.queryType().equals(ri.queryType())) {
                if (li.queryType().equals(intSort)) {
                    _ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), li, ri));
                }
                else if (li.queryType().equals(boolSort)) {
                    _ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("<-->"), li, ri));
                }
            }
            else if (tSymbs.contains(li.queryRoot()) && tSymbs.contains(ri.queryRoot())) {
                if (li.queryRoot().equals(ri.queryRoot())) {
                    if (li.queryType().equals(intSort)) {
                        _ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), li, ri));
                    }
                    if (li.queryType().equals(boolSort)) {
                        _ineqs.add(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("<-->"), li, ri));
                    }
                }
            } else return false;
        }
        return true;
    }

    @Override
    public void apply() {
        Term ct = constructConstraintAddition(_ineqs);
        _proof.setConstraint(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"),
                _proof.getConstraint(), ct));
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
