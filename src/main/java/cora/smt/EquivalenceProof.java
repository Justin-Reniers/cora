package cora.smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
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
    private Term _left, _right, _constraint;
    private ArrayList<ProofHistory> _history;

    /**
     * This constructor is used to create an equivalence proof. Keeps a list _history
     * of ProofHistory that can be used to access previous proof states.
     */
    public EquivalenceProof(TRS lcTrs, Term left, Term right, Term constraint) {
        _lcTrs = lcTrs;
        _left = left;
        _right = right;
        _constraint = constraint;
        _history = new ArrayList<ProofHistory>();
        _history.add(new ProofHistory(_left, _right, _constraint, null));
    }

    /**
     * Parses the given String and applies the user command corresponding to the information
     * contained in the String. Checks whether the user command is applicable first, and if so,
     * adds the current state and to be applied user command to the history, then applies it.
     * Catches a Parser Exception and returns false if the user command is invalid.
     */
    @Override
    public boolean applyNewUserCommand(String uCommand) {
        try {
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand);
            uc.setProof(this);
            if (uc.applicable()) {
                _history.add(new ProofHistory(_left, _right, _constraint, uc));
                uc.apply();
                return true;
            } return false;
        } catch (ParserException e) {
            Logger.log(e.toString());
            return false;
        }
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
        return _left;
    }

    /**
     * Sets the left hand side of the equivalence proof.
     */
    @Override
    public void setLeft(Term t) {
        if (t != null) _left = t;
    }

    /**
     * Returns the right hand side of the equivalence proof.
     */
    @Override
    public Term getRight() {
        return _right;
    }

    /**
     * Sets the right hand side of the equivalence proof.
     */
    @Override
    public void setRight(Term t) {
        if (t != null) _right = t;
    }

    /**
     * Returns the constraint side of the equivalence proof.
     */
    @Override
    public Term getConstraint() {
        return _constraint;
    }

    /**
     * Sets the constraint side of the equivalence proof.
     */
    @Override
    public void setConstraint(Term t) {
        if (t != null) _constraint = t;
    }

    /**
     * This function gives a string representation of the equivalence proof.
     */
    @Override
    public String toString() {
        return _lcTrs.toString() + "\n" + _left.toString() + "\t" + _right.toString() +
                "\t[" + _constraint.toString() + "]";
    }

    /**
     * This function gives a string representation of the current state of the
     * equivalence proof.
     */
    @Override
    public String currentState() {
        return _left.toString() + "\t" + _right.toString() + "\t[" + _constraint.toString() + "]";
    }
}
