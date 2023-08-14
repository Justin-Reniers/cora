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

public class RenameCommandSMTTest {
    private final static TRS lcTrs;

    private final static String s = "(SIG\n" +
            "    (factiter\tInt -> Int)\n" +
            "    (iter\t\tInt Int Int -> Int)\n" +
            "\t(return\t\tInt -> Int)\n" +
            "    (factrec\tInt -> Int)\n" +
            "    (mul        Int Int -> Int)\n" +
            "   (f Int -> Int)\n" +
            "   (h Int Int Int -> Int)\n" +
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
    public void renameTest() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1 /\\ n > 2 /\\ n < 4";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rename n x");
        for (Variable v : vars) assertFalse(eq.getVariables().contains(v));
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void invalidRenameTest() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1 /\\ n > 2 /\\ n < 4";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rename n n");
        for (Variable v : vars) assertFalse(eq.getVariables().contains(v));
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void invalidRename2Test() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1 /\\ n > 2 /\\ n < 4";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rename x y");
        for (Variable v : vars) assertFalse(eq.getVariables().contains(v));
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void invalidRename3Test() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1 /\\ n > 2 /\\ n < 4";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rename factiter(n) y");
        for (Variable v : vars) assertFalse(eq.getVariables().contains(v));
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void invalidRename4Test() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1 /\\ n > 2 /\\ n < 4";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("rename x factrec(n)");
        for (Variable v : vars) assertFalse(eq.getVariables().contains(v));
    }
}
