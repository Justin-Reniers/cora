package cora.smt;

abstract class UserCommandInherit {

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected UserCommandInherit() {
    }
    public String toString() { return ""; }
}
