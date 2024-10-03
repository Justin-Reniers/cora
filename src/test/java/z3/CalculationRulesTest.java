package z3;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import cora.terms.Var;
import cora.z3.Z3Helper;
import cora.z3.Z3TermHandler;
import org.junit.Test;

import java.util.TreeSet;

import static cora.types.Sort.intSort;
import static org.junit.Assert.*;
import static cora.z3.Z3Helper.*;
public class Z3TermHandlerTest {
    private static Context _ctx = new Context();
    private static Solver _s = _ctx.mkSolver();
    private static Z3Helper _z3Helper = new Z3Helper();
    private static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (factiter\tInt -> Int)\n" +
            "    (iter\t\tInt Int Int -> Int)\n" +
            "\t(return\t\tInt -> Int)\n" +
            "    (factrec\tInt -> Int)\n" +
            "    (mul        Int Int -> Int)\n" +
            "   (f Int Int -> Int)\n" +
            "   (g Int -> Int)\n" +
            "   (h Int Int Int -> Int)\n" +
            ")\n" +
            "(RULES\n" +
            "\tfactiter(x) -> iter(x, 1, 1)\n" +
            "\titer(x, z, i) -> iter(x, z*i, i+1)\t[i <= x]\n" +
            "\titer(x, z, i) -> return(z)\t\t\t[i > x]\n" +
            "\tfactrec(x) -> return(1)\t\t\t\t[x <= 1]\n" +
            "\tfactrec(x) -> mul(x, factrec(x-1))\t[x > 1]\n" +
            "\tmul(x, return(y)) -> return(x*y)\n" +
            ")\n";

    static {
        try {
            lcTrs = LcTrsInputReader.readLcTrsFromString(s);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }
    private static Z3TermHandler z3 = new Z3TermHandler(_ctx, _s, _z3Helper, lcTrs);

    @Test
    public void testANegAContextSatisfiability() {
        Expr<BoolSort> e1 = getBoolVar(_ctx, "x");
        Expr<BoolSort> e2 = getNot(_ctx, e1);
        Expr<BoolSort> e3 = getNot(_ctx, e2);
        assertTrue(z3.satisfiable(e2));
    }

    @Test
    public void testConflictingExpressionSatisfiability() {
        Expr<BoolSort> e1 = getBoolVar(_ctx, "x");
        Expr<BoolSort> e2 = getNot(_ctx, e1);
        Expr<BoolSort> e3 = getAnd(_ctx, e1, e2);
        assertFalse(z3.satisfiable(e3));
    }

    @Test
    public void testSimplificationCase() throws ParserException {
        String sl = "x + 0";
        String sr = "g(x)";
        String sc = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term t = eq.getLeft();
        assertTrue(eq.getLeft().isVariable());
    }

    @Test
    public void testSimplificationCase2() throws ParserException {
        String sl = "x*0";
        String sr = "g(x)";
        String sc = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertTrue(eq.getLeft().isVariable());
    }

    @Test
    public void testSimplificationCase3() throws ParserException {
        String sl = "x*1";
        String sr = "g(x)";
        String sc = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertTrue(eq.getLeft().isVariable());
    }

    @Test
    public void testSimplificationCase5() throws ParserException {
        String sl = "g(x) + 0";
        String sr = "g(x)";
        String sc = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertEquals(l, eq.getLeft());
    }
}
