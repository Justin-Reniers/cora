package parsing;

import cora.loggers.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.antlr.v4.runtime.tree.ParseTree;

import cora.exceptions.ParserException;
import cora.exceptions.TypingException;
import cora.interfaces.types.Type;
import cora.interfaces.rewriting.TRS;
import cora.parsers.ErrorCollector;
import cora.parsers.LcTrsInputReader;
import cora.parsers.LcTrsParser;
import cora.loggers.ConsoleLogger;

public class LcTrsReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    @Test
    public void testBasicSignatureAndStandardSortsAndFunctions() throws ParserException{
        String str = "(VAR x ys xs)\n" +
                "(SIG (nil 0) (cons 2) (append 2) (0 0) (s 1))\n" +
                "(RULES\n" +
                "  append(nil, ys) -> ys\n" +
                "  append(cons(x, xs), ys) -> cons(x, append(xs, ys))\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(str);

        assertEquals(lcTrs.lookupSymbol("~").queryType().toString(), "Bool → Bool");
        assertEquals(lcTrs.lookupSymbol("/\\").queryType().toString(), "Bool → Bool → Bool");
        assertEquals(lcTrs.lookupSymbol("\\/").queryType().toString(), "Bool → Bool → Bool");
        assertEquals(lcTrs.lookupSymbol("-->").queryType().toString(), "Bool → Bool → Bool");
        assertEquals(lcTrs.lookupSymbol("<-->").queryType().toString(), "Bool → Bool → Bool");

        assertEquals(lcTrs.lookupSymbol("-").queryType().toString(), "Int → Int");
        assertEquals(lcTrs.lookupSymbol("*").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("/").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("%").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("+").queryType().toString(), "Int → Int → Int");

        assertEquals(lcTrs.lookupSymbol("<").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("<=").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol(">").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol(">=").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("==").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("!=").queryType().toString(), "Int → Int → Bool");
    }

    @Test
    public void testBasicSignature() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool)\n" +
                "(and   Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [True]\n" +
                "and(z, x) -> and(x /\\ z, z)\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test(expected = TypingException.class)
    public void testBasicSignatureIncorrectTyping() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testMinusOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t-2]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testMinusOperator2() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t+-3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testMinusOperator3() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t+-3]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t-3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testMultOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-3*4]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t-3*4]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testWrongTypingMultOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-3*4]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == t-3*4]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testDivOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-3/4]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t-3/4]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingDivOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-y/4]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t-3/x]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testModOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-3%4]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t-3%4]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingModOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+-3%x]\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t-3%4]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testPlusOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+3+4+a]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingPlusOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= t+x+4+a]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testEqualityOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 == 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingEqualityOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [z == 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testInequalityOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 != 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test(expected = TypingException.class)
    public void testWrongTypingInequalityOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [x != 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test(expected = TypingException.class)
    public void testWrongTypingInequalityOperator2() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [x != y]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testGTOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 > 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingGTOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 > x]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testLTOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 < 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingLTOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 < x]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testGTEQOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingGTEQOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 >= x]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testLTEQOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 <= 1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testWrongTypingLTEQOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1 <= x]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test (expected = TypingException.class)
    public void testConstraintType() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [1]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testReadArityInTypeOrArity() throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsInputReader reader = new LcTrsInputReader();
        LcTrsParser parser = LcTrsInputReader.createLcTrsParserFromString("3", collector);
        ParseTree tree = parser.typeorarity();
        collector.throwCollectedExceptions();

        Type type = reader.readTypeOrArity(tree);
        assertEquals(type.toString(), "o → o → o → o");
    }

    @Test
    public void testNegationOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "or(z, or(x, y)) -> z /\\ y \\/ x [~x /\\ y]\n" +
                "~~x -> x\n" +
                "~~~x -> ~x\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testConjunctionOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "x /\\ y /\\ z-> y /\\ x /\\ z [-1 == -3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testDisjunctionOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "x /\\ y \\/ z -> z /\\ y \\/ x [-1 == -3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testConditionalOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "x --> y --> z -> y --> z  [-1 == -3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }

    @Test
    public void testBiconditionalOperator() throws ParserException {
        String s = "(VAR x y z)\n" +
                "(SIG\n" +
                "(or    Bool Bool -> Bool))\n" +
                "(RULES\n" +
                "x <--> y <--> z -> y <--> x <--> z [-1 == -3]\n" +
                ")";
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString(s);
    }
}
