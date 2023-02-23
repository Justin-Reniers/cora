package parsing;

import cora.exceptions.AntlrParserException;
import cora.exceptions.ParserException;
import cora.exceptions.UnsupportedRewritingRuleException;
import cora.interfaces.smt.UserCommand;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import org.junit.Test;

import javax.swing.text.html.parser.Parser;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    @Test
    public void readSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("Simplify 2.2 1");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionStartingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify .2 1");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionTrailingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3. 2");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionDoubleDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2..3 3");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionNonNumericRuleIndex() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3 x");
    }

    @Test
    public void testEmptyArgsSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readGQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("gqdelEte");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness");
    }

    @Test (expected = UnsupportedRewritingRuleException.class)
    public void readClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr");
    }

    @Test
    public void readSwap() throws ParserException {
        LcTrsInputReader.readUserInputFromString("SwAP");
    }
}
