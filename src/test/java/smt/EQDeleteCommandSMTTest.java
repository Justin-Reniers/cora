package smt;

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
        String nc = "[n ==i y /\\ m ==i n - 1 /\\ y_1 ==i y + 1 /\\ x_1 ==i x * y /\\ ~(x_1 ==i n*x)]";
        Term newC = LcTrsInputReader.readLogicalTermFromStringWithEnv(nc, lcTrs, eq.getEquationVariables());
        System.out.println(eq.getConstraint());
        System.out.println(newC);
        assertEquals(eq.getConstraint(), newC);
        //"n==iy/\\m==in+-1/\\y_1==iy+1/\\x_1==ix*y/\\~(x_1==in*x)"
        //"n==iy/\\m==in+-1/\\y_1==iy+1/\\x_1==ix*y/\\~(x_1==in*x)";
        //"n==iy/\\m==in+-1/\\y_1==iy+1/\\x_1==ix*y/\\~(x_1==in*x)";
    }

    @Test
    public void example1() throws ParserException {

    }
}
