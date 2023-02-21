package cora.smt;

import cora.exceptions.NullInitialisationError;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

abstract class UserCommandInherit {
    protected Term _constraint;

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected UserCommandInherit() {
    }
    public String toString() { return ""; }
}
