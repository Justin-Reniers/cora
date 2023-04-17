package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.types.Type;
import cora.loggers.Logger;
import cora.smt.EquivalenceProof;
import cora.terms.Var;
import cora.z3.Z3TermHandler;

public class DeleteCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public DeleteCommand() {
        super();
    }
    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        /**
         * TODO problem with match and two different variables, since it creates substitution, but equals doesn't work
         * TODO due to no prior knowledge of variables in use.
         */
        //Substitution s = _proof.getLeft().unify(_proof.getRight());
        //Logger.log(s.toString());
        //if (_proof.getLeft().substitute(s).equals(_proof.getRight())) return true;
        if (_proof.getLeft().equals(_proof.getRight())) return true;
        Z3TermHandler z3 = new Z3TermHandler();
        return !z3.satisfiable(_proof.getConstraint());
    }

    @Override
    public void apply() {
        _proof.removeCurrentEquation();
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        return null;
    }

    @Override
    public EquivalenceProof getProof() {
        return _proof;
    }
}
