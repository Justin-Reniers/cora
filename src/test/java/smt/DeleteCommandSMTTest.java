package smt;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class DeleteCommandSMTTest {

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
    public void testDeleteEqualTermsConstants() throws ParserException {
        String t1 = "f(0)";
        String t2 = "f(0)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("delete");
        assertNull(eq.getCurrentEquation());
    }

    @Test
    public void testDeleteEqualTermsVariables() throws ParserException {
        String t1 = "f(x)";
        String t2 = "f(x)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("delete");
        assertNull(eq.getCurrentEquation());
    }

    @Test
    public void testDeleteUnsatConstraint() throws ParserException {
        String t1 = "f(0)";
        String t2 = "f(1)";
        String c1 = "[z ==i 0 /\\ z ==i 2]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("delete");
        assertNull(eq.getCurrentEquation());
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void testDeleteUnequalTerms() throws ParserException {
        String t1 = "f(0)";
        String t2 = "f(x)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("delete");
        assertNotNull(eq.getCurrentEquation());
    }

    @Test
    public void testDeleteVarReplacement() throws ParserException {
        String t1 = "f(z)";
        String t2 = "f(x + 0)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("delete");
        assertNull(eq.getCurrentEquation());
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void testDeleteVarUnequalReplacement() throws ParserException {
        String t1 = "f(x + 0)";
        String t2 = "f(y)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("delete");
    }
}
