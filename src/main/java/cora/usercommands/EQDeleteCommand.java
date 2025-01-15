package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidEQDeleteApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.ProofEquation;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;

import java.util.ArrayList;
import java.util.TreeSet;

import static cora.types.Sort.boolSort;
import static cora.types.Sort.intSort;

public class EQDeleteCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;
    private ArrayList<Term> _ineqs;

    public EQDeleteCommand() {
        super();
        _ineqs = new ArrayList<Term>();
    }

    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        Term s = ps.getS();
        Term t = ps.getT();
        Term c = ps.getC();
        try {
            findConstraints(s, t, c);
        } catch (InvalidRuleApplicationException e) {
            throw e;
        }
        Term newC = constructConstraintAddition(_ineqs);
        ps.setC(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, newC));
        return ps;
    }

    private void findConstraints(Term s, Term t, Term c) throws InvalidRuleApplicationException {
        if (s.isVariable() && t.isVariable() && s.equals(t)) ;
        else if (s.isTheoryTerm(c) && t.isTheoryTerm(c)) {
            Term comp;
            if (s.queryType().equals(intSort)) comp = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("==i"), s, t);
            else comp = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("<-->"), s, t);
            if (!_ineqs.contains(comp)) _ineqs.add(comp);
        } else if ((s.isVariable() && !s.isTheoryTerm(c)) || (t.isVariable() && !t.isTheoryTerm(c))) {
            throw new InvalidEQDeleteApplicationException(s +  " or " + t + " is not a theory term");
        }  else if (s.isFunctionalTerm() && t.isFunctionalTerm() && !s.queryRoot().equals(t.queryRoot())) {
            throw new InvalidEQDeleteApplicationException(s +  " and " + t + " do not share root symbol");
        }else if (s.isFunctionalTerm() && t.isFunctionalTerm() && (!s.isTheoryTerm(c) || !t.isTheoryTerm(c))) {
            for (int i = 1; i <= s.numberImmediateSubterms(); i++) {
                findConstraints(s.queryImmediateSubterm(i), t.queryImmediateSubterm(i), c);
            }
        } else {
            throw new InvalidEQDeleteApplicationException("Could not apply, other reason");
        }
    }

    //@Override
    public void apply2() {
        Term ct = constructConstraintAddition(_ineqs);
        _proof.setConstraint(new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"),
                _proof.getConstraint(), ct));
    }

    private Term constructConstraintAddition(ArrayList<Term> ineqs) {
        if (ineqs.isEmpty()) return null;
        Term c = ineqs.get(0);
        for (int i = 1; i < ineqs.size(); i++) {
            Term t = ineqs.get(i);
            c = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, t);
        }
        return new FunctionalTerm(_proof.getLcTrs().lookupSymbol("~"), c);
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
        return "eq-delete";
    }
}
