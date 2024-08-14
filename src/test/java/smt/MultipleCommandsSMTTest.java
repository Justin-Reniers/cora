package smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.TreeSet;

public class MultipleCommandsSMTTest {
    private final static TRS lcTrsfact, lcTrssum, lcTrsreverse;

    private final static String fact = "(SIG\n" +
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
            "\tmul(x, return(y)) -> return(x*y)\n" +
            ")\n";

    static {
        try {
            lcTrsfact = LcTrsInputReader.readLcTrsFromString(fact);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    private final static String sum = "(SIG\n" +
            "    (sumiter\tInt -> Int)\n" +
            "    (iter\t\tInt Int Int -> Int)\n" +
            "\t(return\t\tInt -> Int)\n" +
            "    (sumrec\tInt -> Int)\n" +
            "    (add        Int Int -> Int)\n" +
            ")\n" +
            "(RULES\n" +
            "\tsumiter(x) -> iter(x, 0, 0)\n" +
            "\titer(x, z, i) -> iter(x, z*i, i+1)\t[i <= x]\n" +
            "\titer(x, z, i) -> return(z)\t\t\t[i > x]\n" +
            "\tsumrec(x) -> return(0)\t\t\t\t[x <= 0]\n" +
            "\tsumrec(x) -> add(x, sumrec(x-1))\t[x > 0]\n" +
            "\tadd(x, return(y)) -> return(x+y)\n" +
            ")\n";

    static {
        try {
            lcTrssum = LcTrsInputReader.readLcTrsFromString(sum);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String reverse = "(SIG\n" +
            "    (nil    -> Int)\n" +
            "    (cons   Int Int -> Int)\n" +
            "    (rev    Int -> Int)\n" +
            "    (app    Int Int -> Int)\n" +
            ")\n" +
            "(RULES\n" +
            "    rev(nil)            ->  nil\n" +
            "    rev(cons(x, y))     ->  app(y, cons(x, nil))\n" +
            "    app(nil, x)         ->  x\n" +
            "    app(cons(x, y), z)  ->  cons(x, app(y, z))\n" +
            ")";

    static {
        try {
            lcTrsreverse = LcTrsInputReader.readLcTrsFromString(reverse);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFactProof() throws ParserException {
        String l = "factiter(n)";
        String r = "factrec(n)";
        String c = "n>=1";
        Term tl = LcTrsInputReader.readTermFromString(l, lcTrsfact);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(tl.vars().getVars());
        Term tr = LcTrsInputReader.readTermFromStringWithEnv(r, lcTrsfact, vars);
        vars.addAll(tr.vars().getVars());
        Term tc = LcTrsInputReader.readTermFromStringWithEnv(c, lcTrsfact, vars);
        vars.addAll(tc.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrsfact, tl, tr, tc);

        eq.applyNewUserCommand("simplify 0 1");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0 terminating");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 3");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 7");
        eq.applyNewUserCommand("postulate mul(n, iter(m, x, y)) iter(n, a, b) " +
                "[n>=y/\\m==in-1/\\b==iy+1/\\a==ix*y]");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0 terminating");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 8 [x_1 := x_1, x := 1, y := 2, a := 2, b := 3]");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 3");
        eq.applyNewUserCommand("simplify 0 6");
        eq.applyNewUserCommand("eqdelete");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("simplify 0 8");
        eq.applyNewUserCommand("delete");

        assertTrue(eq.getEquations().isEmpty());
    }

    @Test
    public void testSumProof() throws ParserException {
        String l = "sumiter(n)";
        String r = "sumrec(n)";
        String c = "n>=0";
        Term tl = LcTrsInputReader.readTermFromString(l, lcTrssum);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(tl.vars().getVars());
        Term tr = LcTrsInputReader.readTermFromStringWithEnv(r, lcTrssum, vars);
        vars.addAll(tr.vars().getVars());
        Term tc = LcTrsInputReader.readTermFromStringWithEnv(c, lcTrssum, vars);
        vars.addAll(tc.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrssum, tl, tr, tc);

        eq.applyNewUserCommand("simplify 0 1");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0 terminating");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 3");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 7");
        eq.applyNewUserCommand("postulate add(n, iter(m, x, y)) iter(n, a, b) " +
                "[n>=y/\\m==i n-1/\\b==i y+1/\\a==i x+y]");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0 terminating");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 8 [x_1 := x_1, x := 1, y := 2, a:= 2, b:=3]");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 3");
        eq.applyNewUserCommand("simplify 0 6");
        eq.applyNewUserCommand("eqdelete");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("simplify 0 8");
        eq.applyNewUserCommand("delete");

        assertTrue(eq.getEquations().isEmpty());
    }

    @Test
    public void testSumProofNotInductiveTheorem() throws ParserException {
        String l = "sumiter(n)";
        String r = "sumrec(n+1)";
        String c = "n>=0";
        Term tl = LcTrsInputReader.readTermFromString(l, lcTrssum);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(tl.vars().getVars());
        Term tr = LcTrsInputReader.readTermFromStringWithEnv(r, lcTrssum, vars);
        vars.addAll(tr.vars().getVars());
        Term tc = LcTrsInputReader.readTermFromStringWithEnv(c, lcTrssum, vars);
        vars.addAll(tc.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrssum, tl, tr, tc);

        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("simplify 0 1");
        eq.applyNewUserCommand("simplify 0 2");
    }

    @Test
    public void testReverseProof() throws ParserException, IOException {
        String l = "rev(rev(n))";
        String r = "n";
        String c = "TRUE";
        Term tl = LcTrsInputReader.readTermFromString(l, lcTrsreverse);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(tl.vars().getVars());
        Term tr = LcTrsInputReader.readTermFromStringWithEnv(r, lcTrsreverse, vars);
        vars.addAll(tr.vars().getVars());
        Term tc = LcTrsInputReader.readTermFromStringWithEnv(c, lcTrsreverse, vars);
        vars.addAll(tc.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrsreverse, tl, tr, tc);

        eq.applyNewUserCommand("simplify 1.0 2 [n:=cons(x, y)]");
        eq.applyNewUserCommand("postulate app(app(x, y), z) app(x, app(y, z)) [TRUE]");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("expand 1.0 terminating");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("simplify 0 3 [y:=nil]");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 3");
        eq.applyNewUserCommand("simplify 0 3");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("simplify 0 4");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 4");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 5");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify 1.0 4 [y:=cons(nil, nil)]");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify 1.0 3");
        eq.applyNewUserCommand("simplify 0 4");
        eq.applyNewUserCommand("simplify 2.0 3");
        eq.applyNewUserCommand("delete");

        eq.saveStateToFile("reverselist.prf");

        assertTrue(eq.getEquations().isEmpty());
    }
}
