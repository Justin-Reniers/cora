package cora.smt;

import cora.exceptions.*;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
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
import java.util.Collections;
import java.util.TreeSet;

/**
 * An Equivalence Proof is a to-be-proven (or disproven) equivalence between two terms
 * that can be rewritten under the same LCTRS. There is also a constraint over the proof,
 * which has to hold for the proof to be valid.
 */
public class EquivalenceProof implements Proof {
    private TRS _lcTrs;
    private IProofState _ps;
    private boolean _completeness;
    private ArrayList<ProofHistory> _history;
    private TreeSet<Variable> _env;
    private int _varcounter, _proven;
    private boolean _bottom;

    /**
     * This constructor is used to create an equivalence proof. Keeps a list _history
     * of ProofHistory that can be used to access previous proof states.
     */
    /**public EquivalenceProof(TRS lcTrs, Term left, Term right, Term constraint) {
        _lcTrs = lcTrs;
        _completeness = true;
        _equations = new ArrayList<ProofEquation>();
        _completenessEquations = new ArrayList<ProofEquation>();
        _history = new ArrayList<ProofHistory>();
        if (!(left == null || right == null || constraint == null)) {
            Equation equation = new Equation(left, right, constraint);
            _cur_eq = equation;
            _equations.add(equation);
            _ps = new ProofState(_equations, _completenessEquations, new ArrayList<>(), _completeness);
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
    }**/

    public EquivalenceProof() {
        _lcTrs = null;
        _completeness = false;
        ArrayList<ProofEquation> equations = new ArrayList<ProofEquation>();
        ArrayList<ProofEquation> completenessEquations = new ArrayList<ProofEquation>();
        _ps = new ProofState(equations, completenessEquations, new ArrayList<FirstOrderRule>(), _completeness);
        _history = new ArrayList<ProofHistory>();
        _varcounter = 0;
        _bottom = true;
        _proven = 0;
        _env = new TreeSet<Variable>();
    }

    public EquivalenceProof(TRS lctrs, Term s, Term t, Term c) {
        _lcTrs = lctrs;
        _completeness = true;
        ArrayList<ProofEquation> equations = new ArrayList<ProofEquation>();
        ArrayList<ProofEquation> completenessEquations = new ArrayList<ProofEquation>();
        _history = new ArrayList<ProofHistory>();
        if (!(s == null || t == null || c == null)) {
            Equation equation = new Equation(s, t, c);
            equations.add(equation);
            _ps = new ProofState(equations, completenessEquations, new ArrayList<>(), _completeness);
        } else {
            _ps = new ProofState(new ArrayList<>(), _completeness);
        }
        _varcounter = 0;
        _bottom = false;
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
            _env.addAll(_ps.getCurrentEqVariables());
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public TreeSet<Variable> getEquationVariables() {
        return _env;
    }

    @Override
    public TreeSet<Variable> getCurrentEqVariables() {
        return _ps.getCurrentEqVariables();
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
    public void applyNewUserCommand(String uCommand) throws InvalidRuleApplicationException {
        if (_bottom) throw new BottomException(uCommand);
        try {
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand, this);
            uc.setProof(this);
            try {
                if (!(uc instanceof UndoCommand)) _history.add(new ProofHistory(_ps, uc, _bottom, _lcTrs));
                _ps = uc.apply(_ps);
                updateVariables();
                if (_completeness && _ps.getE().isEmpty() && _bottom) _proven = 2;
                else if (_ps.getE().isEmpty()) _proven = 1;
                else _proven = 0;
            } catch (InvalidRuleApplicationException e) {
                throw e;
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
    public void addEquation(ProofEquation eq) { _ps.addEquations(new ArrayList<>(Collections.singleton(eq))); }

    @Override
    public boolean getCompleteness() {
        return false;
    }

    @Override
    public void setCompleteness(boolean c) {

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

    @Override
    public TRS getLcTrs() {
        return _lcTrs;
    }

    @Override
    public Term getLeft() {
        return _ps.getS();
    }

    @Override
    public void setLeft(Term s) {
        _ps.setS(s);
    }

    @Override
    public Term getRight() {
        return _ps.getT();
    }

    @Override
    public void setRight(Term t) {
        _ps.setT(t);
    }

    @Override
    public Term getConstraint() {
        return _ps.getC();
    }

    @Override
    public void setConstraint(Term c) {
        _ps.setC(c);
    }

    @Override
    public ProofEquation getCurrentEquation() {
        return _ps.getCurrentEquation();
    }

    @Override
    public ArrayList<ProofEquation> getEquations() {
        return _ps.getE();
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

    @Override
    public int proofIsFinished() {
        return _proven;
    }
}
