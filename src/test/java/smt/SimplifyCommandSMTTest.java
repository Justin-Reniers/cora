package smt;

import cora.exceptions.InvalidRuleApplicationException;
import cora.exceptions.ParserException;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
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

    private final static String s = "(SIG\n" +
            "    (sumiter    Int -> Result)\n" +
            "    (iter       Int Int Int -> Result)\n" +
            "    (return     Int -> Result)\n" +
            "    (sumrec     Int -> Result)\n" +
            "    (add        Int Result -> Result)\n" +
            "   (f Int -> Int)\n" +
            "   (fl Int -> Int)\n" +
            "   (g Int Int -> Int)\n" +
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
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(2)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifySubstitution() throws ParserException {
        String t1 = "g(x, f(z))";
        String t2 = "z";
        String c1 = "[x > z + y /\\ y ==i 3]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify ε 9 [q := z]");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("z", lcTrs, vars);
        assertEquals(eq.getLeft(), l2);
    }

    @Test
    public void testSimplifySubstitutionInfoInConstraint() throws ParserException {
        String t1 = "f(x+3)";
        String t2 = "f(a)";
        String c1 = "[x ==i 3]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(x_0)", lcTrs, eq.getCurrentEqVariables());
        assertEquals(eq.getLeft(), l2);
    }

    @Test
    public void testMultipleFreshVars() throws ParserException {
        String t1 = "f(x + y + 1)";
        String t2 = "f(x)";
        String c1 = "[x <= 2 /\\ y >= 4]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(x_1)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
        System.out.println(eq.getConstraint());
    }

    @Test
    public void testCalcSimplifyNestedOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1)";
        String t2 = "f(z)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(3)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1 * 1)";
        String t2 = "f(z)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(3)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedMinusOperators() throws ParserException {
        String t1 = "f(1 + 1 + 1 + -1 * 6)";
        String t2 = "f(z)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(-3)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testCalcSimplifyManyNestedOperators2() throws ParserException {
        String t1 = "f(1 + 2 / 2 + 1 + 1 * 6 * 3 * 1)";
        String t2 = "f(z)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(21)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyNestedPosition() throws ParserException {

    }

    @Test
    public void testSimplifyVarInConstraint() throws ParserException {
        String t1 = "f(x + 0)";
        String t2 = "f(z)";
        String c1 = "[z ==i x + 0]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        assertEquals(eq.getLeft(), eq.getRight());
    }

    @Test
    public void testCalcSimplifyFreshVars() throws ParserException {
        String t1 = "f(x + 1)";
        String t2 = "f(z)";
        String c1 = "[TRUE]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
    }

    @Test
    public void testSimplifyRuleTrueConstraint() throws ParserException {
        String t1 = "sumiter(n)";
        String t2 = "sumrec(n)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 0, 0)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleConstrained() throws ParserException {
        String t1 = "iter(n, 1, 1)";
        String t2 = "sumrec(n)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 2");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 1+1, 1+1)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyRuleUnconstrained() throws ParserException {
        String t1 = "sumiter(n)";
        String t2 = "sumrec(n)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 0, 0)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyEmptyPositionSymbol() throws ParserException {
        String t1 = "sumiter(n)";
        String t2 = "sumrec(n)";
        String c1 = "[n >= 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify ε 1");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("iter(n, 0, 0)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test (expected = InvalidRuleApplicationException.class)
    public void testSimplifyUnusedVariables() throws ParserException {
        String t1 = "iter(k, 2, 3)";
        String t2 = "add(k, iter(l, 1, 2))";
        String c1 = "[~(k <= 1) /\\ l ==i k - 1]";
        String rule = "iter(n, a, b) -> add(n, iter(m, x, y))\t[n>=1 /\\ m ==i n - 1 /\\ b ==i y + 1 /\\ a ==i x * y]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify ε 7 [x := 1, y := 2]");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("add(k, iter(n-1, 1, 2))", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }



    @Test  (expected = InvalidRuleApplicationException.class)
    public void testSimplifyUnusedVariablesNoSubstitution() throws ParserException {
        String t1 = "iter(k, 2, 3)";
        String t2 = "add(k, iter(l, 1, 2))";
        String c1 = "[~(k <= 1) /\\ l ==i k - 1]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify ε 7");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("add(k, iter(n-1, 1, 2))", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft().toString(), l2.toString());
    }

    @Test
    public void testSimplifyComposition() throws ParserException {
        String t1 = "fl(x)";
        String t2 = "x";
        String c1 = "[x ==i 3]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify 0 8");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("x", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft(), l2);
    }

    @Test
    public void testSimplifyExtra() throws ParserException {
        String t1 = "f(y_0)";
        String t2 = "f(x)";
        String c1 = "[y ==i 2 /\\ y_0 ==i y + 3]";
        Term l = LcTrsInputReader.readTermFromString(t1, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv(t2, lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv(c1, lcTrs, vars);
        EquivalenceProof eq = new EquivalenceProof(lcTrs, l, r, c);
        eq.applyNewUserCommand("simplify");
        Term l2 = LcTrsInputReader.readTermFromStringWithEnv("f(y_0)", lcTrs, eq.getEquationVariables());
        assertEquals(eq.getLeft(), l2);
    }

    private static EquivalenceProof testProof(String filePath) throws Exception {
        TRS rec_fac = readInput(filePath);
        Term l = LcTrsInputReader.readTermFromString("factrec(n)", rec_fac);
        Term r = LcTrsInputReader.readTermFromString("factiter(n)", rec_fac);
        Term c = LcTrsInputReader.readTermFromString("n >= 1", rec_fac);
        return new EquivalenceProof(rec_fac, l, r, c);
    }

    private static EquivalenceProof testProof() throws Exception {
        Term l = LcTrsInputReader.readTermFromString("sumrec(n)", lcTrs);
        TreeSet<Variable> vars = new TreeSet<Variable>();
        vars.addAll(l.vars().getVars());
        Term r = LcTrsInputReader.readTermFromStringWithEnv("sumiter(n)", lcTrs, vars);
        vars.addAll(r.vars().getVars());
        Term c = LcTrsInputReader.readLogicalTermFromStringWithEnv("[n >= 1]", lcTrs, vars);
        return new EquivalenceProof(lcTrs, l, r, c);
    }

    @Test
    public void equivalenceProofDemo() throws Exception {
        EquivalenceProof p = testProof();
        p.applyNewUserCommand("swap");
        p.applyNewUserCommand("simplify 0 1");
        p.applyNewUserCommand("simplify 0 2");
        p.applyNewUserCommand("simPLIFY");
        p.saveStateToFile("savestate.out");
    }
}
