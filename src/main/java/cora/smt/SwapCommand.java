package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;

public class SwapCommand extends UserCommandInherit implements UserCommand {

    public SwapCommand() {};

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable(TRS lcTrs, Term t, Term constraint) {
        return true;
    }

    @Override
    public Term apply(TRS lcTrs, Term t, Term constraint) {
        return null;
    }

    @Override
    public String toString() {
        return "swap";
    }
}
