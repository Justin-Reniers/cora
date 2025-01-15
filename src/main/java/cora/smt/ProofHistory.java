package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.History;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.rewriting.TermRewritingSystem;

import java.util.ArrayList;

/**
 * A Proof History is an object that contains a single state of an equivalence proof.
 * It serves as a record type for
 */
public class ProofHistory implements History {
    private final boolean _bottom;

    private final UserCommand _uCommand;

    private final TRS _lcTrs;
    private final IProofState _ps;

    /**
     * This constructor is used to create a Proof History record.
     */
    public ProofHistory(IProofState ps, UserCommand uc, boolean bottom, TRS lctrs) {
        _ps = ps;
        _uCommand = uc;
        _bottom = bottom;
        if (lctrs != null) {
            _lcTrs = new TermRewritingSystem(lctrs, (ArrayList<FunctionSymbol>) lctrs.queryTheorySymbols());
        }
        else _lcTrs = null;
    }

    /**
     * This function gives a String representation of a proof history record.
     */
    public String toString() {
        return _ps.getE().toString().replace("[", "").replace("]", "") + //"\tCompleteness: " + _completeness +
                (_uCommand != null ? "\n" + _uCommand : "");
    }

    @Override
    public IProofState getProofState() {
        return _ps;
    }

    @Override
    public ArrayList<ProofEquation> getEquations() {
        return _ps.getE();
    }

    @Override
    public ArrayList<ProofEquation> getCompletenessEquations() {
        return _ps.getCompletenessE();
    }

    @Override
    public boolean getCompleteness() {
        return _ps.getCompleteness();
    }

    /**
     * Returns the user command to be applied to the equivalence proof at this proof state.
     */
    public UserCommand getUserCommand() {
        return _uCommand;
    }

    @Override
    public boolean getBottom() {
        return _bottom;
    }

    @Override
    public TRS getLcTrs() {
        return _lcTrs;
    }
}
