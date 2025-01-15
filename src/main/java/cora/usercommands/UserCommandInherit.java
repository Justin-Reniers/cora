package cora.usercommands;

import cora.exceptions.invalidruleapplications.InvalidConstraintRewritingException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.IProofState;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.smt.EquivalenceProof;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.terms.Subst;
import cora.terms.Var;
import cora.z3.Z3TermHandler;
import java.util.ArrayList;
import java.util.TreeSet;

import static cora.types.Sort.boolSort;
import static cora.types.Sort.intSort;

/**
 * A "User Command" is a rewriting rule that is to be applied to an equivalence proof.
 * This inherit provides default functionality for such rewriting rules.
 */
abstract class UserCommandInherit {
    protected Substitution rewrittenConstraintValid(IProofState ps, TRS lctrs, int ruleIndex, Position pos,
                                                    Substitution gamma) throws InvalidConstraintRewritingException {
        if (gamma == null) gamma = new Subst();
        FirstOrderRule r = (FirstOrderRule) lctrs.queryRule(ruleIndex);
        Term s = ps.getS();
        Term c = ps.getC();
        Term ruleC = r.queryConstraint();
        Term ruleL = r.queryLeftSide().substitute(gamma);
        Term sAtPos = s.querySubterm(pos);
        Substitution y = ruleL.match(sAtPos);
        if (y == null) throw new InvalidConstraintRewritingException("Cannot rewrite constraint, y is null");
        gamma.compose(y);
        Z3TermHandler z3 = new Z3TermHandler(lctrs);
        if (!checkLVARcondition(r, gamma, c)) return null;
        ruleC = ruleC.substitute(gamma);
        if (pos != null && ruleIndex >= 0) {
            Term valid = new FunctionalTerm(lctrs.lookupSymbol("-->"), c, ruleC);
            if (z3.validity(valid)) return gamma;
        }
        throw new InvalidConstraintRewritingException("Cannot rewrite constraint, y is null");
    }

    protected boolean checkLVARcondition(FirstOrderRule r, Substitution y, Term c) {
        TreeSet<Variable> LVar = LVar(r.queryLeftSide(), r.queryRightSide(),
                r.queryConstraint());
        for (Variable v : LVar) {
            boolean inSubst = false;
            TreeSet<Variable> cvars = c.vars().getVars();
            for (Variable v2 : y.domain()) {
                Term yx = y.getReplacement(v);
                if (yx instanceof Var && cvars.contains(yx) || yx instanceof Constant) inSubst = true;
            }
            if (!inSubst) return false;
        }
        return true;
    }

    protected IProofState rewriteConstraintCalc(IProofState ps, EquivalenceProof proof) {
        TRS lctrs = proof.getLcTrs();
        ps.setS(applyConstraintSubstitutions(ps, ps.getC()));
        ps.setS(applyConstraintSubstitutions(ps, ps.getC()));
        Term newConstraint = freshSubstitutionsConstraint(ps, lctrs, proof);
        while(!ps.getC().equals(newConstraint)) {
            ps.setC(newConstraint);
            ps.setS(applyConstraintSubstitutions(ps, ps.getC()));
            newConstraint = freshSubstitutionsConstraint(ps, lctrs, proof);
        }
        ps.setS(simplifyValuesOnlyEquations(lctrs, ps.getS()));
        ps.setC(simplifyValuesOnlyEquations(lctrs, ps.getC()));
        return ps;
    }

    protected Term simplifyValuesOnlyEquations(TRS lctrs, Term t) {
        Z3TermHandler z3 = new Z3TermHandler(lctrs);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Position p : t.queryAllPositions()) {
                Term ti = t.querySubterm(p);
                if (ti.isConstant() || ti.isVariable()) continue;
                if (ti.isFunctionalTerm() && !ti.queryRoot().queryName().equals("-")) {
                    boolean allConst = true;
                    for (int j = 1; j < ti.numberImmediateSubterms() + 1; j++) {
                        Term tj = ti.queryImmediateSubterm(j);
                        if (!tj.isConstant() && !(tj.isFunctionalTerm() && tj.queryRoot().queryName().equals("-")
                                && tj.queryImmediateSubterm(1).isConstant())) {
                            allConst = false;
                        }
                    }
                    if (allConst && ti.numberImmediateSubterms() > 1 &&
                            lctrs.queryTheorySymbols().contains(ti.queryRoot())) {
                        t = t.replaceSubterm(p, z3.simplify(ti));
                        changed = true;
                    }
                }
            }
        }
        return t;
    }

    protected IProofState rewriteConstraintConstrainedRule(IProofState ps, EquivalenceProof proof, Position pos, int ruleIndex,
                                                    Substitution y) {
        Term applied = proof.getLcTrs().queryRule(ruleIndex).apply(ps.getS().querySubterm(pos)).substitute(y);
        ps.setS(ps.getS().replaceSubterm(pos, applied));
        ps.setT(ps.getT());
        return ps;
    }

    protected TreeSet<Variable> LVar(Term l, Term r, Term c) {
        TreeSet<Variable> LVar = new TreeSet<>();
        LVar.addAll(c.vars().getVars());
        for (Variable v : r.vars()) {
            if (!l.vars().contains(v)) LVar.add(v);
        }
        return LVar;
    }

    protected Term freshSubstitutionsConstraint(IProofState ps, TRS lctrs, EquivalenceProof proof) {
        Term temp = ps.getS();
        Term constraint = ps.getC();
        TreeSet<Variable> cVars = constraint.vars().getVars();
        ArrayList<Term> t_vars = new ArrayList<>();
        varsInFunctionalTerms(proof, temp, null, t_vars);
        for (Term t : t_vars) {
            Term eq = null;
            boolean LVarCondition = true;
            for (int i = 1; i <= t.numberImmediateSubterms(); i++) {
                if (!t.queryImmediateSubterm(i).isConstant() && (t.queryImmediateSubterm(i).isVariable()
                        && !cVars.contains(t.queryImmediateSubterm(i)))) LVarCondition = false;
            }
            if (t.queryType().equals(intSort) && LVarCondition) {
                eq = new FunctionalTerm(lctrs.lookupSymbol("==i"),
                        getFreshVar(proof, t.queryType()), t);
            } else if (t.queryType().equals(boolSort) && LVarCondition) {
                eq = new FunctionalTerm(lctrs.lookupSymbol("==b"),
                        getFreshVar(proof, t.queryType()), t);
            }
            if (LVarCondition) {
                constraint = new FunctionalTerm(lctrs.lookupSymbol("/\\"),
                        constraint, eq);
            }
        }
        return constraint;
    }

    protected void varsInFunctionalTerms(EquivalenceProof eq, Term c, Term parent, ArrayList<Term> vars) {
        if (c.isConstant()) return;
        if (c.isVariable()) {
            if (parent != null && eq.getLcTrs().queryTheorySymbols().contains(parent.queryRoot())) {
                if (!vars.contains(parent)) vars.add(parent);
            }
            return;
        }
        for (int i = 1; i < c.numberImmediateSubterms() + 1; i++) {
            varsInFunctionalTerms(eq, c.queryImmediateSubterm(i), c, vars);
        }
    }

    protected Term applyConstraintSubstitutions(IProofState p, Term c) {
        ArrayList<Term> c_eqs = new ArrayList<>();
        getEqualities(c, c_eqs);
        Term temp = p.getS();
        if (!c_eqs.isEmpty()) {
            for (Term eq : c_eqs) {
                Term eql = eq.queryImmediateSubterm(1);
                Term eqr = eq.queryImmediateSubterm(2);
                temp = replaceSubtermsInTerm(temp, eql, eqr);
            }
        }
        return temp;
    }

    protected void getEqualities(Term c, ArrayList<Term> equalities) {
        if (c.isFunctionalTerm() && c.queryImmediateSubterm(1).isVariable()
                && c.queryRoot().queryName().equals("==i")) {
            equalities.add(c);
        } else if (c.isFunctionalTerm() && c.queryImmediateSubterm(1).isVariable()
                && c.queryRoot().queryName().equals("==b")) {
            equalities.add(c);
        } else {
            for (int i = 1; i < c.numberImmediateSubterms() + 1; i++) {
                getEqualities(c.queryImmediateSubterm(i), equalities);
            }
        }
    }

    protected Term replaceSubtermsInTerm(Term t, Term eql, Term eqr) {
        for (Position pos : t.queryAllPositions()) {
            Term sub = t.querySubterm(pos);
            if (sub.isFunctionalTerm() && sub.equals(eql)) t = t.replaceSubterm(pos, eqr);
            else if (sub.isFunctionalTerm() && sub.equals(eqr)) t = t.replaceSubterm(pos, eql);
        }
        return t;
    }

    public Var getFreshVar(EquivalenceProof proof, Type expectedType) {
        return proof.getFreshVar(expectedType);
    }

    protected boolean isBasicTerm(Term t, EquivalenceProof _proof) {
        if (t.isVariable()) return true;
        boolean defined = false;
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
            if (t.queryRoot().equals(_proof.getLcTrs().queryRule(i).queryLeftSide().queryRoot())) defined = true;
        }
        if (!defined) return false;
        for (int i = 1; i <= t.numberImmediateSubterms(); i++) {
            if (!isConstructorTerm(t.queryImmediateSubterm(i), _proof)) return false;
        }
        return true;
    }

    protected boolean isConstructorTerm(Term t, EquivalenceProof _proof) {
        if (!t.isVariable() && _proof.getLcTrs().queryTheorySymbols().contains(t.queryRoot())) {
            for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
                Rule rule = _proof.getLcTrs().queryRule(i);
                if (!t.isVariable() && rule.queryLeftSide().queryRoot().equals(t.queryRoot())) return false;
            }
        }
        return true;
    }
}
