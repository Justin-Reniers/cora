package smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;
public class ConstraintRewritingTest {
    private final static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (factiter\tInt -> Int)\n" +
            "    (iter\t\tInt Int Int -> Int)\n" +
            "\t(return\t\tInt -> Int)\n" +
            "    (factrec\tInt -> Int)\n" +
            "    (mul        Int Int -> Int)\n" +
            "   (f Int -> Int)\n" +
            ")\n" +
            "(RULES\n" +
            "\tfactiter(x) -> iter(x, 1, 1)\n" +
            "\titer(x, z, i) -> iter(x, z*i, i+1)\t[i <= x]\n" +
            "\titer(x, z, i) -> return(z)\t\t\t[i > x]\n" +
            "\tfactrec(x) -> return(1)\t\t\t\t[x <= 1]\n" +
            "\tfactrec(x) -> mul(x, factrec(x-1))\t[x > 1]\n" +
            "\tmul(x, return(y)) -> return(x*1)\n" +
            ")\n";

    static {
        try {
            lcTrs = LcTrsInputReader.readLcTrsFromString(s);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstraintRewrite() throws ParserException {
        String lctrs = "(SIG\n" +
                "(f\t Int Int -> Int)\n" +
                "(g\t Int -> Int)\n" +
                ")\n" +
                "(RULES\n" +
                "\tf(y, z) -> g(1)\t[y>=z /\\ a == y + 1]\n" +
                ")\n";
        String t1 = "f(x + 1, 1)";
        String t2 = "f(x + 1, 1)";
        String c1 = "x >= 0";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(lctrs);
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("g(1)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft(), l2);
    }

    @Test
    public void testConstraintRewrite2() throws ParserException {
        String lctrs = "(SIG\n" +
                "(factrec\t Int -> Int)\n" +
                "(return\t Int -> Int)\n" +
                "(mul\t Int Int -> Int)\n" +
                "(iter\t Int Int Int -> Int)\n" +
                ")\n" +
                "(RULES\n" +
                "\tfactrec(x) -> return(1) [x <= 1]\n" +
                "\tfactrec(x) -> mul(x, factrec(x - 1)) [x > 1]\n" +
                ")\n";
        String t1 = "factrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "n >= 1";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(lctrs);
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("return(1)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft(), l2);
    }

    @Test
    public void testConstraintRewrite3() throws ParserException {
        String lctrs = "(SIG\n" +
                "(factrec\t Int -> Int)\n" +
                "(return\t Int -> Int)\n" +
                "(mul\t Int Int -> Int)\n" +
                "(iter\t Int Int Int -> Int)\n" +
                ")\n" +
                "(RULES\n" +
                "\tfactrec(x) -> return(1) [x <= 1]\n" +
                "\tfactrec(x) -> mul(x, factrec(x - 1)) [x > 1]\n" +
                ")\n";
        String t1 = "factrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "n >= 1";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(lctrs);
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 2");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("mul(x_0, factrec(x_0 - 1))", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft(), l2);
    }
}
