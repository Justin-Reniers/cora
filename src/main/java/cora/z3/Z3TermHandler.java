package cora.z3;

import com.microsoft.z3.*;
import cora.exceptions.IndexingError;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.*;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.types.Sort;
import java.util.*;

import static cora.z3.Z3Helper.*;
import static java.lang.Integer.parseInt;

public class Z3TermHandler {
    private Solver _s;
    private Context _ctx;
    private Z3Helper _z3Helper;
    private TRS _lcTrs;

    private static final ArrayList<String> fsymbols = new ArrayList<>(Arrays.asList(
            "-", "*", "/", "%", "+"));

    public Z3TermHandler(TRS lcTrs) {
        _ctx = new Context();
        _s = _ctx.mkSolver();
        _z3Helper = new Z3Helper();
        _lcTrs = lcTrs;
    }

    public Z3TermHandler(Context ctx, Solver s, Z3Helper z3h, TRS lctrs) {
        _ctx = ctx;
        _s = s;
        _z3Helper = z3h;
        _lcTrs = lctrs;
    }

    public Expr deconstruct(Term t) {
        if (t.isConstant()) {
            if (t.queryType().equals(Sort.intSort)) return getIntVal(_ctx, parseInt(t.queryRoot().queryName()));
            if (t.queryType().equals(Sort.boolSort)) {
                return getBoolVal(_ctx, Boolean.parseBoolean(t.queryRoot().queryName()));
            }
        }
        if (t.isVariable()) {
            if (t.queryType().equals(Sort.intSort)) {
                return getIntVar(_ctx, t.queryVariable().queryName() + t.queryVariable().queryVariableIndex());
            }
            if (t.queryType().equals(Sort.boolSort)) {
                return getBoolVar(_ctx, t.queryVariable().queryName() + t.queryVariable().queryVariableIndex());
            }
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
                case "==i", "==b":
                    return getEqExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                            deconstruct(t.queryImmediateSubterm(2)));
                case "!=i", "!=b":
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
                    try {
                        return getSubExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)),
                                deconstruct(t.queryImmediateSubterm(2)));
                    } catch (IndexingError e) {
                        return getUnaryMinusExpr(_ctx, deconstruct(t.queryImmediateSubterm(1)));
                    }
                default:
                    boolean boolSort = t.queryType().equals(Sort.boolSort);
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
        Environment env = t.vars();
        Expr e = deconstruct(t);
        e = e.simplify();
        return reconstruct(e, env);
    }

    private static boolean isNumeric(String s) {
        try {
            parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Term reconstruct(Expr e, Environment env) {
        if (e.isUMinus()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("-"), reconstruct(e.getArgs()[0], env));
        }
        if (e.isInt() && !e.isApp()) {
            if (isNumeric(e.toString())) {
                int val = parseInt(e.toString());
                if (val >= 0) return new Constant(String.valueOf(val), Sort.intSort);
                return new FunctionalTerm(_lcTrs.lookupSymbol("-"), new Constant(String.valueOf(val*-1),
                        Sort.intSort));
            }
            //return new Var(e.toString(), Sort.intSort);
        }
        if (e.isBool() && !e.isApp()) {
            if (e.isTrue()) return _lcTrs.lookupSymbol("TRUE");
            if (e.isFalse()) return _lcTrs.lookupSymbol("FALSE");
            //return new Var(e.toString(), Sort.boolSort);
        }
        if (e.isNot()) {
            if (e.getArgs()[0].isEq() && e.getArgs()[1].isBool()) {
                return new FunctionalTerm(_lcTrs.lookupSymbol("!=b"), reconstruct(e.getArgs()[0], env),
                        reconstruct(e.getArgs()[1], env));
            } else if (e.getArgs()[0].isEq() && e.getArgs()[1].isInt()) {
                return new FunctionalTerm(_lcTrs.lookupSymbol("!=i"), reconstruct(e.getArgs()[0], env),
                        reconstruct(e.getArgs()[1], env));
            } else {
                return new FunctionalTerm(_lcTrs.lookupSymbol("~"), reconstruct(e.getArgs()[0], env));
            }
        }
        if (e.isAnd()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("/\\"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isOr()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("\\/"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isImplies()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("-->"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isIff()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("<-->"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isLT()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("<"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isLE()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("<="), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isGT()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol(">"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isGE()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol(">="), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isEq()) {
            if (e.getArgs()[0].isBool()) {
                return new FunctionalTerm(_lcTrs.lookupSymbol("==b"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
            } else {
                return new FunctionalTerm(_lcTrs.lookupSymbol("==i"), reconstruct(e.getArgs()[0], env),
                        reconstruct(e.getArgs()[1], env));
            }
        }
        if (e.isMul()) {
            if (e.getArgs()[0].isInt())
            return new FunctionalTerm(_lcTrs.lookupSymbol("*"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isDiv()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("/"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isModulus()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("%"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isAdd()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("+"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        if (e.isSub()) {
            return new FunctionalTerm(_lcTrs.lookupSymbol("-"), reconstruct(e.getArgs()[0], env),
                    reconstruct(e.getArgs()[1], env));
        }
        /**if (e.isApp()) {
            Type type;
            List<Term> args = new ArrayList<>();
            if (e.getSort().equals(_ctx.getIntSort())) type = Sort.intSort;
            else type = Sort.boolSort;
            for (int i = 0; i < e.getNumArgs(); i++) {
                Expr expr = e.getArgs()[i];
                if (expr.getSort().equals(_ctx.getIntSort())) type = new ArrowType(Sort.intSort, type);
                else type = new ArrowType(Sort.boolSort, type);
                args.add(reconstruct(expr, env));
            }
            String fSymbol = e.toString().replace("(", "");
            fSymbol = fSymbol.split(" ", 2)[0];
            if (e.getNumArgs() <= 0) {
                for (Variable v : env) {
                    if (v.queryName().equals(e.toString())) {
                        return v;
                    }
                }
            }
            if (_lcTrs.lookupSymbol(fSymbol) != null) return new FunctionalTerm(_lcTrs.lookupSymbol(fSymbol), args);
            return new FunctionalTerm(new Constant(fSymbol, type), args);
        }**/
        return null;
    }

    public boolean validity(Term valid) {
        Expr v = deconstruct(valid);
        Expr e = getNot(_ctx, v);
        return satisfiable(e) == SatisfiabilityEnum.UNSAT;
    }

    public SatisfiabilityEnum getSatStatus() {
        return Z3Helper.getModel(_s);
    }

    public SatisfiabilityEnum satisfiable(Term constraint) {
        Expr e = deconstruct(constraint);
        _s.add(e);
        return getSatStatus();
    }

    public SatisfiabilityEnum satisfiable(Expr constraint) {
        _s.add(constraint);
        return getSatStatus();
    }
}
