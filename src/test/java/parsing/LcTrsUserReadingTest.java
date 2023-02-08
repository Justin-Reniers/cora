package parsing;

import cora.exceptions.ParserException;
import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import cora.parsers.LcTrsInputReader;
import org.junit.Test;

public class LcTrsUserReadingTest {

    public static Logger l = new Logger(new ConsoleLogger());

    @Test
    public void readUserInputSimplify() throws ParserException {
        LcTrsInputReader.readUserInputFromString("Simplify");
    }

    @Test
    public void readUserInputExpansion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("ExpAnd");
    }

    @Test
    public void readUserInputDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("deleTe");
    }

    @Test
    public void readUserInputPostulate() throws ParserException {
        LcTrsInputReader.readUserInputFromString("POSTUlate");
    }

    @Test
    public void readUserInputGeneralization() throws ParserException {
        LcTrsInputReader.readUserInputFromString("generaliZE");
    }

    @Test
    public void readUserInputGQDeletion() throws ParserException {
        LcTrsInputReader.readUserInputFromString("gqdelEte");
    }

    @Test
    public void readUserInputConstructor() throws ParserException {
        LcTrsInputReader.readUserInputFromString("conSTRUCTOR");
    }

    @Test
    public void readUserInputDisprove() throws ParserException {
        LcTrsInputReader.readUserInputFromString("DISProve");
    }

    @Test
    public void readUserInputCompleteness() throws ParserException {
        LcTrsInputReader.readUserInputFromString("completeness");
    }

    @Test
    public void readUserInputClear() throws ParserException {
        LcTrsInputReader.readUserInputFromString("CLeAr");
    }
}
