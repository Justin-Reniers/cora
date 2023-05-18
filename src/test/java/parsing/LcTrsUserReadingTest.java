package parsing;

import cora.exceptions.AntlrParserException;
import cora.exceptions.ParserException;
import cora.exceptions.UnsupportedRewritingRuleException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import org.junit.Test;

import javax.swing.text.html.parser.Parser;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

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
    public void readSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("Simplify 2.2 1", lcTrs);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionStartingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify .2 1", lcTrs);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionTrailingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3. 2", lcTrs);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionDoubleDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2..3 3", lcTrs);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionNonNumericRuleIndex() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3 x", lcTrs);
    }

    @Test
    public void testEmptyArgsSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify", lcTrs);
    }

    @Test
    public void readExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd 1.1", lcTrs);
    }

    @Test
    public void readDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe", lcTrs);
    }

    @Test
    public void readPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) f(x+2) [x>=3]", lcTrs);
    }

    @Test //(expected = cora.exceptions.DeclarationException.class)
    public void readPostulateUndeclaredFunctions() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) g(x+2) [x>=3]", lcTrs);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE", lcTrs);
    }

    @Test
    public void readEQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("eqdelEte", lcTrs);
    }

    @Test
    public void readConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR", lcTrs);
    }

    @Test
    public void readDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve", lcTrs);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness", lcTrs);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr", lcTrs);
    }

    @Test
    public void readSwap() throws ParserException {
        LcTrsInputReader.readUserInputFromString("SwAP", lcTrs);
    }
}
