package cora.smt;

import cora.exceptions.*;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.parsers.LcTrsInputReader;
import cora.rewriting.FirstOrderRule;
import cora.terms.Var;
import cora.usercommands.UndoCommand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

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
    private TreeSet<Variable> _env;
    private int _varcounter, _proven;
    private boolean _bottom;

    /**
     * This constructor is used to create an equivalence proof. Keeps a list _history
     * of ProofHistory that can be used to access previous proof states.
     */
    public EquivalenceProof(TRS lcTrs, Term left, Term right, Term constraint) {
        _lcTrs = lcTrs;
        _completeness = true;
        _equations = new ArrayList<Equation>();
        _completenessEquations = new ArrayList<Equation>();
        _history = new ArrayList<ProofHistory>();
        if (!(left == null || right == null || constraint == null)) {
            Equation equation = new Equation(left, right, constraint);
            _cur_eq = equation;
            _equations.add(equation);
        }
        _varcounter = 0;
        _bottom = false;
        _proven = 0;
        _env = new TreeSet<Variable>();
        try {
            _env.addAll(_cur_eq.getLeft().vars().getVars());
            _env.addAll(_cur_eq.getRight().vars().getVars());
            _env.addAll(_cur_eq.getConstraint().vars().getVars());
        } catch (NullPointerException ignored) {}
    }

    public EquivalenceProof() {
        _lcTrs = null;
        _completeness = false;
        _equations = new ArrayList<Equation>();
        _completenessEquations = new ArrayList<Equation>();
        _cur_eq = null;
        _history = new ArrayList<ProofHistory>();
        _varcounter = 0;
        _bottom = true;
        _proven = 0;
        _env = new TreeSet<Variable>();
    }

    private void updateVariables() {
        _env = new TreeSet<>();
        try {
            for (int i = 0; i < _lcTrs.queryRuleCount(); i++) {
                FirstOrderRule r = (FirstOrderRule) _lcTrs.queryRule(i);
                _env.addAll(r.queryLeftSide().vars().getVars());
                _env.addAll(r.queryRightSide().vars().getVars());
                _env.addAll(r.queryConstraint().vars().getVars());
            }
            _env.addAll(_cur_eq.getLeft().vars().getVars());
            _env.addAll(_cur_eq.getRight().vars().getVars());
            _env.addAll(_cur_eq.getConstraint().vars().getVars());
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public TreeSet<Variable> getEquationVariables() {
        return _env;
    }

    @Override
    public TreeSet<Variable> getCurrentEqVariables() {
        TreeSet<Variable> vars = new TreeSet<>();
        try {
            vars.addAll(_cur_eq.getLeft().vars().getVars());
            vars.addAll(_cur_eq.getRight().vars().getVars());
            vars.addAll(_cur_eq.getConstraint().vars().getVars());
        } catch (NullPointerException ignored) {

        }
        return vars;
    }

    @Override
    public TreeSet<Variable> getRuleVariables(int ruleIndex) {
        TreeSet<Variable> vars = new TreeSet<>();
        try {
            FirstOrderRule r = (FirstOrderRule) _lcTrs.queryRule(ruleIndex);
            vars.addAll(r.queryLeftSide().vars().getVars());
            vars.addAll(r.queryRightSide().vars().getVars());
            vars.addAll(r.queryConstraint().vars().getVars());
        } catch (NullPointerException ignored) {

        }
        return vars;
    }

    /**
     * Parses the given String and applies the user command corresponding to the information
     * contained in the String. Checks whether the user command is applicable first, and if so,
     * adds the current state and to be applied user command to the history, then applies it.
     * Catches a Parser Exception and returns false if the user command is invalid.
     */
    @Override
    public void applyNewUserCommand(String uCommand) {
        if (_bottom) throw new BottomException(uCommand);
        try {
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand, this);
            uc.setProof(this);
            if (uc.applicable()) {
                if (!(uc instanceof UndoCommand)) _history.add(new ProofHistory(_equations, uc, _completeness,
                        _completenessEquations, _bottom, _lcTrs));
                uc.apply();
                updateVariables();
                if (_completeness && _equations.isEmpty() && _bottom) _proven = 2;
                else if (_equations.isEmpty()) _proven = 1;
                else _proven = 0;
            } else {
                throw new InvalidRuleApplicationException(uCommand);
            };
        } catch (ParserException e) {
            throw new InvalidRuleParseException(uCommand);
        } catch (InvalidRuleApplicationException | BottomException e) {
            throw e;
        } catch (UnsatException e) {
            throw new UnsatException(e.getMessage());
        }
    }

    @Override
    public void removeCurrentEquation() {
        _equations.remove(_cur_eq);
        try {
            _cur_eq = _equations.get(0);
        } catch (IndexOutOfBoundsException e) {
            _cur_eq = null;
        }
    }

    @Override
    public void clearEquations() {
        _equations = new ArrayList<Equation>();
        _cur_eq = null;
    }

    @Override
    public void addEquations(ArrayList<Equation> eqs) {
        _equations.addAll(eqs);
        if (_cur_eq == null && !_equations.isEmpty()) _cur_eq = _equations.get(0);
        if (_cur_eq == null) _cur_eq = _equations.get(0);
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
    public void addCompletenessEquations(ArrayList<Equation> cEqs) {
        _completenessEquations.addAll(cEqs);
    }

    @Override
    public void addCompletenessEquation(Equation eq) {
        _completenessEquations.add(eq);
    }

    @Override
    public void emptyCompletenessEquationSet() {
        _completenessEquations = new ArrayList<Equation>();
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
    public void setLcTrs(TRS lcTrs) {
        _lcTrs = lcTrs;
    }

    @Override
    public ProofHistory getPreviousState() {
        return _history.get(_history.size()-1);
    }

    @Override
    public void deleteLastState() {
        _history.remove(_history.size()-1);
    }

    @Override
    public void recordHistory() {
        _history.add(new ProofHistory(_equations, null, _completeness, _completenessEquations, _bottom,
                _lcTrs));
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
        FileWriter fw = new FileWriter(filePath, false);
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
    public void setCurrentEquation() {
        _cur_eq = _equations.get(0);
        updateVariables();
    }

    @Override
    public void setCurrentEquation(Equation eq) {
        _cur_eq = eq;
        _equations.set(0, eq);
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

    @Override
    public int proofIsFinished() {
        return _proven;
    }
}
