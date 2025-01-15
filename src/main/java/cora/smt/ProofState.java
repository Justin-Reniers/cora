package cora.smt;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.rewriting.FirstOrderRule;
import cora.interfaces.smt.IProofState;
import cora.terms.FunctionalTerm;

import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.TreeSet;

public class ProofState implements IProofState {
    private final ArrayList<ProofEquation> _E;
    private final ArrayList<ProofEquation> _completenessE;
    private final ArrayList<FirstOrderRule> _H;
    private boolean _completeness;

    public ProofState(ArrayList<ProofEquation> E, ArrayList<ProofEquation> completenessE, ArrayList<FirstOrderRule> H,
                      boolean completeness) {
        _E = new ArrayList<>();
        _E.addAll(E);
        _completenessE = new ArrayList<>();
        _completenessE.addAll(completenessE);
        _H = new ArrayList<>();
        _H.addAll(H);
        _completeness = completeness;
    }

    public ProofState(ArrayList<FirstOrderRule> H, boolean completeness) {
        _E = new ArrayList<>();
        _completenessE = new ArrayList<>();
        _H = new ArrayList<>();
        _H.addAll(H);
        _completeness = completeness;
    }

    public ArrayList<ProofEquation> getE() {
        return _E;
    }

    @Override
    public Term getS() {
        return _E.get(0).getLeft();
    }

    @Override
    public Term getT() {
        return _E.get(0).getRight();
    }

    @Override
    public Term getC() {
        return _E.get(0).getConstraint();
    }

    public ArrayList<ProofEquation> getCompletenessE() {
        return _completenessE;
    }

    @Override
    public void setCompletenessE(ArrayList<ProofEquation> eqs) {
        _completenessE.addAll(eqs);
    }

    @Override
    public void emptyCompletenessSet() {
        _completenessE.clear();
    }

    public ArrayList<FirstOrderRule> getH() {
        return _H;
    }

    @Override
    public ProofEquation getCurrentEquation() {
        if (_E.isEmpty()) return null;
        return _E.get(0);
    }

    @Override
    public void removeCurrentEquation() {
        try {
            _E.remove(_E.remove(0));
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Override
    public void addEquations(ArrayList<ProofEquation> eqs) {
        for (ProofEquation eq : eqs) {
            if (!_E.contains(eq)) _E.add(eq);
        }
    }

    @Override
    public void setS(Term s) {
        _E.get(0).setLeft(s);
    }

    @Override
    public void setT(Term t) {
        _E.get(0).setRight(t);
    }

    @Override
    public void setC(Term c) {
        _E.get(0).setConstraint(c);
    }

    @Override
    public TreeSet<Variable> getCurrentEqVariables() {
        TreeSet<Variable> vars = new TreeSet<>();
        try {
            vars.addAll(_E.get(0).getLeft().vars().getVars());
            vars.addAll(_E.get(0).getRight().vars().getVars());
            vars.addAll(_E.get(0).getConstraint().vars().getVars());
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {

        }
        return vars;
    }

    public boolean getCompleteness() {
        return _completeness;
    }

    @Override
    public void setCompleteness(boolean completeness) {
        _completeness = completeness;
    }

    @Override
    public String toString() {
        StringJoiner eqs = new StringJoiner(", ", "{", "}");
        for (ProofEquation e : _E) eqs.add(e.toString());
        StringJoiner hs = new StringJoiner(", ", "{", "}");
        for (FirstOrderRule r : _H) hs.add(r.toString());
        return "(" + eqs + ", " + hs + ", " + _completeness + ")";
    }
}
