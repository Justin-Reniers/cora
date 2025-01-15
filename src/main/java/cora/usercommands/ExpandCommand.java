package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidExpandAppicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Substitution;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.HashSet;

public class ExpandCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Position _p;
    private ArrayList<ProofEquation> _equations;
    private ArrayList<Rule> _applicableRules;
    private Boolean _terminating;
    private ArrayList<Substitution> _gammas;

    public ExpandCommand(Position p) {
        super();
        _p = p;
        _equations = new ArrayList<>();
        _applicableRules = new ArrayList<>();
        _gammas = new ArrayList<>();
    }

    public ExpandCommand(Position p, Boolean terminating) {
        super();
        _p = p;
        _equations = new ArrayList<>();
        _applicableRules = new ArrayList<>();
        _gammas = new ArrayList<>();
        _terminating = terminating;
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        Term lp = ps.getS().querySubterm(_p);
        if (!(isBasicTerm(lp, _proof))) throw new InvalidExpandAppicationException(lp + " is not a basic term");
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
            Rule rule = _proof.getLcTrs().queryRule(i);
            Substitution gamma = rule.queryLeftSide().unify(lp);
            if (gamma != null && rule.applicable(lp.substitute(gamma))) {
                _applicableRules.add(rule);
                _gammas.add(gamma);
            }
        }
        if (_applicableRules.isEmpty()) throw new InvalidExpandAppicationException("No applicable rules found");

        for (Rule rule : _applicableRules) {
            Substitution y = rule.queryLeftSide().unify(lp);
            Term lpt = rule.apply(ps.getS().querySubterm(_p).substitute(y));
            Term s = ps.getS().replaceSubterm(_p, lpt).substitute(y);
            Term t = ps.getT().substitute(y);
            Term c = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"),
                    _proof.getConstraint(), rule.queryConstraint()).substitute(y);
            Equation eq = new Equation(s, t, c);
            _equations.add(eq);
        }
        if (_terminating == null || _terminating) {
            Rule rule = new FirstOrderRule(ps.getS(), ps.getT(), ps.getC(), true);
            if (_terminating == null) _terminating = isTerminating(rule);
            if (_terminating || isTerminating(rule))
                _proof.addRule(new FirstOrderRule(ps.getS(), ps.getT(), ps.getC(), true));
        }
        ps.removeCurrentEquation();
        ps.addEquations(_equations);
        return ps;
    }

    @Override
    public void setProof(EquivalenceProof proof) {
        _proof = proof;
    }

    @Override
    public Var getFreshVar(Type expectedType) {
        return _proof.getFreshVar(expectedType);
    }

    @Override
    public EquivalenceProof getProof() {
        return _proof;
    }

    @Override
    public String toString() {
        return "expand " + _p + (_terminating ? " terminating" : " nonterminating");
    }

    private boolean isTerminating(Rule r) {
        HashSet<Rule> rules = new HashSet<>();
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) rules.add(_proof.getLcTrs().queryRule(i));
        rules.add(r);
        throw new UnsupportedOperationException("Checking termination of ruleset not yet implemented!");
        //TODO termination check on set of rules
    }
}
