package smt;

import cora.exceptions.ParserException;
import cora.interfaces.terms.Term;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.CoraInputReader;
import cora.parsers.LcTrsInputReader;
import cora.parsers.TrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;
import static org.junit.Assert.*;
import cora.interfaces.rewriting.TRS;


public class SimplifyCommandSMTTest {
    public static Logger l = new Logger(new ConsoleLogger());

    private static TRS lcTrs;

    private static String s = "(SIG\n" +
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
    public void testCalcSimplify() throws ParserException {
        String t1 = "f(x + 0)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        Term r = LcTrsInputReader.readTermFromString(t2, lcTrs);
        Term c = LcTrsInputReader.readTermFromString(c1, lcTrs);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromString("f(x_0)", lcTrs);
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyFreshVars() throws ParserException {
        String t1 = "f(x + 1)";
        String t2 = "f(z)";
        String c1 = "TRUE";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        Term r = LcTrsInputReader.readTermFromString(t2, lcTrs);
        Term c = LcTrsInputReader.readTermFromString(c1, lcTrs);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromString("f(x_0)", lcTrs);
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleTrueConstraint() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        Term r = LcTrsInputReader.readTermFromString(t2, lcTrs);
        Term c = LcTrsInputReader.readTermFromString(c1, lcTrs);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 1 0");
        Term l2 = LcTrsInputReader.readTermFromString("iter(n, 1, 1)", lcTrs);
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleConstrained() throws ParserException {
        String t1 = "iter(n, 1, 1)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        Term r = LcTrsInputReader.readTermFromString(t2, lcTrs);
        Term c = LcTrsInputReader.readTermFromString(c1, lcTrs);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 1 1");
        Term l2 = LcTrsInputReader.readTermFromString("iter(n, 1*1, 1+1)", lcTrs);
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    private static EquivalenceProof testProof(String filePath) throws Exception {
        TRS rec_fac = readInput(filePath);
        Term l = LcTrsInputReader.readTermFromString("factrec(n)", rec_fac);
        Term r = LcTrsInputReader.readTermFromString("factiter(n)", rec_fac);
        Term c = LcTrsInputReader.readTermFromString("n >= 1", rec_fac);
        return new EquivalenceProof(rec_fac, l, r, c);
    }

    @Test
    public void equivalenceProofDemo() throws Exception {
        EquivalenceProof p = testProof("D:\\Uni\\Master Thesis\\cora backup\\cora\\src\\test\\java\\smt\\utils\\recursive_fact.lctrs");
        p.applyNewUserCommand("swap");
        p.applyNewUserCommand("simplify 1 0");
        p.applyNewUserCommand("simplify 1 1");
        p.applyNewUserCommand("simPLIFY");
        p.applyNewUserCommand("swap");
        p.saveStateToFile("savestate.out");
        Logger.finalized();
    }
}
