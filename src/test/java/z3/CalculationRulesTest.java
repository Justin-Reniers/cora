package z3;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import cora.z3.Z3Helper;
import cora.z3.Z3TermHandler;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;
import static cora.z3.Z3Helper.*;
public class CalculationRulesTest {
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
            "\tg(x) -> x\n" +
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
        String sc = "[x >= 2]";
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
        String sc = "[x ==i 3]";
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
        String sc = "[x < 18]";
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

    @Test
    public void testNoFreshVariables() throws ParserException {
        String sl = "g(x+1+y+3) + 0";
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
        //assertEquals(l, eq.getLeft());
        System.out.println(eq.getConstraint());
        assertEquals(c, eq.getConstraint());
    }

    @Test
    public void testMultipleFreshVariables2() throws ParserException {
        String sl = "g(x + (y + 3))";
        String sr = "g(x)";
        String sc = "[x < 3 /\\ y > 1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        //assertEquals(l, eq.getLeft());
        assertNotEquals(c, eq.getConstraint());
    }

    @Test
    public void testFreshVariables() throws ParserException {
        String sl = "g(1+x)";
        String sr = "g(x)";
        String sc = "[x > 1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertNotEquals(l, eq.getLeft());
    }

    @Test
    public void testFreshVariables2() throws ParserException {
        String sl = "g(1+x)";
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
        //assertEquals(sc, eq.getConstraint());
    }

    @Test
    public void testFreshVariables3() throws ParserException {
        String sl = "g(x)";
        String sr = "g(x)";
        String sc = "[x ==i y + 1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertEquals(l, eq.getLeft());
        assertEquals(c, eq.getConstraint());
    }

    @Test
    public void testFreshVariables4() throws ParserException {
        String sl = "g(-x)";
        String sr = "g(x)";
        String sc = "[x > 2]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertNotEquals(l, eq.getLeft());
    }

    @Test
    public void testFreshVariables5() throws ParserException {
        String sl = "f(x, 2+1)";
        String sr = "g(x)";
        String sc = "[x ==i 2+1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("f(x, x)", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables6() throws ParserException {
        String sl = "g(0)";
        String sr = "g(x)";
        String sc = "[x ==i 0]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(x)", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables7() throws ParserException {
        String sl = "g((2*y)+1)";
        String sr = "g(x)";
        String sc = "[x ==i (2*y)+1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(x)", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables8() throws ParserException {
        String sl = "g(1 + (2*y))";
        String sr = "g(x)";
        String sc = "[x ==i 1 + (2*y)]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(x)", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables9() throws ParserException {
        String sl = "g(x+2)";
        String sr = "g(x)";
        String sc = "[x ==i 2]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(x_0)", lcTrs, eq.getCurrentEqVariables());
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables10() throws ParserException {
        String sl = "g(y)";
        String sr = "g(z)";
        String sc = "[y ==i 3]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(y)", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables11() throws ParserException {
        String sl = "g(y)";
        String sr = "g(z)";
        String sc = "[y ==i 3]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 7");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("y", lcTrs, vars);
        eq.applyNewUserCommand("simplify");
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables12() throws ParserException {
        String sl = "g(y)";
        String sr = "g(z)";
        String sc = "[y ==i y_0 + 1 /\\ y_0 ==i z + 1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 7");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("y", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariables13() throws ParserException {
        String sl = "g(x + (y + 3))";
        String sr = "g(z)";
        String sc = "[x >= y]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("g(x_0)", lcTrs, eq.getCurrentEqVariables());
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testFreshVariablesNotAllowed() throws ParserException {
        String sl = "g(y)";
        String sr = "g(z)";
        String sc = "[y ==i y_0 + 1 /\\ y_0 ==i z + 1]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 7");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("y", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testNoNewVariables() throws ParserException {
        String sl = "factrec(x+(y+1))";
        String sr = "g(z)";
        String sc = "[x>=y]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("factrec(x_0)", lcTrs, eq.getCurrentEqVariables());
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testReduction2() throws ParserException {
        String sl = "factrec(2*x+1)";
        String sr = "g(z)";
        String sc = "[x > 0]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 5");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("mul(2*x+1, factrec(2*x+1+-1))", lcTrs,
                eq.getCurrentEqVariables());
        Term cComp = LcTrsInputReader.readLogicalTermFromStringWithEnv("[x>0]", lcTrs, eq.getCurrentEqVariables());
        assertEquals(comp, eq.getLeft());
        assertEquals(cComp, eq.getConstraint());
    }

    @Test
    public void testReduction3() throws ParserException {
        String sl = "factrec(5)";
        String sr = "g(z)";
        String sc = "[x ==i 5]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 5");
        Term comp = LcTrsInputReader.readTermFromStringWithEnv("mul(5, factrec(5+-1))", lcTrs, vars);
        assertEquals(comp, eq.getLeft());
    }

    @Test
    public void testReduction4() throws ParserException {
        String sl = "factrec(x+2)";
        String sr = "g(z)";
        String sc = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        //Term comp = LcTrsInputReader.readTermFromStringWithEnv("mul(5, factrec(5+-1))", lcTrs, vars);
        assertEquals(l, eq.getLeft());
    }

    @Test
    public void testReduction5() throws ParserException {
        String sl = "factrec(x+2)";
        String sr = "g(z)";
        String sc = "[x ==i 2 + 2]";
        Term l = LcTrsInputReader.readTermFromString(sl, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(sr, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(sc, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertNotEquals(l, eq.getLeft());
        assertNotEquals(c, eq.getConstraint());
    }
}
