package cora.z3;

import com.microsoft.z3.*;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * The Z3Helper is a class that acts as a bridge between
 */
public class Z3Helper {

    public Expr<BoolSort> deconstructTerm(Term t) {
        return null;
    }

    public static IntExpr getIntVar(Context ctx, String varName) {
        return ctx.mkIntConst(varName);
    }

    public static IntExpr getIntVal(Context ctx, int val) {
        return ctx.mkInt(val);
    }

    public static Expr<IntSort> getAddExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkAdd(e1, e2);
    }

    public static Expr<IntSort> getSubExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkSub(e1, e2);
    }

    public static Expr<IntSort> getMultExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkMul(e1, e2);
    }

    public static Expr<IntSort> getDivExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkDiv(e1, e2);
    }

    public static IntExpr getModExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkMod(e1, e2);
    }

    public static BoolExpr getBoolVar(Context ctx, String varName) {
        return ctx.mkBoolConst(varName);
    }

    public static BoolExpr getBoolVal(Context ctx, boolean val) {
        return ctx.mkBool(val);
    }

    public static BoolExpr getLtExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkLt(e1, e2);
    }

    public static BoolExpr getLeExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkLe(e1, e2);
    }

    public static BoolExpr getGtExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkGt(e1, e2);
    }

    public static BoolExpr getGeExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkGe(e1, e2);
    }

    public static BoolExpr getEqExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkEq(e1, e2);
    }

    public static BoolExpr getNeqExpr(Context ctx, Expr<IntSort> e1, Expr<IntSort> e2) {
        return ctx.mkNot(getEqExpr(ctx, e1, e2));
    }

    public static BoolExpr getNot(Context ctx, Expr<BoolSort> e1) {
        return ctx.mkNot(e1);
    }

    public static BoolExpr getAnd(Context ctx, Expr<BoolSort> e1, Expr<BoolSort> e2) {
        return ctx.mkAnd(e1, e2);
    }

    public static BoolExpr getOr(Context ctx, Expr<BoolSort> e1, Expr<BoolSort> e2) {
        return ctx.mkOr(e1, e2);
    }

    public static BoolExpr getImplies(Context ctx, Expr<BoolSort> e1, Expr<BoolSort> e2) {
        return ctx.mkImplies(e1, e2);
    }

    public static BoolExpr getIff(Context ctx, Expr<BoolSort> e1, Expr<BoolSort> e2) {
        return ctx.mkIff(e1, e2);
    }

    public static Expr getFunc(Context ctx, String fName, ArrayList<Expr> exprs, boolean boolSort) {
        Sort[] sortArr = new Sort[exprs.size()];
        Expr[] exprArr = new Expr[exprs.size()];
        for (int i = 0; i < exprs.size(); i++) {
            sortArr[i] = exprs.get(i).getSort();
            exprArr[i] = exprs.get(i);
        }
        FuncDecl f;
        if (boolSort) f = ctx.mkFuncDecl(fName, sortArr, ctx.getBoolSort());
        else f = ctx.mkFuncDecl(fName, sortArr, ctx.getIntSort());
        Expr func = ctx.mkApp(f, exprArr);
        return func;
    }

    public static Model getModel(Solver s) {
        Status q = s.check();
        if (q == Status.SATISFIABLE) {
            //Logger.log("SAT");
            return s.getModel();
        }
        //if (q == Status.UNSATISFIABLE) Logger.log("UNSAT");
        return null;
    }

    public void addExprToSolver(Solver s, ArrayList<BoolExpr> exprs) {
        for (BoolExpr e : exprs) s.add(e);
    }

    public static Expr<?> getModelConst(Model m, Expr<?> e) {
        return m.getConstInterp(e);
    }
}
