package cora.usercommands;

import cora.exceptions.InvalidRuleApplicationException;
import cora.interfaces.rewriting.Rule;
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
import java.util.Arrays;
import java.util.TreeSet;

import static cora.types.Sort.intSort;

/**
 * A "User Command" is a rewriting rule that is to be applied to an equivalence proof.
 * This inherit provides default functionality for such rewriting rules.
 */
abstract class UserCommandInherit {

    /** Helper function to return the current classname for use in Errors. */
    private String queryMyClassName() { return "RuleInherit (" + this.getClass().getSimpleName() + ")"; }

    protected Substitution rewrittenConstraintValid(EquivalenceProof proof, int ruleIndex, Position pos,
                                                    Substitution gamma) {
        //Substitution s = freshVariables(proof, (FirstOrderRule) proof.getLcTrs().queryRule(ruleIndex));
        Term l = proof.getLeft();
        Term r = proof.getRight();
        Term c = proof.getConstraint();
        Term ruleC = proof.getLcTrs().queryRule(ruleIndex).queryConstraint();//.substitute(s);
        Term ruleL = proof.getLcTrs().queryRule(ruleIndex).queryLeftSide();//.substitute(s);
        Term ruleR = proof.getLcTrs().queryRule(ruleIndex).queryRightSide();//.substitute(s);
        Term lAtPos = l.querySubterm(pos);
        Substitution y = new Subst();
        if (ruleL.match(lAtPos) == null) {
            if (lAtPos.match(ruleL) != null) y = lAtPos.match(ruleL);
        } else y = ruleL.match(lAtPos);
        //ruleC = ruleC.substitute(y);
        Term t = l.replaceSubterm(pos, ruleR);
        ArrayList<Term> ruleConstraints = new ArrayList<>();
        getEqualities(ruleC, ruleConstraints);
        for (Term eq : ruleConstraints) {
            y.extend((Variable) eq.queryImmediateSubterm(1), eq.queryImmediateSubterm(2));
        }
        if (gamma != null) {
            for (Variable v : gamma.domain()) y.extend(v, gamma.get(v));
        }
        for (Variable v : ruleC.vars().getVars()) {
            if (v.queryName().equals("x")) y.extend(v, new Constant("1", intSort));
            if (v.queryName().equals("y")) y.extend(v, new Constant("2", intSort));
        }
        TreeSet<Variable> LVar = LVar(ruleL, ruleR, ruleC);
        if (y == null) return null;
        System.out.println(y.domain());
        System.out.println(y);
        System.out.println(LVar);

        for (Variable v : LVar) {
            TreeSet<Variable> cvars = c.vars().getVars();
            boolean inSubst = false;
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
            if (!inSubst) return null;
        }
        if (pos != null && ruleIndex >= 0) {
            ArrayList<Term> ceqs = new ArrayList<>();
            getEqualities(ruleC, ceqs);
            Term newConstraint = c;
            for (Term eq : ceqs) {
                if (eq.queryImmediateSubterm(2).isConstant()) continue;
                newConstraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
                        newConstraint, eq);
            }
            newConstraint = newConstraint.substitute(y);
            Z3TermHandler z3 = new Z3TermHandler(proof.getLcTrs());
            if (z3.validity(c.substitute(y), newConstraint, proof.getLcTrs().lookupSymbol("-->"))) return y;
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
        LVar.addAll(c.vars().getVars());
        for (Variable v : r.vars()) {
            if (!l.vars().contains(v)) LVar.add(v);
        }
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

    private Substitution freshVariables(EquivalenceProof proof, FirstOrderRule r) {
        Subst s = new Subst();
        for (Variable v : r.queryLeftSide().vars()) {
            Var fresh = getFreshVar(proof, v.queryType());
            s.extend(v, fresh);
        }
        for (Variable v : r.queryRightSide().vars()) {
            if (s.domain().contains(v)) continue;
            Var fresh = getFreshVar(proof, v.queryType());
            s.extend(v, fresh);
        }
        for (Variable v : r.queryConstraint().vars()) {
            if (s.domain().contains(v)) continue;
            Var fresh = getFreshVar(proof, v.queryType());
            s.extend(v, fresh);
        }
        return s;
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
        for (int i = 0; i < _proof.getLcTrs().queryRuleCount(); i++) {
            Rule rule = _proof.getLcTrs().queryRule(i);
            if (!t.isVariable() && rule.queryLeftSide().queryRoot().equals(t.queryRoot())) return false;
        }
        return true;
    }
}