package cora.z3;

import com.microsoft.z3.*;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Position;
import cora.interfaces.terms.Term;
import cora.loggers.Logger;
import cora.types.Sort;

import java.util.ArrayList;
import java.util.Arrays;

import static cora.z3.Z3Helper.*;

public class Z3TermDeconstructor {
    private Solver _s;
    private Context _ctx;
    private Z3Helper _z3Helper;

    public Z3TermDeconstructor() {
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
            }
        }
        return null;
    }

    public Context getContext() {
        return _ctx;
    }

    public Model getModel() {
        return Z3Helper.getModel(_s);
    }

    public void printContext(Context ctx) {

    }
}
