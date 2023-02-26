package cora.smt;

abstract class UserCommandInherit {
    private EquivalenceProof _equivalenceProof;

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected UserCommandInherit() {
        _equivalenceProof = null;
    }
}
