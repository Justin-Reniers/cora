package cora.smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;

import javax.swing.text.html.parser.Parser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

public class EquivalenceProof implements Proof {
    private TRS _lcTrs;
    private Term _left, _right, _constraint;
    private ArrayList<ProofHistory> _history;

    public EquivalenceProof(TRS lcTrs, Term left, Term right, Term constraint) {
        _lcTrs = lcTrs;
        _left = left;
        _right = right;
        _constraint = constraint;
        _history = new ArrayList<ProofHistory>();
    }

    @Override
    public boolean applyNewUserCommand(String uCommand) {
        try {
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand);
            uc.setProof(this);
            if (uc.applicable()) uc.apply();
            _history.add(new ProofHistory(_left, _right, _constraint, uc));
            return true;
        } catch (ParserException e) {
            Logger.log(e.toString());
            return false;
        }
    }

    @Override
    public void saveStateToFile(String filePath) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (ProofHistory ph : _history) fw.write(ph.toString() + "\n");
        fw.close();
    }

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
        return _left;
    }

    @Override
    public void setLeft(Term t) {
        if (t != null) _left = t;
    }

    @Override
    public Term getRight() {
        return _right;
    }

    @Override
    public void setRight(Term t) {
        if (t != null) _right = t;
    }

    @Override
    public Term getConstraint() {
        return _constraint;
    }

    @Override
    public void setConstraint(Term t) {
        if (t != null) _constraint = t;
    }

    @Override
    public String toString() {
        return _lcTrs.toString() + "\n" + _left.toString() + "\t" + _right.toString() +
                "\t[" + _constraint.toString() + "]";
    }

    @Override
    public String currentState() {
        return _left.toString() + "\t" + _right.toString() + "\t[" + _constraint.toString() + "]";
    }
}
