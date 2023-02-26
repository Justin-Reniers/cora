package cora.z3;

import com.microsoft.z3.*;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.loggers.Logger;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.types.ArrowType;
import cora.types.Sort;

import java.util.ArrayList;
import java.util.List;

import static cora.z3.Z3Helper.*;

public class Z3TermHandler {
    private Solver _s;
    private Context _ctx;
    private Z3Helper _z3Helper;

    public Z3TermHandler() {
        _ctx = new Context();
        _s = _ctx.mkSolver();
        _z3Helper = new Z3Helper();
    }

    public Expr deconstruct(Term t) {
        if (t.isConstant()) {
            if (t.queryType().equals(Sort.intSort)) return getIntVal(_ctx, Integer.parseInt(t.queryRoot().queryName()));
            if (t.queryType().equals(Sort.boolSort)) {
                return getBoolVal(_ctx, Boolean.parseBoolean(t.queryRoot().queryName()));
            }
        }
        if (t.isVariable()) {
            if (t.queryType().equals(Sort.intSort)) return getIntVar(_ctx, t.queryVariable().queryName());
            if (t.queryType().equals(Sort.boolSort)) return getBoolVar(_ctx, t.queryVariable().queryName());
        }
        if (t.isFunctionalTerm()) {
            switch(t.queryRoot().queryName()) {
                case "~":
                    return getNot(_ctx, deconstruct(t.queryImmediateSubterm(1)));
                case "/\\":
                    return getAnd(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "\\/":
                    return getOr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "-->":
                    return getImplies(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "<-->":
                    return getIff(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "<":
                    return getLtExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "<=":
                    return getLeExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case ">":
                    return getGtExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case ">=":
                    return getGeExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "==":
                    return getEqExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "!=":
                    return getNeqExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "*":
                    return getMultExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "/":
                    return getDivExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "%":
                    return getModExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "+":
                    return getAddExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "-":
                    return getSubExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                default:
                    boolean boolSort = false;
                    if (t.queryType().equals(Sort.boolSort)) boolSort = true;
                    if (t.queryType().equals(Sort.intSort)) boolSort = false;
                    ArrayList<Expr> subExprs = new ArrayList<>();
                    for (int i = 1; i < t.numberImmediateSubterms() + 1; i++) {
                        subExprs.add(deconstruct(t.queryImmediateSubterm(i)));
                    }
                    return getFunc(_ctx, t.queryRoot().queryName(), subExprs, boolSort);
            }
        }
        return null;
    }

    public Term simplify(Term t) {
        Expr e = deconstruct(t);
        if (e.getSort().equals(_ctx.getBoolSort())) {
            Goal g = _ctx.mkGoal(true, false, false); //params: models, unsatCores, proofs
            g.add(e);
            Tactic css = _ctx.mkTactic("ctx-solver-simplify");
            ApplyResult ar = css.apply(g);
            return reconstruct(ar.getSubgoals()[0].getFormulas()[0]);
        }
        e = e.simplify();
        return reconstruct(e);
    }

    private static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Term reconstruct(Expr e) {
        if (e.isInt() && !e.isApp()) {
            if (isNumeric(e.toString())) return new Constant(e.toString(), Sort.intSort);
            return new Var(e.toString(), Sort.intSort);
        }
        if (e.isBool() && !e.isApp()) {
            if (e.isTrue()) return new Constant("TRUE", Sort.boolSort);
            if (e.isFalse()) return new Constant("FALSE", Sort.boolSort);
            return new Var(e.toString(), Sort.boolSort);
        }
        if (e.isNot()) {
            if (e.getArgs()[0].isEq()) {
                Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
                return new FunctionalTerm(new Constant("!=", type), reconstruct(e.getArgs()[0]),
                        reconstruct(e.getArgs()[1]));
            } else {
                Type type = new ArrowType(Sort.boolSort, Sort.boolSort);
                return new FunctionalTerm(new Constant("~", type), reconstruct(e.getArgs()[0]));
            }
        }
        if (e.isAnd()) {
            Type type = new ArrowType(Sort.boolSort, new ArrowType(Sort.boolSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("/\\", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isOr()) {
            Type type = new ArrowType(Sort.boolSort, new ArrowType(Sort.boolSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("\\/", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isImplies()) {
            Type type = new ArrowType(Sort.boolSort, new ArrowType(Sort.boolSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("-->", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isIff()) {
            Type type = new ArrowType(Sort.boolSort, new ArrowType(Sort.boolSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("<-->", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isLT()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("<", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isLE()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("<=", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isGT()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
            return new FunctionalTerm(new Constant(">", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isGE()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
            return new FunctionalTerm(new Constant(">=", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isEq()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.boolSort));
            return new FunctionalTerm(new Constant("==", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isMul()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.intSort));
            return new FunctionalTerm(new Constant("*", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isDiv()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.intSort));
            return new FunctionalTerm(new Constant("/", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isModulus()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.intSort));
            return new FunctionalTerm(new Constant("%", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isAdd()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.intSort));
            return new FunctionalTerm(new Constant("+", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isSub()) {
            Type type = new ArrowType(Sort.intSort, new ArrowType(Sort.intSort, Sort.intSort));
            return new FunctionalTerm(new Constant("-", type), reconstruct(e.getArgs()[0]),
                    reconstruct(e.getArgs()[1]));
        }
        if (e.isApp()) {
            Type type;
            List<Term> args = new ArrayList<>();
            if (e.getSort().equals(_ctx.getIntSort())) type = Sort.intSort;
            else type = Sort.boolSort;
            for (int i = 0; i < e.getNumArgs(); i++) {
                Expr expr = e.getArgs()[i];
                if (expr.getSort().equals(_ctx.getIntSort())) type = new ArrowType(Sort.intSort, type);
                else type = new ArrowType(Sort.boolSort, type);
                args.add(reconstruct(expr));
            }
            String fSymbol = e.toString().replace("(", "");
            fSymbol = fSymbol.split(" ", 2)[0];
            if (e.getNumArgs() <= 0) return new Var(fSymbol, type);
            return new FunctionalTerm(new Constant(fSymbol, type), args);
        }
        return null;
    }

    public void constraintCheck(Term ruleConstraint, Term proofConstraint, Substitution s) {
        Expr rc = deconstruct(ruleConstraint);
        Expr pc = deconstruct(proofConstraint);
        Expr e = getAnd(_ctx, rc, pc);
        for (Variable v : s.domain()) {
            Expr ass = getEqExpr(_ctx, deconstruct(v), deconstruct(s.getReplacement(v)));
            e = getAnd(_ctx, e, ass);
        }
        _s.add(e);
    }

    public Context getContext() {
        return _ctx;
    }

    public Model getModel() {
        return Z3Helper.getModel(_s);
    }

    public void printContext(Context ctx) {

    }

    public Substitution getSubstitutions(Term c) {
        Set<Expr> equalities = new HashSet<>();
        getEqualities(c, equalities);
        Substitution s = new Subst();
        for (Expr e : equalities) {
            Term arg1 = reconstruct(e.getArgs()[0]);
            Term arg2 = reconstruct(e.getArgs()[1]);
            if (arg1 instanceof Var) s.extend((Var) arg1, arg2);
            else if (arg2 instanceof Var) s.extend((Var) arg2, arg1);
        }
        return s;
    }

    private void getEqualities(Term c, Set equalities) {
        if (c.isFunctionalTerm() && c.queryRoot().queryName().equals("==")) {
            equalities.add(deconstruct(c));
        } else {
            for (int i = 1; i < c.numberImmediateSubterms() + 1; i++) {
                getEqualities(c.queryImmediateSubterm(i), equalities);
            }
        }
    }
}
