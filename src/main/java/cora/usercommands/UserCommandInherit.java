package cora.usercommands;

import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.smt.EquivalenceProof;
import cora.terms.FunctionalTerm;
import cora.terms.Subst;
import cora.terms.Var;
import cora.z3.Z3TermHandler;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * A "User Command" is a rewriting rule that is to be applied to an equivalence proof.
 * This inherit provides default functionality for such rewriting rules.
 */
abstract class UserCommandInherit {

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected Substitution rewrittenConstraintValid(EquivalenceProof proof, int ruleIndex, Position pos) {
        Substitution s = freshVariables(proof, (FirstOrderRule) proof.getLcTrs().queryRule(ruleIndex));
        Term l = proof.getLeft();
        Term r = proof.getRight();
        Term c = proof.getConstraint();
        Term ruleC = proof.getLcTrs().queryRule(ruleIndex).queryConstraint().substitute(s);
        Term ruleL = proof.getLcTrs().queryRule(ruleIndex).queryLeftSide().substitute(s);
        Term ruleR = proof.getLcTrs().queryRule(ruleIndex).queryRightSide().substitute(s);
        Term lAtPos = l.querySubterm(pos);
        Substitution y = lAtPos.unify(ruleL);
        if (y == null) return null;
        TreeSet<Variable> LVar = LVar(ruleL, ruleR, ruleC);
        for (Variable v : LVar) {
            boolean inSubst = false;
            for (Variable v2 : y.domain()) {
                if (y.getReplacement(v2).equals(v) || v.equals(v2)) inSubst = true;
            }
            if (!inSubst) return null;
        }
        if (pos != null && ruleIndex >= 0) {
            ArrayList<Term> ceqs = new ArrayList<>();
            getEqualities(proof.getLcTrs().queryRule(ruleIndex).queryConstraint(), ceqs);
            Term newConstraint = c;
            for (Term eq : ceqs) {
                if (eq.queryImmediateSubterm(2).isConstant()) continue;
                newConstraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
                        newConstraint, eq);
            }
            newConstraint = newConstraint.substitute(y);
            Term checkValidity = new FunctionalTerm(proof.getLcTrs().lookupSymbol("-->"), c.substitute(y),
                    newConstraint);
            Z3TermHandler z3 = new Z3TermHandler(proof.getLcTrs());
            if (z3.validity(checkValidity)) return y;
        }
        return null;
    }

    protected void rewriteConstraintCalc(EquivalenceProof proof) {
        proof.setLeft(applyConstraintSubstitutions(proof, proof.getConstraint()));
         Term newConstraint = freshSubstitutionsConstraint(proof);
         while(!proof.getConstraint().equals(newConstraint)) {
             proof.setConstraint(freshSubstitutionsConstraint(proof));
             proof.setLeft(applyConstraintSubstitutions(proof, proof.getConstraint()));
            newConstraint = freshSubstitutionsConstraint(proof);
         }
         Z3TermHandler z3 = new Z3TermHandler(proof.getLcTrs());
        proof.setLeft(z3.simplify(proof.getLeft()));
        proof.setConstraint(z3.simplify(proof.getConstraint()));
    }

    protected void rewriteConstraintConstrainedRule(EquivalenceProof proof, Position pos, int ruleIndex,
                                                    Substitution y) {
        ArrayList<Term> ceqs = new ArrayList<>();
        getEqualities(proof.getLcTrs().queryRule(ruleIndex).queryConstraint(), ceqs);
        Term newConstraint = proof.getConstraint();
        for (Term eq : ceqs) {
            if (eq.queryImmediateSubterm(2).isConstant()) continue;
            newConstraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
                newConstraint, eq);
        }
        proof.setConstraint(newConstraint.substitute(y));
        Term applied = proof.getLcTrs().queryRule(ruleIndex).apply(proof.getLeft().querySubterm(pos));
        proof.setLeft(proof.getLeft().replaceSubterm(pos, applied).substitute(y));
        proof.setRight(proof.getRight().substitute(y));
    }

    protected TreeSet<Variable> LVar(Term l, Term r, Term c) {
        TreeSet<Variable> LVar = new TreeSet<>();
        for (Variable v : r.vars()) {
            if (!l.vars().contains(v)) LVar.add(v);
        }
        LVar.addAll(c.vars().getVars());
        return LVar;
    }

    protected Term freshSubstitutionsConstraint(EquivalenceProof proof) {
        Term temp = proof.getLeft();
        Term constraint = proof.getConstraint();
        ArrayList<Term> t_vars = new ArrayList<>();
        containsVarsInFunctionalTerms(temp, t_vars);
        for (int i = 0; i < t_vars.size(); i++) {
            Term t = t_vars.get(i);
            Term eq = new FunctionalTerm(proof.getLcTrs().lookupSymbol("=="),
                    getFreshVar(proof, t.queryType()), t);
            constraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
                    constraint, eq);
        }
        return constraint;
    }

    protected void containsVarsInFunctionalTerms(Term c, ArrayList<Term> vars) {
        ArrayList<String> ops = new ArrayList<>(Arrays.asList("-", "*", "/", "%", "+"));
        if (c.isFunctionalTerm() && !ops.contains(c.queryRoot().queryName())) {
            for (int i = 1; i < c.numberImmediateSubterms() + 1; i++) {
                containsVarsInFunctionalTerms(c.queryImmediateSubterm(i), vars);
            }
        } else if (c.isFunctionalTerm() && ops.contains(c.queryRoot().queryName())) {
            if (c.queryImmediateSubterm(1).isVariable()) vars.add(c);
            else if (c.queryImmediateSubterm(2).isVariable()) vars.add(c);
            else {
                if (c.queryImmediateSubterm(1).isConstant() || c.queryImmediateSubterm(2).isConstant()) return;
                containsVarsInFunctionalTerms(c.queryImmediateSubterm(1), vars);
                containsVarsInFunctionalTerms(c.queryImmediateSubterm(2), vars);
            }
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
        if (c.isFunctionalTerm() && c.queryRoot().queryName().equals("==")) {
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

    private void checkVarsInConstraint(Term ruleConstraint, TreeSet<Variable> vars) {
        for (Variable v : ruleConstraint.queryImmediateSubterm(1).vars().getVars()) {
            if (!vars.contains(v)) return;

        }
        vars.addAll(ruleConstraint.queryImmediateSubterm(1).vars().getVars());
        if (ruleConstraint.queryImmediateSubterm(2) != null) {
            checkVarsInConstraint(ruleConstraint.queryImmediateSubterm(2), vars);
        }
    }

    protected Term freshVariableConstraint(EquivalenceProof proof, int ruleIndex) {
        TRS lcTrs = proof.getLcTrs();
        Term t = proof.getLeft();
        Term ruleConstraint = lcTrs.queryRule(ruleIndex).queryConstraint();
        Substitution fVars = freshVariables(proof, (FirstOrderRule) lcTrs.queryRule(ruleIndex));
        Term ruleConstraintFreshVars = ruleConstraint.substitute(fVars);
        Term ruleLeft = lcTrs.queryRule(ruleIndex).queryLeftSide().substitute(fVars);
        Substitution s2 = ruleLeft.unify(t);
        ArrayList<Term> subst = new ArrayList<>();
        equalitiesFromSubstitution(proof, s2, subst);
        Term freshConstraint = new FunctionalTerm(lcTrs.lookupSymbol("/\\"),
                proof.getConstraint(), ruleConstraintFreshVars);
        for (Term eq : subst) {
            if (eq.queryImmediateSubterm(2).isConstant()) continue;
            freshConstraint = new FunctionalTerm(lcTrs.lookupSymbol("/\\"),
                    freshConstraint, eq);
        }
        return freshConstraint;
    }

    private void equalitiesFromSubstitution(EquivalenceProof proof, Substitution s, ArrayList<Term> eqs) {
        for (Variable v : s.domain()) {
            eqs.add(new FunctionalTerm(proof.getLcTrs().lookupSymbol("=="), v, s.getReplacement(v)));
        }
    }

    private Substitution freshVariables(EquivalenceProof proof, FirstOrderRule r) {
        Subst s = new Subst();
        for (Variable v : r.queryLeftSide().vars()) {
            Var fresh = getFreshVar(proof, v.queryType());
            s.extend(v, fresh);
        }
        return s;
    }

    public Var getFreshVar(EquivalenceProof proof, Type expectedType) {
        return proof.getFreshVar(expectedType);
    }

    protected void addTermToConstraint(EquivalenceProof proof, Term eq) {
        Term c = proof.getConstraint();
        Term newAnd = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"), c, eq);
        proof.setConstraint(newAnd);
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
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
            Rule rule = _proof.getLcTrs().queryRule(i);
            if (!t.isVariable() && rule.queryLeftSide().queryRoot().equals(t.queryRoot())) return false;
        }
        return true;
    }

    protected boolean isConstructorTerm (FunctionSymbol f, EquivalenceProof proof) {
        for (int i = 0; i < proof.getLcTrs().queryRuleCount(); i++) {
            Rule rule = proof.getLcTrs().queryRule(i);
            if (!f.isVariable() && rule.queryLeftSide().queryRoot().equals(f)) return false;
        }
        return true;
    }
}
