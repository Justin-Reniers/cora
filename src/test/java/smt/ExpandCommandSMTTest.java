package smt;

import cora.exceptions.InvalidPositionException;
import cora.exceptions.ParserException;
import cora.exceptions.invalidruleapplications.InvalidRuleApplicationException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.rewriting.FirstOrderRule;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class ExpandCommandSMTTest {

    private final static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (sumiter    Int -> Result)\n" +
            "    (iter       Int Int Int -> Result)\n" +
            "    (return     Int -> Result)\n" +
            "    (sumrec     Int -> Result)\n" +
            "    (add        Int Result -> Result)\n" +
            "    (f Int -> Int)\n" +
            ")\n" +
            "(RULES\n" +
            "    sumiter(x) -> iter(x, 0, 0)\n" +
            "    iter(x, z, i) -> iter(x, z+i, i+1)  [i <= x]\n" +
            "    iter(x, z, i) -> return(z)          [i > x]\n" +
            "    sumrec(x) -> return(0)              [x <= 0]\n" +
            "    sumrec(x) -> add(x, sumrec(x-1))    [x > 0]\n" +
            "    add(x, return(y)) -> return(x+y)\n" +
            ")";

    static {
        try {
            lcTrs = LcTrsInputReader.readLcTrsFromString(s);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void expandExampleTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "sumrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0 terminating");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        assertEquals(rule, eq.getLcTrs().queryRule(eq.getLcTrs().queryRuleCount()-1));
    }

    @Test
    public void expandExampleTest2() throws ParserException, InvalidRuleApplicationException {
        String t1 = "iter(n, a, b)";
        String t2 = "add(n, iter(m, x, y))";
        String c1 = "[n>=y/\\m==i n+-1/\\b==iy+1/\\a==ix*y]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0 terminating");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        assertEquals(rule, eq.getLcTrs().queryRule(eq.getLcTrs().queryRuleCount()-1));
    }

    @Test
    public void expandNonTerminatingTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "sumrec(n)";
        String t2 = "sumiter(n+1)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0 nonterminating");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        for (int i = 0; i < eq.getLcTrs().queryRuleCount(); i++) assertNotEquals(rule, eq.getLcTrs().queryRule(i));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void expandExampleTestTerminationNotSupported() throws ParserException, InvalidRuleApplicationException {
        String t1 = "sumrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("expand 0");
        FirstOrderRule rule = new FirstOrderRule(l, r, c);
        assertEquals(rule, eq.getLcTrs().queryRule(eq.getLcTrs().queryRuleCount()-1));
    }

    @Test (expected = InvalidPositionException.class)
    public void invalidExpandTest() throws ParserException, InvalidRuleApplicationException {
        String t1 = "sumrec(n)";
        String t2 = "iter(n, 1, 2)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        int eqSize = eq.getEquations().size();
        eq.applyNewUserCommand("expand 1.1");
        assertEquals(eqSize, eq.getEquations().size());
    }
}
