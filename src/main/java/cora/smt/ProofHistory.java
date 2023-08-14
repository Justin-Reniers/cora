package cora.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.History;
import cora.interfaces.smt.UserCommand;
import cora.rewriting.TermRewritingSystem;

import java.util.ArrayList;

/**
 * A Proof History is an object that contains a single state of an equivalence proof.
 * It serves as a record type for
 */
public class ProofHistory implements History {
    private final ArrayList<Equation> _eqs, _cEqs;
    private final boolean _completeness, _bottom;

    private final UserCommand _uCommand;

    private final TRS _lcTrs;

    /**
     * This constructor is used to create a Proof History record.
     */
    public ProofHistory(ArrayList<Equation> eqs, UserCommand uCommand, boolean completeness,
                        ArrayList<Equation> cEqs, boolean bottom, TRS lcTrs) {
        _eqs = new ArrayList<>();
        for (Equation eq : eqs) _eqs.add(new Equation(eq));
        _uCommand = uCommand;
        _completeness = completeness;
        _bottom = bottom;
        _cEqs = new ArrayList<>();
        for (Equation ceq : cEqs) _cEqs.add(new Equation(ceq));
        if (lcTrs != null) _lcTrs = new TermRewritingSystem(lcTrs);
        else _lcTrs = null;
    }

    /**
     * This function gives a String representation of a proof history record.
     */
    public String toString() {
        return _eqs.toString() + //"\tCompleteness: " + _completeness +
                (_uCommand != null ? "\n" + _uCommand.toString() : "");
    }

    @Override
    public ArrayList<Equation> getEquations() {
        return _eqs;
    }

    @Override
    public ArrayList<Equation> getCompletenessEquations() {
        return _cEqs;
    }

    @Override
    public boolean getCompleteness() {
        return _completeness;
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
