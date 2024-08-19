package cora.usercommands;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.InvalidSubstitutionTypingException;
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

import static cora.types.Sort.boolSort;
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
        if (gamma == null) gamma = new Subst();
        Term l = proof.getLeft();
        Term r = proof.getRight();
        Term c = proof.getConstraint();
        Term ruleC = proof.getLcTrs().queryRule(ruleIndex).queryConstraint().substitute(gamma);//.substitute(s);
        Term ruleL = proof.getLcTrs().queryRule(ruleIndex).queryLeftSide().substitute(gamma);//.substitute(s);
        Term ruleR = proof.getLcTrs().queryRule(ruleIndex).queryRightSide().substitute(gamma);//.substitute(s);
        Term lAtPos = l.querySubterm(pos);
        Substitution y = ruleL.match(lAtPos);
        //ruleC = ruleC.substitute(y);
        Term t = l.replaceSubterm(pos, ruleR);
        ArrayList<Term> ruleConstraints = new ArrayList<>();
        getEqualities(ruleC, ruleConstraints);
        for (Term eq : ruleConstraints) {
            if (eq.queryImmediateSubterm(1).isVariable()) {
                y.extend((Variable)eq.queryImmediateSubterm(1), eq.queryImmediateSubterm(2));
            }
        }
        TreeSet<Variable> LVar = LVar(ruleL, ruleR, ruleC);
        if (y == null) return null;

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
            Z3TermHandler z3 = new Z3TermHandler(proof.getLcTrs());
            if (z3.validity(c, ruleC.substitute(gamma), proof.getLcTrs().lookupSymbol("-->"))) return gamma;
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
        Term l = proof.getLeft();
        Term r = proof.getRight();
        Term proofRuleConstraint = proof.getLcTrs().queryRule(ruleIndex).queryConstraint();
        proofRuleConstraint = proofRuleConstraint.substitute(y);
        getEqualities(proofRuleConstraint, ceqs);
        Term newConstraint = proof.getConstraint();
        Substitution y2 = proofRuleConstraint.match(newConstraint);
        Substitution repl = new Subst();
        ArrayList<Term> proofConstraintEqs = new ArrayList<>();
        getEqualities(newConstraint, proofConstraintEqs);
        for (Term eq : ceqs) {
            if (eq.queryImmediateSubterm(2).isConstant()) continue;
            else {
                for (Variable v : y.domain()) {
                    for (Term eqp : proofConstraintEqs) {
                        Term e = eqp.queryImmediateSubterm(2).substitute(y);
                        if (y.getReplacement(v).equals(eqp.queryImmediateSubterm(2))) {
                            repl.extend((Variable) eqp.queryImmediateSubterm(1), v);
                        }
                    }
                }
            }
            repl.extend((Variable) eq.queryImmediateSubterm(1), eq.queryImmediateSubterm(2));
            newConstraint = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"),
                    newConstraint, eq);
        }
        proof.setConstraint(newConstraint.substitute(y));
        Term applied = proof.getLcTrs().queryRule(ruleIndex).apply(proof.getLeft().querySubterm(pos)).substitute(y);
        proof.setLeft(proof.getLeft().replaceSubterm(pos, applied).substitute(y));
        proof.setRight(proof.getRight().substitute(y));
    }

    protected Term addSubstitutionToConstraint(EquivalenceProof proof, Term c, Substitution y) {
        for (Variable v : y.domain()) {
            if (v.queryType().equals(intSort)) {
                c = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"), c,
                        new FunctionalTerm(proof.getLcTrs().lookupSymbol("==i"), v, y.getReplacement(v)));
            } else if (v.queryType().equals(boolSort)) {
                c = new FunctionalTerm(proof.getLcTrs().lookupSymbol("/\\"), c,
                        new FunctionalTerm(proof.getLcTrs().lookupSymbol("==b"), v, y.getReplacement(v)));
            } else {
                throw new InvalidSubstitutionTypingException(v, y.getReplacement(v));
            }
        }
        return c;
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
            Term eq = null;
            if (t.queryType().equals(intSort)) {
                eq = new FunctionalTerm(proof.getLcTrs().lookupSymbol("==i"),
                        getFreshVar(proof, t.queryType()), t);
            }
            else if (t.queryType().equals(boolSort)) {
                eq = new FunctionalTerm(proof.getLcTrs().lookupSymbol("==b"),
                        getFreshVar(proof, t.queryType()), t);
            }
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

    protected void getEqualities(Substitution y, ArrayList<Term> equalities, EquivalenceProof proof) {
        for (Variable v : y.domain()) {
            if (v.queryType().equals(intSort)) {
                equalities.add(new FunctionalTerm(proof.getLcTrs().lookupSymbol("==i"), v, y.getReplacement(v)));
            } else if (v.queryType().equals(boolSort)) {
                equalities.add(new FunctionalTerm(proof.getLcTrs().lookupSymbol("==b"), v, y.getReplacement(v)));
            } else {
                throw new InvalidSubstitutionTypingException(v, y.getReplacement(v));
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
