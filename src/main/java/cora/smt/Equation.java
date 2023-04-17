package cora.smt;

import cora.interfaces.smt.ProofEquation;
import cora.interfaces.terms.Term;

public class Equation implements ProofEquation {
    private Term _left, _right, _constraint;
    public Equation(Term l, Term r, Term c) {
        _left = l;
        _right = r;
        _constraint = c;
    }

    @Override
    public Term getLeft() { return _left; }

    @Override
    public Term getRight() { return _right; }

    @Override
    public Term getConstraint() { return  _constraint; }

    @Override
    public String toString() {
        return _left.toString() + "\t" + _right.toString() + "\t[" + _constraint.toString() + "]";
    }

    @Override
    public void setLeft(Term left) { if (left != null) _left = left; }

    @Override
    public void setRight(Term right) { if (right != null) _right = right; }

    @Override
    public void setConstraint(Term constraint) { if (constraint != null) _constraint = constraint; }
}
