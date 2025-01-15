package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidDisproveApplicationException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.smt.IProofState;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.types.Sort;
import cora.z3.SatisfiabilityEnum;
import cora.z3.Z3TermHandler;

import java.util.ArrayList;

import static cora.types.Sort.intSort;

public class DisproveCommand extends UserCommandInherit implements UserCommand {
    private EquivalenceProof _proof;

    public DisproveCommand() {
        super();
    }
    @Override
    public Position queryPosition() {
        return null;
    }

    @Override
    public IProofState apply(IProofState ps) throws InvalidRuleApplicationException {
        if (!ps.getCompleteness()) throw new InvalidDisproveApplicationException("No completeness property");
        Term l = _proof.getLeft();
        Term r = _proof.getRight();
        Term c = _proof.getConstraint();
        ArrayList<FunctionSymbol> tSymbs = (ArrayList<FunctionSymbol>) _proof.getLcTrs().queryTheorySymbols();
        Z3TermHandler z3 = new Z3TermHandler(_proof.getLcTrs());
        //s, t E Terms(Sigma_theory, V), i is a theory sort, and phi /\ s != t is satisfiable
        if ((l.queryType().equals(intSort) || l.queryType().equals(Sort.boolSort)) &&
            ((isNumeric(l.queryRoot().queryName()) && isNumeric(r.queryRoot().queryName()))) ||
            (isNumeric(l.queryRoot().queryName()) && tSymbs.contains(r.queryRoot())) ||
            (tSymbs.contains(l.queryRoot()) && isNumeric(r.queryRoot().queryName())) ||
            ((tSymbs.contains(l.queryRoot()) && tSymbs.contains(r.queryRoot())))
        ) {
            Term ineq;
                if (l.queryType().equals(r.queryType()) && l.queryType().equals(intSort)) {
                    ineq = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("!=i"), l, r);
                } else {
                    ineq = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("!=b"), l, r);
                }
            Term nc = new FunctionalTerm(_proof.getLcTrs().lookupSymbol("/\\"), c, ineq);
            if (z3.satisfiable(nc) == SatisfiabilityEnum.SAT) {
                _proof.setBottom(true);
                return ps;
            }
        }
        //s = f(s->) and t = g(t->) with f, g distinct constructors and phi satisfiable
        if (l.isFunctionalTerm() && r.isFunctionalTerm() && !l.queryRoot().equals(r.queryRoot()) &&
            isConstructorTerm(l, _proof) && isConstructorTerm(r, _proof) &&
            z3.satisfiable(c) == SatisfiabilityEnum.SAT) {
            _proof.setBottom(true);
            return ps;
        }

        //s E V\Var(phi), phi satisfiable, at least two different constructors have output sort i,
        //and either t is a variable distinct from s or t has the form g(t->) with g E Cons
        int counter = 0;
        for (FunctionSymbol fs : _proof.getLcTrs().querySymbols()) {
            if (fs.queryType().equals(l.queryType())) counter++;
        }
        if (l.isVariable() && !c.vars().getVars().contains((Variable) l) && counter >= 2 &&
                (r.isVariable() && !l.equals(r)) || (r.isFunctionalTerm() && isConstructorTerm(r, _proof))
                && z3.satisfiable(c) == SatisfiabilityEnum.SAT) {
            _proof.setBottom(true);
            return ps;
        }
        throw new InvalidDisproveApplicationException("No disprove cases apply");
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
        return "disprove";
    }
}
