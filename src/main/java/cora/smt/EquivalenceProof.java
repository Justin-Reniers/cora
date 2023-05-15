package cora.smt;

import cora.exceptions.BottomException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import cora.terms.Var;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An Equivalence Proof is a to-be-proven (or disproven) equivalence between two terms
 * that can be rewritten under the same LCTRS. There is also a constraint over the proof,
 * which has to hold for the proof to be valid.
 */
public class EquivalenceProof implements Proof {
    private TRS _lcTrs;
    private boolean _completeness;
    private ArrayList<Equation> _equations, _completenessEquations;
    private Equation _cur_eq;
    private ArrayList<ProofHistory> _history;
    private int _varcounter;
    private boolean _bottom;

    /**
     * This constructor is used to create an equivalence proof. Keeps a list _history
     * of ProofHistory that can be used to access previous proof states.
     */
    public EquivalenceProof(TRS lcTrs, Term left, Term right, Term constraint) {
        _lcTrs = lcTrs;
        //TODO add flag reading for confluence of lctrs yes or no.
        _completeness = false;
        Equation equation = new Equation(left, right, constraint);
        _equations = new ArrayList<Equation>();
        _equations.add(equation);
        _completenessEquations = new ArrayList<Equation>();
        _cur_eq = equation;
        _history = new ArrayList<ProofHistory>();
        //_history.add(new ProofHistory(_left, _right, _constraint, null));
        _varcounter = 0;
        _bottom = false;
    }

    /**
     * Parses the given String and applies the user command corresponding to the information
     * contained in the String. Checks whether the user command is applicable first, and if so,
     * adds the current state and to be applied user command to the history, then applies it.
     * Catches a Parser Exception and returns false if the user command is invalid.
     */
    @Override
    public boolean applyNewUserCommand(String uCommand) {
        if (_bottom) throw new BottomException(uCommand);
        try {
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand);
            uc.setProof(this);
            if (uc.applicable()) {
                Equation eq = _equations.get(_equations.size() - 1);
                _history.add(new ProofHistory(eq, uc));
                uc.apply();
                return true;
            } return false;
        } catch (ParserException e) {
            Logger.log(e.toString());
            return false;
        }
    }

    @Override
    public void removeCurrentEquation() {
        _equations.remove(_cur_eq);
        try {
            _cur_eq = _equations.get(0);
        } catch (IndexOutOfBoundsException e) {
            //Logger.log("No more equations in proof");
            _cur_eq = null;
        }
    }

    @Override
    public void addEquations(ArrayList<Equation> eqs) {
        _equations.addAll(eqs);
        if (_cur_eq == null && !_equations.isEmpty()) _cur_eq = _equations.get(0);
    }

    @Override
    public void addEquation(Equation eq) { _equations.add(eq); }

    @Override
    public boolean getCompleteness() {
        return _completeness;
    }

    @Override
    public void setCompleteness(boolean c) {
        _completeness = c;
    }

    @Override
    public ArrayList<Equation> getCompletenessEquationSet() {
        return _completenessEquations;
    }

    @Override
    public void setCompletenessEquationSet() {
        if (_completenessEquations.isEmpty()) _completenessEquations.addAll(_equations);
    }

    @Override
    public void emptyCompletenessEquationSet() {
        _completenessEquations.clear();
    }

    @Override
    public void addRule(Rule r) {
        _lcTrs.addRule(r);
    }

    @Override
    public void setBottom(boolean bottom) {
        if (!_bottom) _bottom = bottom;
    }

    @Override
    public boolean getBottom() {
        return _bottom;
    }

    /**
     * Given a path to a file, writes the proof history to the file at said path.
     * Creates a new file if file does not yet exist. Throws an IO exception if problems
     * with the file arise.
     */
    @Override
    public void saveStateToFile(String filePath) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (ProofHistory ph : _history) fw.write(ph.toString() + "\n");
        fw.write(currentState());
        fw.close();
    }

    /**
     * Returns the last applied command in the proof history.
     */
    @Override
    public UserCommand getLastCommand() {
        return _history.get(_history.size() - 1).getUserCommand();
    }

    /**
     * Returns the LCTRS with which the equivalence proof is done.
     */
    @Override
    public TRS getLcTrs() {
        return _lcTrs;
    }

    /**
     * Returns the left hand side of the equivalence proof.
     */
    @Override
    public Term getLeft() {
        return _cur_eq.getLeft();
    }

    /**
     * Sets the left hand side of the equivalence proof.
     */
    @Override
    public void setLeft(Term t) {
        _cur_eq.setLeft(t);
    }

    /**
     * Returns the right hand side of the equivalence proof.
     */
    @Override
    public Term getRight() {
        return _cur_eq.getRight();
    }

    /**
     * Sets the right hand side of the equivalence proof.
     */
    @Override
    public void setRight(Term t) { _cur_eq.setRight(t); }

    /**
     * Returns the constraint side of the equivalence proof.
     */
    @Override
    public Term getConstraint() {
        return _cur_eq.getConstraint();
    }

    /**
     * Sets the constraint side of the equivalence proof.
     */
    @Override
    public void setConstraint(Term t) {
        _cur_eq.setConstraint(t);
    }

    @Override
    public Equation getCurrentEquation() {
        return _cur_eq;
    }

    @Override
    public ArrayList<Equation> getEquations() {
        return _equations;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        Var fresh = new Var("x_" + _varcounter, expectedType);
        _varcounter++;
        return fresh;
    }

    /**
     * This function gives a string representation of the equivalence proof.
     */
    @Override
    public String toString() {
        //return _lcTrs.toString() + "\n" + _left.toString() + "\t" + _right.toString() +
        //        "\t[" + _constraint.toString() + "]";
        return "";
    }

    /**
     * This function gives a string representation of the current state of the
     * equivalence proof.
     */
    @Override
    public String currentState() {
        return _cur_eq.getLeft().toString() + "\t" + _cur_eq.getRight().toString() +
                "\t[" + _cur_eq.getConstraint().toString() + "]";
    }
}
