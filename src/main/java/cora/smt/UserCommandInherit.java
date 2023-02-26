package cora.smt;

/**
 * A "User Command" is a rewriting rule that is to be applied to an equivalence proof.
 * This inherit provides default functionality for such rewriting rules.
 */
abstract class UserCommandInherit {
    private EquivalenceProof _equivalenceProof;

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected UserCommandInherit() {
        _equivalenceProof = null;
    }
}
