package parsing;

import cora.exceptions.*;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.Variable;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import cora.smt.EquivalenceProof;
import org.junit.Test;

import javax.swing.text.html.parser.Parser;
import java.util.TreeSet;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    private final static TRS lcTrs;
    private final static TreeSet<Variable> env = new TreeSet<>();
    private final static EquivalenceProof eq;

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

    static {
        eq = new EquivalenceProof(lcTrs, null, null, null);
    }

    @Test
    public void readSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("Simplify 2.2.0 1", eq);
    }

    @Test (expected = InvalidPositionException.class)
    public void readIncorrectSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simpliFY 2.2 1", eq);
    }

    @Test (expected = InvalidPositionException.class)
    public void readIncorrectPosition() throws ParserException {
        LcTrsInputReader.readUserInputFromString("expand 2.2", eq);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionStartingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify .2 1", eq);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionTrailingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3. 2", eq);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionDoubleDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2..3 3", eq);
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionNonNumericRuleIndex() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3 x", eq);
    }

    @Test
    public void testEmptyArgsSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify", eq);
    }

    @Test (expected = DeclarationException.class)
    public void testSingleAssignmentIncorrectArgSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := y]", eq);
    }

    @Test (expected = DeclarationException.class)
    public void testMultipleAssignmentIncorrectArgSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := y, z := d]", eq);
    }

    @Test
    public void testSingleAssignmentArgSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := j - 1]", eq);
    }

    @Test (expected = TypingException.class)
    public void testMultipleAssignmentArgSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := y - 1, z := d /\\ e]", eq);
    }

    @Test (expected = DeclarationException.class)
    public void testMultipleAssignmentOneIncorrectArgSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := y - 1, z := d]", eq);
    }

    @Test (expected = AntlrParserException.class)
    public void testMissingCommaAssignmentSimplify() throws ParserException {
        UserCommand uc = LcTrsInputReader.readUserInputFromString("simplify 2.3.0 2 [x := y := z]", eq);
    }

    @Test
    public void readExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd 1.1.0", eq);
    }

    @Test (expected = InvalidPositionException.class)
    public void readWrongExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("expand 1.1", eq);
    }

    @Test
    public void readDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe", eq);
    }

    @Test
    public void readPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) f(x+2) [x>=3]", eq);
    }

    @Test (expected = cora.exceptions.DeclarationException.class)
    public void readPostulateUndeclaredFunctions() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate f(x+1) g(x+2) [x>=3]", eq);
    }

    @Test
    public void readPostulateTheorySymbols() throws ParserException {
        LcTrsInputReader.readUserInputFromString("postulate (-2) (3+(2+1)) [x>=1]", eq);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE", eq);
    }

    @Test
    public void readEQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("eqdelEte", eq);
    }

    @Test
    public void readConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR", eq);
    }

    @Test
    public void readDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve", eq);
    }

    @Test
    public void readCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness", eq);
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr", eq);
    }

    @Test
    public void readSwap() throws ParserException {
        LcTrsInputReader.readUserInputFromString("SwAP", eq);
    }

    @Test
    public void readRewrite() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite [x > y] [x >= y]", eq);
    }

    @Test (expected = ParserException.class)
    public void readInvalidRewrite() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite x > y x >= y", eq);
    }

    @Test (expected = TypingException.class)
    public void readInvalidRewriteTyping() throws ParserException {
        LcTrsInputReader.readUserInputFromString("rewrite [3] [x>y]", eq);
    }
}
