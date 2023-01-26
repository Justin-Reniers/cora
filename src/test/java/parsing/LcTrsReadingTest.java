import cora.parsers.TrsInputReader;
import org.antlr.v4.runtime.Parser;
import org.junit.Test;
import static org.junit.Assert.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cora.exceptions.ParserException;
import cora.exceptions.DeclarationException;
import cora.exceptions.TypingException;
import cora.interfaces.types.Type;
import cora.interfaces.terms.Variable;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Position;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.types.Sort;
import cora.types.ArrowType;
import cora.terms.Var;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.parsers.ErrorCollector;
import cora.parsers.LcTrsInputReader;
import cora.parsers.LcTrsParser;

public class LcTrsReadingTest {
    @Test
    public void testBasicSignature() throws ParserException{
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

        //assertEquals(lcTrs.lookupSymbol("-").queryType().toString(), "Int → Int");
        assertEquals(lcTrs.lookupSymbol("*").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("/").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("%").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("+").queryType().toString(), "Int → Int → Int");
        assertEquals(lcTrs.lookupSymbol("-").queryType().toString(), "Int → Int → Int");

        assertEquals(lcTrs.lookupSymbol("<").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("<=").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol(">").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol(">=").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("==").queryType().toString(), "Int → Int → Bool");
        assertEquals(lcTrs.lookupSymbol("!=").queryType().toString(), "Int → Int → Bool");
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
    public void readSimpleUnsortedLcTrs() throws ParserException {
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString("(VAR x y)\n" +
                                                            "(RULES\n" +
                                                            "plus(x, 0) -> x [a /\\ b]\n" +
                                                            "div(x, y) -> div(x, y) [y0]\n" +
                                                            ")");
        assertEquals(lcTrs.lookupSymbol("plus").queryType().toString(), ("o → o → o"));
        assertEquals(lcTrs.lookupSymbol("div").queryType().toString(), ("o → o → o"));
        assertNull(lcTrs.lookupSymbol("x"));
        assertNull(lcTrs.lookupSymbol("y"));
    }

    @Test
    public void readTermInLcTrs() throws ParserException {
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString("(VAR x y)\n" +
                                                            "(RULES\n" +
                                                            "plus(x, 0) -> x \n)");
        Term lt = LcTrsInputReader.readLogicalTermFromString("a /\\ b", lcTrs);
        assertEquals(lt.toString(), "a /\\ b");
    }

    @Test
    public void readRuleInLcTrs() throws ParserException {
        TRS lcTrs = LcTrsInputReader.readLcTrsFromString("(RULES\n" +
                                                            "plus(x, 0) -> x [a /\\ b]\n" +
                                                            ")");
        Term a = LcTrsInputReader.readTermFromString("plus(x, 0)", lcTrs);
        assertEquals(a.toString(), "plus(x, 0)");
    }

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