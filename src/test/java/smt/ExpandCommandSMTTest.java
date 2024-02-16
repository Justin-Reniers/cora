package smt;

import cora.exceptions.InvalidPositionException;
import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.rewriting.FirstOrderRule;
import cora.smt.Equation;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class ExpandCommandSMTTest {

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
    public void expandExampleTest() throws ParserException {
        String t1 = "factrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0 terminating");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        assertEquals(rule, eq.getLcTrs().queryRule(eq.getLcTrs().queryRuleCount()-1));
    }

    @Test
    public void expandNonTerminatingTest() throws ParserException {
        String t1 = "factrec(n)";
        String t2 = "factiter(n+1)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0 nonterminating");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        for (int i = 0; i < eq.getLcTrs().queryRuleCount(); i++) assertNotEquals(rule, eq.getLcTrs().queryRule(i));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void expandExampleTestTerminationNotSupported() throws ParserException {
        String t1 = "factrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        assertEquals(rule, eq.getLcTrs().queryRule(eq.getLcTrs().queryRuleCount()-1));
    }

    @Test (expected = InvalidPositionException.class)
    public void invalidExpandTest() throws ParserException {
        String t1 = "factrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        int eqSize = eq.getEquations().size();
        eq.applyNewUserCommand("expand 1.1");
        assertEquals(eqSize, eq.getEquations().size());
    }
}
