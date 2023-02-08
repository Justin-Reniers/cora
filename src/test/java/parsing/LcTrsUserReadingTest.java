package parsing;

import cora.exceptions.AntlrParserException;
import cora.exceptions.ParserException;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import org.junit.Test;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    @Test
    public void readSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("Simplify 2.2");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionStartingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify .2");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionTrailingDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2.3.");
    }

    @Test (expected = AntlrParserException.class)
    public void readPositionDoubleDotSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("simplify 2..3");
    }

    @Test
    public void readExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd");
    }

    @Test
    public void readDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe");
    }

    @Test
    public void readPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate");
    }

    @Test
    public void readGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE");
    }

    @Test
    public void readGQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("gqdelEte");
    }

    @Test
    public void readConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR");
    }

    @Test
    public void readDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve");
    }

    @Test
    public void readCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness");
    }

    @Test
    public void readClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr");
    }

    @Test
    public void readSwap() throws ParserException {
        LcTrsInputReader.readUserInputFromString("SwAP");
    }
}
