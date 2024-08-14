package smt;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.CoraInputReader;
import cora.parsers.LcTrsInputReader;
import cora.parsers.TrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class SwapCommandSMTTest {

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
            "\tmul(x, return(y)) -> return(x*y)\n" +
            ")\n";

    static {
        try {
            lcTrs = LcTrsInputReader.readLcTrsFromString(s);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i >= 0) return filename.substring(i+1);
        return "";
    }

    private static TRS readInput(String file) throws Exception {
        String extension = getExtension(file);
        if (extension.equals("trs") || extension.equals("mstrs")) {
            return TrsInputReader.readTrsFromFile(file);
        }
        if (extension.equals("cora")) {
            return CoraInputReader.readProgramFromFile(file);
        }
        if (extension.equals("lctrs")) {
            return LcTrsInputReader.readLcTrsFromFile(file);
        }
        throw new Exception("Unknown file extension: " + extension + ".");
    }

    @Test
    public void swapNoArgsTest() throws ParserException {
        String t1 = "f(1 + 1)";
        String t2 = "f(z)";
        String c1 = "z ==i x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("swap");
        assertEquals(eq.getLeft(), r);
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void swapInvalidIndexTest() throws ParserException {
        String t1 = "f(1 + 1)";
        String t2 = "f(z)";
        String c1 = "z ==i x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("swap 1 2");
        assertEquals(eq.getLeft(), r);
    }
}
