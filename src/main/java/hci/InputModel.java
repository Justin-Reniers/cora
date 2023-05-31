package hci;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.InvalidUserInputException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.parsers.LcTrsParser;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import hci.interfaces.UserInputModel;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class InputModel implements UserInputModel {
    private final ArrayList<String> _userCommands;
    private EquivalenceProof _eqp;
    private String _lastCommand;
    private int _commandIndex;
    private static LcTrsInputReader _lctrsIn;
    private static LcTrsParser _lctrsParse;
    private TRS _lcTrs;

    public InputModel() {
        _userCommands = new ArrayList<String>();
        _commandIndex = 0;
        initDefault();
    }

    private void initDefault() {
        _eqp = new EquivalenceProof(null, null, null, null);
        _lctrsIn = new LcTrsInputReader();

    }

    @Override
    public String getUserInput() {
        return _lastCommand;
    }

    @Override
    public String getPreviousInput() {
        System.out.println(_commandIndex);
        if (_commandIndex > 0 && !_userCommands.isEmpty()) {
            _commandIndex--;
            return _userCommands.get(_commandIndex);
        }
        return "";
    }

    @Override
    public String getNextInput() {
        System.out.println(_commandIndex + "\t" + (_userCommands.size()));
        if (_commandIndex >= 0 && _commandIndex < _userCommands.size()) {
            _commandIndex++;
            return _userCommands.get(_commandIndex-1);
        }
        return "";
    }

    @Override
    public boolean addUserInput(String input) {
        if (applyUserInput(input)) {
            _userCommands.add(input);
            _lastCommand = input;
            _commandIndex++;
            return true;
        }
        return false;
    }

    @Override
    public void openFile(File file) {
        StringBuilder content = new StringBuilder();
        if (file.canRead() && file.getName().contains(".lctrs")) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = in.readLine();
                while (line != null) {
                    content.append(line).append("\n");
                    line = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                _lcTrs = LcTrsInputReader.readLcTrsFromString(content.toString());
                _eqp.setLcTrs(_lcTrs);
            } catch (ParserException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Invalid file");
        }
    }

    @Override
    public void enterProof(String proof) throws ParserException {
        TreeSet<Variable> vars = new TreeSet<Variable>();
        String[] strs = proof.split("\\s+");
        if (strs.length != 3) throw new InvalidUserInputException(proof);
        Term l = LcTrsInputReader.readTermFromStringWithEnv(strs[0], _lcTrs, vars);
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(strs[1], _lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(strs[2], _lcTrs, vars);
        vars.addAll(c.vars().getVars());
        _eqp.setLeft(l);
        _eqp.setRight(r);
        _eqp.setConstraint(c);
        _eqp.recordHistory();
    }

    @Override
    public String getRules() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < _eqp.getLcTrs().queryRuleCount(); i++) {
            s.append(" ").append(i+1).append(": ").append(_eqp.getLcTrs().queryRule(i)).append("\n");
        }
        return s.toString();
    }

    @Override
    public String getEquations() {
        StringBuilder s = new StringBuilder();
        for (Equation eq : _eqp.getEquations()) {
            s.append(" ").append(eq.toString()).append("\n");
        }
        return s.toString();
    }

    private boolean applyUserInput(String input) {
        try {
            _eqp.applyNewUserCommand(input);
            return true;
        } catch (InvalidRuleApplicationException e) {
            return false;
        }
    }
}
