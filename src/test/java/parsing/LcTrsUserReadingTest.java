package parsing;

import cora.exceptions.*;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Variable;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import org.junit.Test;

import javax.swing.text.html.parser.Parser;
import java.util.TreeSet;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    private final static TRS lcTrs;
    private final static TreeSet<Variable> env = new TreeSet<>();

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
        LcTrsInputReader.readUserInputFromString("Simplify 0.2.2 1", lcTrs, env);
    }

    @Test (expected = InvalidPositionException.class)
    public void readIncorrectSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simpliFY 2.2 1", lcTrs, env);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionStartingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify .2 1", lcTrs, env);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionTrailingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3. 2", lcTrs, env);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionDoubleDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2..3 3", lcTrs, env);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionNonNumericRuleIndex() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3 x", lcTrs, env);
    }

    @Test
    public void testEmptyArgsSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify", lcTrs, env);
    }

    @Test
    public void readExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd 0.1", lcTrs, env);
    }

    @Test (expected = InvalidPositionException.class)
    public void readWrongExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("expand 1.1", lcTrs, env);
    }

    @Test
    public void readDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe", lcTrs, env);
    }

    @Test
    public void readPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) f(x+2) [x>=3]", lcTrs, env);
    }

    @Test (expected = cora.exceptions.DeclarationException.class)
    public void readPostulateUndeclaredFunctions() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) g(x+2) [x>=3]", lcTrs, env);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE", lcTrs, env);
    }

    @Test
    public void readEQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("eqdelEte", lcTrs, env);
    }

    @Test
    public void readConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR", lcTrs, env);
    }

    @Test
    public void readDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve", lcTrs, env);
    }

    @Test
    public void readCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness", lcTrs, env);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr", lcTrs, env);
    }

    @Test
    public void readSwap() throws ParserException {
        LcTrsInputReader.readUserInputFromString("SwAP", lcTrs, env);
    }

    @Test
    public void readRewrite() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite [x > y] [x >= y]", lcTrs, env);
    }

    @Test (expected = ParserException.class)
    public void readInvalidRewrite() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite x > y x >= y", lcTrs, env);
    }

    @Test (expected = TypingException.class)
    public void readInvalidRewriteTyping() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite [3] [x>y]", lcTrs, env);
    }
}
