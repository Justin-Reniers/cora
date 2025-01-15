package cora.smt;

import cora.interfaces.smt.ProofEquation;
import cora.interfaces.terms.Term;

public class Equation implements ProofEquation {
    private Term _s, _t, _c;
    public Equation(Term s, Term t, Term c) {
        _s = s;
        _t = t;
        _c = c;
    }

    public Equation(Equation eq) {
        _s = eq.getLeft();
        _t = eq.getRight();
        _c = eq.getConstraint();
    }

    @Override
    public Term getLeft() { return _s; }

    @Override
    public Term getRight() { return _t; }

    @Override
    public Term getConstraint() { return _c; }

    @Override
    public String toString() {
        return _s.toString() + "\t" + _t.toString() + "\t[" + _c.toString() + "]";
    }

    @Override
    public String toHTMLString() {
        return _s.toHTMLString() + "\t≈" +
                "\t" + _t.toHTMLString() + "\t\t[" + _c.toHTMLString() + "]";
    }

    @Override
    public void setLeft(Term left) { if (left != null) _s = left; }

    @Override
    public void setRight(Term right) { if (right != null) _t = right; }

    @Override
    public void setConstraint(Term constraint) { if (constraint != null) _c = constraint; }
}
