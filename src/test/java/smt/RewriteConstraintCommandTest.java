package smt;

import cora.exceptions.InvalidRuleParseException;
import cora.exceptions.ParserException;
import cora.exceptions.UnsatException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class RewriteConstraintCommandTest {
    private final static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (factiter\tInt -> Int)\n" +
            "    (iter\t\tInt Int Int -> Int)\n" +
            "\t(return\t\tInt -> Int)\n" +
            "    (factrec\tInt -> Int)\n" +
            "    (mul        Int Int -> Int)\n" +
            "   (f Int Int -> Int)\n" +
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

    @Test
    public void constraintRewrite1Test() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n < 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n >= 1 /\\ n > 2 /\\ n < 4] [n ==i 3]");
        Term c2 = LcTrsInputReader.readTermFromStringWithEnv("n ==i 3", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getConstraint().toString(), c2.toString());
    }

    @Test
    public void constraintRewrite2Test() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n < 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n > 2 /\\ n < 4] [n==i3]");
        Term c2 = LcTrsInputReader.readTermFromStringWithEnv("n >= 1 /\\ n ==i 3", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getConstraint().toString(), c2.toString());
    }

    @Test (expected = UnsatException.class)
    public void invalidConstraintRewrite2Test() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n > 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n >= 1 /\\ n > 2 /\\ n > 4] [n > 8]");
    }

    @Test (expected = InvalidRuleParseException.class)
    public void invalidConstraintRewriteTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n < 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n >= 1 /\\ n > 5 /\\ n < 4] [n==3]");
    }

    @Test (expected = UnsatException.class)
    public void unsatConstraintRewriteTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n < 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n >= 1 /\\ n > 2 /\\ n < 4] [n==i5]");
    }

    @Test (expected = UnsatException.class)
    public void unsatConstraintRewrite2Test() throws ParserException, InvalidRuleApplicationException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "[n >= 1 /\\ n > 2 /\\ n < 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [n >= 1 /\\ n > 2 /\\ n < 4] [n<=2]");
    }

    @Test
    public void unsatConstraintFreeVarTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "f(x, z)";
        String t2 = "f(x, z)";
        String c1 = "[x > y /\\ y >= z]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rewrite [x > y /\\ y >= z] [x > z]");
        assertFalse(eq.getCompleteness());
    }
}
