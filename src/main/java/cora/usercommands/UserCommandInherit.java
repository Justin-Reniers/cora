package cora.usercommands;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.smt.EquivalenceProof;
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
    protected Substitution rewrittenConstraintValid(EquivalenceProof proof, int ruleIndex, Position pos,
                                                    Substitution gamma) {
        if (gamma == null) gamma = new Subst();
        Term s = proof.getLeft();
        Term c = proof.getConstraint();
        Term ruleC = proof.getLcTrs().queryRule(ruleIndex).queryConstraint().substitute(gamma);//.substitute(s);
        Term ruleL = proof.getLcTrs().queryRule(ruleIndex).queryLeftSide().substitute(gamma);//.substitute(s);
        Term sAtPos = s.querySubterm(pos);
        Substitution y = ruleL.match(sAtPos);
        gamma.compose(y);
        if (y == null) return null;
        Z3TermHandler z3 = new Z3TermHandler(proof.getLcTrs());
        if (!checkLVARcondition(proof, ruleIndex, gamma, c)) return null;
        ruleC = ruleC.substitute(gamma);
        if (pos != null && ruleIndex >= 0) {
            if (z3.validity(c, ruleC, proof.getLcTrs().lookupSymbol("-->"))) return gamma;
        }
        return null;
    }

    protected boolean checkLVARcondition(EquivalenceProof eq, int ruleIndex, Substitution y, Term c) {
        FirstOrderRule r = (FirstOrderRule) eq.getLcTrs().queryRule(ruleIndex);
        TreeSet<Variable> LVar = LVar(r.queryLeftSide(), r.queryRightSide(),
                r.queryConstraint());
        for (Variable v : LVar) {
            boolean inSubst = false;
            TreeSet<Variable> cvars = c.vars().getVars();
            for (Variable v2 : y.domain()) {
                Term yx = y.getReplacement(v);
                if (yx instanceof Var && cvars.contains(yx)) inSubst = true;
                else {
                    for (Position p : c.queryAllPositions()) {
                        if (yx.equals(c.querySubterm(p))) inSubst = true;
                    }
                }
                if (y.getReplacement(v2).equals(v) || v.equals(v2)) inSubst = true;
            }
            if (!inSubst) return false;
        }
        return true;
    }

    protected void rewriteConstraintCalc(EquivalenceProof proof) {
        proof.setLeft(applyConstraintSubstitutions(proof, proof.getConstraint()));
        Term newConstraint = freshSubstitutionsConstraint(proof);
        while(!proof.getConstraint().equals(newConstraint)) {
            proof.setConstraint(newConstraint);
            proof.setLeft(applyConstraintSubstitutions(proof, proof.getConstraint()));
            newConstraint = freshSubstitutionsConstraint(proof);
        }
        proof.setLeft(simplifyValuesOnlyEquations(proof, proof.getLeft()));
        proof.setConstraint(simplifyValuesOnlyEquations(proof, proof.getConstraint()));
    }

    protected Term simplifyValuesOnlyEquations(EquivalenceProof eq, Term t) {
        Z3TermHandler z3 = new Z3TermHandler(eq.getLcTrs());
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
                            eq.getLcTrs().queryTheorySymbols().contains(ti.queryRoot())) {
                        t = t.replaceSubterm(p, z3.simplify(ti));
                        changed = true;
                    }
                }
            }
        }
        return t;
    }

    protected void rewriteConstraintConstrainedRule(EquivalenceProof proof, Position pos, int ruleIndex,
                                                    Substitution y) {
        Term applied = proof.getLcTrs().queryRule(ruleIndex).apply(proof.getLeft().querySubterm(pos)).substitute(y);
        proof.setLeft(proof.getLeft().replaceSubterm(pos, applied));
        proof.setRight(proof.getRight());
    }

    protected TreeSet<Variable> LVar(Term l, Term r, Term c) {
        TreeSet<Variable> LVar = new TreeSet<>();
        LVar.addAll(c.vars().getVars());
        for (Variable v : r.vars()) {
            if (!l.vars().contains(v)) LVar.add(v);
        }
        return LVar;
    }

    protected Term freshSubstitutionsConstraint(EquivalenceProof proof) {
        Term temp = proof.getLeft();
        Term constraint = proof.getConstraint();
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
                eq = new FunctionalTerm(proof.getLcTrs().lookupSymbol("==i"),
                        getFreshVar(proof, t.queryType()), t);
            } else if (t.queryType().equals(boolSort) && LVarCondition) {
                eq = new FunctionalTerm(proof.getLcTrs().lookupSymbol("==b"),
                        getFreshVar(proof, t.queryType()), t);
            }
            if (LVarCondition) {
                constraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
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

    protected Term applyConstraintSubstitutions(EquivalenceProof proof, Term constraint) {
        ArrayList<Term> c_eqs = new ArrayList<>();
        getEqualities(constraint, c_eqs);
        Term temp = proof.getLeft();
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
