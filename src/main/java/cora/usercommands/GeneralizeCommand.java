package cora.usercommands;

import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.Var;
import java.util.ArrayList;

public class GeneralizeCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private ArrayList<Term> _terms;

    public GeneralizeCommand(ArrayList<Term> terms) {
        super();
        _terms = terms;
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        ArrayList<Boolean> tsInConstraint = new ArrayList<>();
        Term c = _proof.getConstraint();
        return false;
    }

    private boolean constraintInListTerms(Term c, ArrayList<Boolean> tsInConstraint) {
        for (Term t : _terms) {
            if (_terms.contains(c)) {
                tsInConstraint.add(true);
                break;
            }
            else if (c.isFunctionalTerm()) {
                for (int i = 0; i < c.numberImmediateSubterms(); i++) {
                    if (constraintInListTerms(c.queryImmediateSubterm(i), tsInConstraint)) {
                        tsInConstraint.add(true);
                        break;
                    }

                }
            }
        }
        return false;
    }

    @Override
    public void apply() {

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
