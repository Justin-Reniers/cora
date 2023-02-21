package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;

public interface UserCommand {

    /** For a user command, returns the position of the subterm to which the user command
        should be applied if the command has an argument position. */
    Position queryPosition();

    /** Returns whether the user command can be applied to the term. */
    boolean applicable(TRS lcTrs, Term t);

    /** Applies the user command to Term t, otherwise it returns null. */
    Term apply(TRS lcTrs, Term t);

    /** Gives a string representation of the current user command situation. */
    String toString();
}
