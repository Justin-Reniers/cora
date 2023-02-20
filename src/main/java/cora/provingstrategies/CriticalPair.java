package cora.provingstrategies;

import cora.interfaces.terms.Term;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof CriticalPair))
            return false;
        CriticalPair cp = (CriticalPair) o;
        if (!this.left.equals(cp.getLeft()))
            return false;
        return this.right.equals(cp.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(left.hashCode(), right.hashCode());
    }
}
