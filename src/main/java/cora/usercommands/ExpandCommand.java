package cora.usercommands;

import cora.interfaces.rewriting.Rule;
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

public class ExpandCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private Position _p;
    private ArrayList<Equation> _equations;
    private ArrayList<Rule> _applicableRules;
    private boolean _terminating;

    public ExpandCommand(Position p) {
        super();
        _p = p;
        _equations = new ArrayList<>();
        _applicableRules = new ArrayList<>();
        _terminating = true;
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public boolean applicable() {
        Term lp = _proof.getLeft().querySubterm(_p);
        if (!(isBasicTerm(lp, _proof))) return false;
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
            Rule rule = _proof.getLcTrs().queryRule(i);
            // && rewriteConstraint(_proof, _p, i) != null
            Substitution gamma = rewrittenConstraintValid(_proof, i, _p);
            if (gamma != null && rule.applicable(lp)) _applicableRules.add(rule);
            //if (rule.applicable(lp)) _applicableRules.add(rule);
        }
        return !_applicableRules.isEmpty();
    }

    @Override
    public void apply() {
        Term lp = _proof.getLeft().querySubterm(_p);
        for (Rule rule : _applicableRules) {
            Substitution y = lp.unify(rule.queryLeftSide());
            Term lpt = rule.apply(_proof.getLeft().querySubterm(_p));
            Term l = _proof.getLeft().replaceSubterm(_p, lpt).substitute(y);
            Term r = _proof.getRight().substitute(y);
            Term c = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"),
                    _proof.getConstraint(), rule.queryConstraint()).substitute(y);
            Equation eq = new Equation(l, r, c);
            _equations.add(eq);
        }
        if (_terminating) _proof.addRule(new FirstOrderRule(_proof.getLeft(), _proof.getRight(),
                _proof.getConstraint(), true));
        _proof.removeCurrentEquation();
        _proof.addEquations(_equations);
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
}
