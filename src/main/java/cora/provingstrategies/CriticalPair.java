package cora.provingstrategies;

import cora.interfaces.terms.Term;

public class CriticalPair {

    private final Term left;
    private final Term right;

    /**
     * Constructor for a CriticalPair. Creates a unmodifiable critical pair for the
     * given left and right term.
     */
    public CriticalPair(Term left, Term right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left term of the critical pair.
     */
    public Term getLeft() {
        return left;
    }

    /**
     * Returns the right term of the critical pair.
     */
    public Term getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "[" + left.toString() + ", " + right.toString() + "]";
    }
}
