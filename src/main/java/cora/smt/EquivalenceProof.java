package cora.smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;

import javax.swing.text.html.parser.Parser;
import java.util.ArrayList;

public class EquivalenceProof implements Proof {
    private TRS _lcTrs;
    private Term _left, _right;
    private String _uCommand;
    private ArrayList<ProofHistory> _uCommands;

    public EquivalenceProof(TRS lcTrs, Term left, Term right) {
        _lcTrs = lcTrs;
        _left = left;
        _right = right;
    }

    public boolean applyNewUserCommand(String uCommand) {
        try {
            //TODO make inputreader return usercommand, fiddle around with TRS passing or no
            UserCommand uc = LcTrsInputReader.readUserInputFromString(uCommand);
            if (uc instanceof SwapCommand) {
                Term temp = _left;
                _left = _right;
                _right = temp;
            }
            else if (uc.applicable(_lcTrs, _left)) uc.apply(_lcTrs, _left);
            return true;
        } catch (ParserException e) {
            Logger.log(e.toString());
            return false;
        }
    }

    @Override
    public void writeToFile(String filePath) {

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
    public Term getRight() {
        return _right;
    }

    @Override
    public String toString() {
        return "";
    }
}
