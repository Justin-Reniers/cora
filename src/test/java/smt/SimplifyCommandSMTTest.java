package smt;

import cora.exceptions.ParserException;
import cora.interfaces.logging.ILogger;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.CoraInputReader;
import cora.parsers.LcTrsInputReader;
import cora.parsers.TrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;
import static org.junit.Assert.*;
import cora.interfaces.rewriting.TRS;

import java.util.TreeSet;


public class SimplifyCommandSMTTest {

    private final static TRS lcTrs;

    //private static ILogger il = (ILogger) (ILogger) new Logger(new ConsoleLogger());

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
        String t1 = "f(1 + 1)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(2)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyNestedOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(3)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1 * 1)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(3)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedMinusOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1 + -1 * 6)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(-3)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedOperators2() throws ParserException {
        String t1 = "f(1 + 2 / 2 + 1 + 1 * 6 * 3 * 1)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(21)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyVarInConstraint() throws ParserException {
        String t1 = "f(x + 0)";
        String t2 = "f(z)";
        String c1 = "z == x + 0";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertEquals(eq.getLeft(), eq.getRight());
    }

    @Test
    public void testCalcSimplifyFreshVars() throws ParserException {
        String t1 = "f(x + 1)";
        String t2 = "f(z)";
        String c1 = "TRUE";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
    }

    @Test
    public void testSimplifyRuleTrueConstraint() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 1, 1)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleConstrained() throws ParserException {
        String t1 = "iter(n, 1, 1)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 2");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(x_0, 1*1, 1+1)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleUnconstrained() throws ParserException {
        String t1 = "factiter(n)";
        String t2 = "factrec(n)";
        String c1 = "n >= 1";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 1, 1)", lcTrs, eq.getVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    private static EquivalenceProof testProof(String filePath) throws Exception {
        TRS rec_fac = readInput(filePath);
        Term l = LcTrsInputReader.readTermFromString("factrec(n)", rec_fac);
        Term r = LcTrsInputReader.readTermFromString("factiter(n)", rec_fac);
        Term c = LcTrsInputReader.readTermFromString("n >= 1", rec_fac);
        return new EquivalenceProof(rec_fac, l, r, c);
    }

    private static EquivalenceProof testProof() throws Exception {
        //TRS rec_fac = readInput(filePath);
        Term l = LcTrsInputReader.readTermFromString("factrec(n)", lcTrs);
        TreeSet<Variable> vars = new TreeSet<Variable>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv("factiter(n)", lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readTermFromStringWithEnv("n >= 1", lcTrs, vars);
        return new EquivalenceProof(lcTrs, l, r, c);
    }

    @Test
    public void equivalenceProofDemo() throws Exception {
        EquivalenceProof p = testProof();//testProof("D:\\Uni\\Master Thesis\\cora backup\\cora\\src\\test\\java\\smt\\utils\\recursive_fact.lctrs");
        p.applyNewUserCommand("swap");
        p.applyNewUserCommand("simplify 0 1");
        p.applyNewUserCommand("simplify 0 2");
        p.applyNewUserCommand("simPLIFY");
        p.saveStateToFile("savestate.out");
        //Logger.finalized();
    }
}
