package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.types.Sort;
import cora.z3.Z3TermHandler;

import java.util.ArrayList;
import java.util.Arrays;

public class DisproveCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private ArrayList<String> _theorySymbols;

    public DisproveCommand() {
        super();
        _theorySymbols = new ArrayList<>(Arrays.asList("~", "/\\", "\\/", "-->", "<-->", "-", "*", "/", "%", "+",
                "<", ">", "<=", ">=", "==", "!="));
    }
    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        Term c = _proof.getConstraint();
        //s, t E Terms(Sigma_theory, V), i is a theory sort, and phi /\ s != t is satisfiable
        if ((l.queryType().equals(Sort.intSort) || l.queryType().equals(Sort.boolSort)) &&
            ((isNumeric(l.queryRoot().queryName()) && isNumeric(r.queryRoot().queryName()))) ||
            (isNumeric(l.queryRoot().queryName()) && _theorySymbols.contains(r.queryRoot().queryName())) ||
            (_theorySymbols.contains(l.queryRoot().queryName()) && isNumeric(r.queryRoot().queryName())) ||
            ((_theorySymbols.contains(l.queryRoot().queryName()) && _theorySymbols.contains(r.queryRoot().queryName())))
        ) {
            Term ineq = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("!="), l, r);
            Term nc = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, ineq);
            Z3TermHandler z3 = new Z3TermHandler();
            if (z3.satisfiable(nc)) return true;
        }
        //s = f(s->) and t = g(t->) with f, g distinct constructors and phi satisfiable

        //s E V\Var(phi), phi satisfiable, at least two different constructors have output sort i,
        //and either t is a variable distinct from s or t has the form g(t->) with g E Cons
        return false;
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void apply() {
        _proof.setBottom(true);
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
