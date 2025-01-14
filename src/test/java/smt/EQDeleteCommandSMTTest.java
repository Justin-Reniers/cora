package smt;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class EQDeleteCommandSMTTest {

    private final static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (sumiter    Int -> Result)\n" +
            "    (iter       Int Int Int -> Result)\n" +
            "    (return     Int -> Result)\n" +
            "    (sumrec     Int -> Result)\n" +
            "    (add        Int Result -> Result)\n" +
            "   (f Int -> Int)\n" +
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
    public void eqDeleteExampleTest() throws ParserException {
        String t1 = "return(n*x)";
        String t2 = "return(x_1)";
        String c1 = "[n ==i y /\\ m ==i n - 1 /\\ y_1 ==i y + 1 /\\ x_1 ==i x * y]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("eqdelete");
        String nc = "[n ==i y /\\ m ==i n - 1 /\\ y_1 ==i y + 1 /\\ x_1 ==i x * y /\\ ~(n*x ==i x_1)]";
        Term newC = LcTrsInputReader.readLogicalTermFromStringWithEnv(nc, lcTrs, eq.getEquationVariables());
        assertEquals(eq.getConstraint(), newC);
    }

    @Test
    public void example1() throws ParserException {

    }



    private final static TRS toolCheckLcTrs;

    private final static String toolCheck = "(SIG\n" +
            "   (f Int -> Int)\n" +
            "   (fio Int o -> o)\n" +
            "   (g Int -> o)\n" +
            "   (h Int -> o)\n" +
            "   (fo o -> o)\n" +
            "   (a    -> o)\n" +
            "   (b    -> o)\n" +
            ")\n" +
            "(RULES\n" +
            ")";

    static {
        try {
            toolCheckLcTrs = LcTrsInputReader.readLcTrsFromString(toolCheck);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void toolCheck1() throws ParserException {
        String t1 = "fo(a)";
        String t2 = "fo(b)";
        String c1 = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(t1, toolCheckLcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, toolCheckLcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, toolCheckLcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(toolCheckLcTrs, l, r, c);
        eq.applyNewUserCommand("eqdelete");
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void toolCheck2() throws ParserException {
        String t1 = "f(x)";
        String t2 = "f(y)";
        String c1 = "[x > 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, toolCheckLcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, toolCheckLcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, toolCheckLcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(toolCheckLcTrs, l, r, c);
        eq.applyNewUserCommand("eqdelete");
    }

    @Test
    public void toolCheck3() throws ParserException {
        String t1 = "f(x+1)";
        String t2 = "f(1+x)";
        String c1 = "[x > 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, toolCheckLcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, toolCheckLcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, toolCheckLcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(toolCheckLcTrs, l, r, c);
        eq.applyNewUserCommand("eqdelete");
        //assertNotEquals(l, eq.getLeft());
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void toolCheck4() throws ParserException {
        String t1 = "fio(x, g(z))";
        String t2 = "fio(y, h(x))";
        String c1 = "[x > 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, toolCheckLcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, toolCheckLcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, toolCheckLcTrs, vars);
        vars.addAll(c.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(toolCheckLcTrs, l, r, c);
        eq.applyNewUserCommand("eqdelete");
    }
}
