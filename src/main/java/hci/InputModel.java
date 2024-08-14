package hci;

import cora.exceptions.*;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.Proof;
import cora.interfaces.terms.Position;
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
    private InputPresenter inputPresenter;
    private final ArrayList<String> _userCommands;
    private Proof _eqp;
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
    public InputPresenter getPresenter() {
        return inputPresenter;
    }

    @Override
    public void setPresenter(InputPresenter inputPresenter) {
        this.inputPresenter = inputPresenter;
    }

    @Override
    public String getUserInput() {
        return _lastCommand;
    }

    @Override
    public String getPreviousInput() {
        if (_commandIndex > 0 && !_userCommands.isEmpty()) {
            _commandIndex--;
            return _userCommands.get(_commandIndex);
        }
        return "";
    }

    @Override
    public String getNextInput() {
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
                getPresenter().displayWarning(e.getMessage());
            }
            try {
                _lcTrs = LcTrsInputReader.readLcTrsFromString(content.toString());
                _eqp.setLcTrs(_lcTrs);
            } catch (ParserException e) {
                getPresenter().displayWarning(e.getMessage());
            }
        } else {
            String[] s = file.getName().split("\\.");
            String ex = s[s.length - 1];
            throw new InvalidFileExtensionError(ex);
        }
    }

    @Override
    public void enterProof(String proof) throws ParserException {
        TreeSet<Variable> vars = new TreeSet<Variable>();
        String[] strs = proof.split("\\s+");
        if (strs.length != 3) throw new InvalidUserInputException(proof);
        Term l = LcTrsInputReader.readTermFromString(strs[0], _lcTrs);
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(strs[1], _lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(strs[2], _lcTrs, vars);
        vars.addAll(c.vars().getVars());
        Equation eq = new Equation(l, r, c);
        _eqp.addEquation(eq);
        _eqp.setCurrentEquation();
    }

    @Override
    public String getRules() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        ArrayList<Character> counter = new ArrayList<>();
        counter.add('A');
        for (int i = 0; i < _eqp.getLcTrs().queryRuleCount(); i++) {
            if (_eqp.getLcTrs().queryRule(i).inCompletenessSet()) {
                s.append(" ");
                for (Character c : counter) s.append(c);
                s.append("\t: ").append(_eqp.getLcTrs().queryRule(i).toHTMLString()).append("<br>");
                updateCounter(counter);
            } else {
                s.append(" ").append(i+1).append("\t: ")
                        .append(_eqp.getLcTrs().queryRule(i).toHTMLString()).append("<br>");
            }
        }
        s.append("</html>");
        return s.toString();
    }

    @Override
    public String getRulesLeft() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        ArrayList<Character> counter = new ArrayList<>();
        counter.add('A');
        for (int i = 0; i < _eqp.getLcTrs().queryRuleCount(); i++) {
            if (_eqp.getLcTrs().queryRule(i).inCompletenessSet()) {
                s.append(" ");
                for (Character c : counter) s.append(c);
                s.append("\t: ").append(_eqp.getLcTrs().queryRule(i).queryLeftSide().toHTMLString()).append("<br>");
                updateCounter(counter);
            } else {
                s.append(" ").append(i+1).append("\t: ")
                        .append(_eqp.getLcTrs().queryRule(i).queryLeftSide().toHTMLString()).append("<br>");
            }
        }
        s.append("</html>");
        return s.toString();
    }

    @Override
    public String getRulesRight() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        for (int i = 0; i < _eqp.getLcTrs().queryRuleCount(); i++) {
            s.append(" ").append(_eqp.getLcTrs().queryRule(i).queryRightSide().toHTMLString()).append("<br>");
        }
        s.append("</html>");
        return s.toString();
    }

    @Override
    public String getRulesConstraint() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        for (int i = 0; i < _eqp.getLcTrs().queryRuleCount(); i++) {
            s.append(" ").append(_eqp.getLcTrs().queryRule(i).queryConstraint().toHTMLString()).append("<br>");
        }
        s.append("</html>");
        return s.toString();
    }

    private void updateCounter(ArrayList<Character> counter) {
        if (counter.get(counter.size() - 1) == 'Z') {
            boolean all = true;
            for (Character c : counter)
                if (c != 'Z') {
                    all = false;
                    break;
                }
            if (all) {
                for (Character c : counter) counter.set(counter.indexOf(c), 'A');
                counter.add('A');
            } else {
                for (int i = counter.size() - 1; i >= 0; i--) {
                    if (counter.get(i) != 'Z') {
                        counter.set(i, (char) (counter.get(i) + 1));
                        break;
                    }
                }
            }
        } else {
            for (int i = counter.size() - 1; i >= 0; i--) {
                if (counter.get(i) != 'Z') {
                    counter.set(i, (char) (counter.get(i) + 1));
                    break;
                }
            }
        }
    }

    @Override
    public String getEquations() {
        StringBuilder s = new StringBuilder();
        for (Equation eq : _eqp.getEquations()) {
            s.append(" ").append(_eqp.getEquations().indexOf(eq)+1).append("\t: ")
                    .append(eq.toHTMLString()).append("<br>");
        }
        return s.toString();
    }

    @Override
    public String getEquationsLeft() {
        StringBuilder s = new StringBuilder();
        for (Equation eq : _eqp.getEquations()) {
            s.append(" ").append(_eqp.getEquations().indexOf(eq)+1).append("\t: ")
                    .append(eq.getLeft().toHTMLString()).append("<br>");
        }
        return s.toString();
    }

    @Override
    public String getEquationsRight() {
        StringBuilder s = new StringBuilder();
        for (Equation eq : _eqp.getEquations()) {
            s.append(" ").append(eq.getRight().toHTMLString()).append("<br>");
        }
        return s.toString();
    }

    @Override
    public String getEquationsConstraint() {
        StringBuilder s = new StringBuilder();
        for (Equation eq : _eqp.getEquations()) {
            s.append(" ").append(eq.getConstraint().toHTMLString()).append("<br>");
        }
        return s.toString();
    }

    @Override
    public String getPositions() {
        StringBuilder s = new StringBuilder();
        if (_eqp.getCurrentEquation() == null) return "";
        Term l = _eqp.getCurrentEquation().getLeft();
        for (Position p : l.queryAllPositions()) {
            s.append(" ").append(p.toHTMLString()).append("\t").append(l.querySubterm(p).toHTMLString()).append("<br>");
        }
        return s.toString();
    }

    @Override
    public boolean getCompleteness() {
        return _eqp.getCompleteness();
    }

    @Override
    public boolean getBottom() {
        return _eqp.getBottom();
    }

    @Override
    public void saveProofToFile(File file) {
        try {
            _eqp.saveStateToFile(file.getPath());
        } catch (IOException e) {
            getPresenter().displayWarning(e.getMessage());
        }
    }

    @Override
    public void loadProofFromFile(File file) throws ParserException {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            while (line != null) {
                content.append(line).append("\n");
                line = in.readLine();
            }
        } catch (IOException e) {
            getPresenter().displayWarning(e.getMessage());
        }
        if (!content.toString().equals("")) {
            String[] lines = content.toString().split("\n");
            enterProof(lines[0]);
            for (int i = 0; i < lines.length; i++) {
                if (i % 2 == 1) {
                    applyUserInput(lines[i]);
                }
            }
        }
    }

    private boolean applyUserInput(String input) {
        try {
            _eqp.applyNewUserCommand(input);
            return true;
        } catch (InvalidRuleApplicationException | InvalidPositionException | UnsatException e) {
            getPresenter().displayWarning(e.getMessage());
            return false;
        }
    }
}
