package smt;

import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import java.util.TreeSet;

public class MultipleCommandsSMTTest {
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
    public void testFactProof() throws ParserException {
        String l = "factiter(n)";
        String r = "factrec(n)";
        String c = "n>=1";
        Term tl = LcTrsInputReader.readTermFromString(l, lcTrs);
        TreeSet<Variable> vars = new TreeSet<>();
        vars.addAll(tl.vars().getVars());
        Term tr = LcTrsInputReader.readTermFromStringWithEnv(r, lcTrs, vars);
        vars.addAll(tr.vars().getVars());
        Term tc = LcTrsInputReader.readTermFromStringWithEnv(c, lcTrs, vars);
        vars.addAll(tc.vars().getVars());
        EquivalenceProof eq = new EquivalenceProof(lcTrs, tl, tr, tc);

        eq.applyNewUserCommand("simplify 0 1");
        eq.applyNewUserCommand("simplify 0 2");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0");
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
                "[n>=y/\\m==n-1/\\b==y+1/\\a==x*y]");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("expand 0");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 8");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("swap 1 2");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 2.0 3");
        eq.applyNewUserCommand("simplify 0 6");
        eq.applyNewUserCommand("eqdelete");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("simplify");
        eq.applyNewUserCommand("swap");
        eq.applyNewUserCommand("simplify 0 8");
        eq.applyNewUserCommand("delete");
        eq.applyNewUserCommand("completeness");
    }
}
