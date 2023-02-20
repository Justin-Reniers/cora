package parsing;

import cora.loggers.ConsoleLogger;
import cora.loggers.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import java.util.Arrays;

import cora.parsers.LcTrsLexer;

public class LcTrsLexerTest {
    private static final ArrayList<String> forbiddenSymbols = new ArrayList<>(Arrays.asList(
            "~", "/\\", "\\/", "-->", "<-->", "-", "*", "/", "%", "+",
            "<", "<=", ">", ">=", "==", "!="));
    public static Logger l = new Logger(new ConsoleLogger());

    private ArrayList<String> warnings;

    private LcTrsLexer createLexer (String s) {
        return new LcTrsLexer(CharStreams.fromString(s));
    }

    private ArrayList<Token> tokenize(String s) {
        LcTrsLexer lexer = createLexer (s);
        ArrayList<Token> ret = new ArrayList<Token>();
        while(true) {
            Token t = lexer.nextToken();
            if (t.getType() == Token.EOF) break;
            ret.add(t);
        }
        //warnings = lexer.queryWarnings();
        return ret;
    }

    private void verifyToken(Token t, int kind, String text) {
        assertEquals(t.getType(), kind);
        assertEquals(t.getText(), text);
    }

    private void verifyError(int i, int line, int pos) {
        assertTrue(warnings.size() > i);
        String posdesc = "" + line + ":" + pos + ": ";
        assertEquals(warnings.get(i).substring(0, posdesc.length()), posdesc);
    }

    private void verifyError(int line, int pos) {
        verifyError(0, line, pos);
    }

    @Test
    public void testMinusArrowConditional() {
        ArrayList<Token> ts = tokenize("t --> -> - --> -->");
    }

    @Test
    public void testSymbolsInIdentifier() {
        ArrayList<Token> ts = tokenize("=a=b==ts+t==st>=ts<=s");
    }

    @Test
    public void testInvalidSymbolsInIdentifier() {
        ArrayList<Token> ts = tokenize("a()ts)a|a\\a]a[a],a<a>>=a=%a*a-a/a~a{a}");
    }

    @Test
    public void testArrowsInIdentifier() {
        ArrayList<Token> ts = tokenize("ts->t-->st<-->tss");
    }

    @Test
    public void testLexerSimpleIdentifier() {
         ArrayList<Token> ts = tokenize("function");
         assertEquals(ts.size(), 1);
         verifyToken(ts.get(0), LcTrsLexer.IDENTIFIER, "function");
    }

    @Test
    public void testIdentifierWithForbiddenSymbol() {
        ArrayList<Token> ts = tokenize("-ts");
        assertEquals(ts.size(), 2);
        verifyToken(ts.get(0), LcTrsLexer.MINUS, "-");
        verifyToken(ts.get(1), LcTrsLexer.IDENTIFIER, "ts");
    }

    @Test
    public void testLexerAllBasicTokens() {
        ArrayList<Token> ts = tokenize("a(o){,er 8}[eaf]");
        assertEquals(ts.size(), 12);
        assertEquals(ts.get(0).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(1).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(2).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(3).getType(), LcTrsLexer.BRACKETCLOSE);
        assertEquals(ts.get(4).getType(), LcTrsLexer.BRACEOPEN);
        assertEquals(ts.get(5).getType(), LcTrsLexer.COMMA);
        assertEquals(ts.get(6).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(7).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(8).getType(), LcTrsLexer.BRACECLOSE);
        assertEquals(ts.get(9).getType(), LcTrsLexer.SQUAREOPEN);
        assertEquals(ts.get(10).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(11).getType(), LcTrsLexer.SQUARECLOSE);
    }

    @Test
    public void testLexerArrow() {
        ArrayList<Token> ts = tokenize("-> - > x --> y");
        assertEquals(ts.size(), 6);
        assertEquals(ts.get(0).getType(), LcTrsLexer.ARROW);
        assertEquals(ts.get(1).getType(), LcTrsLexer.MINUS);
        assertEquals(ts.get(2).getType(), LcTrsLexer.GT);
        assertEquals(ts.get(3).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(4).getType(), LcTrsLexer.CONDITIONAL);
        assertEquals(ts.get(5).getType(), LcTrsLexer.IDENTIFIER);
    }

    @Test
    public void testLexerEquality() {
        ArrayList<Token> ts = tokenize("==");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.EQUALITY);
    }

    @Test
    public void testLexerNegation() {
        ArrayList<Token> ts = tokenize("~");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.NEGATION);
    }

    @Test
    public void testLexerConjunction() {
        ArrayList<Token> ts = tokenize("/\\");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.CONJUNCTION);
    }

    @Test
    public void testLexerDisjunction() {
         ArrayList<Token> ts = tokenize("\\/");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.DISJUNCTION);
    }

    @Test
    public void testLexerConditional() {
         ArrayList<Token> ts = tokenize("-->");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.CONDITIONAL);
    }

    @Test
    public void testLexerBiConditional() {
         ArrayList<Token> ts = tokenize("<-->");
         assertEquals(ts.size(), 1);
         assertEquals(ts.get(0).getType(), LcTrsLexer.BICONDITIONAL);
    }

    @Test
    public void testLexerMultiplication() {
        ArrayList<Token> ts = tokenize("*");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.MULT);
    }

    @Test
    public void testLexerDivision() {
        ArrayList<Token> ts = tokenize("/");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.DIV);
    }

    @Test
    public void testLexerModulo() {
        ArrayList<Token> ts = tokenize("%");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.MOD);
    }

    @Test
    public void testLexerPlus() {
        ArrayList<Token> ts = tokenize("+");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.PLUS);
    }

    @Test
    public void testLexerMinus() {
        ArrayList<Token> ts = tokenize("-");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.MINUS);
    }

    @Test
    public void testLexerLessThan() {
        ArrayList<Token> ts = tokenize("<");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.LT);
    }

    @Test
    public void testLexerLessThanOrEqual() {
        ArrayList<Token> ts = tokenize("<=");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.LTEQ);
    }

    @Test
    public void testLexerGreaterThan() {
        ArrayList<Token> ts = tokenize(">");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.GT);
    }

    @Test
    public void testLexerGreaterThanOrEqual() {
        ArrayList<Token> ts = tokenize(">=");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.GTEQ);
    }

    @Test
    public void testLexerNotEqual() {
        ArrayList<Token> ts = tokenize("!=");
        assertEquals(ts.size(), 1);
        assertEquals(ts.get(0).getType(), LcTrsLexer.NEQ);
    }

    @Test
    public void testKeywordSimplify() {
         ArrayList<Token> ts = tokenize("simplifY 2.2");
         assertEquals(ts.get(0).getType(), LcTrsLexer.SIMPLIFICATION);
         assertEquals(ts.get(1).getType(), LcTrsLexer.POS);
    }

    @Test
    public void testKeywordExpand() {
         ArrayList<Token> ts = tokenize("eXPANd");
         assertEquals(ts.get(0).getType(), LcTrsLexer.EXPANSION);
    }

    @Test
    public void testKeywordDelete() {
         ArrayList<Token> ts = tokenize("deletE");
         assertEquals(ts.get(0).getType(), LcTrsLexer.DELETION);
    }

    @Test
    public void testKeywordPostulate() {
         ArrayList<Token> ts = tokenize("postulate");
         assertEquals(ts.get(0).getType(), LcTrsLexer.POSTULATE);
    }

    @Test
    public void testKeywordGeneralize() {
         ArrayList<Token> ts = tokenize("GENERALIZE");
         assertEquals(ts.get(0).getType(), LcTrsLexer.GENERALIZATION);
    }

    @Test
    public void testKeywordGQDelete() {
         ArrayList<Token> ts = tokenize("gqdeleTe");
         assertEquals(ts.get(0).getType(), LcTrsLexer.GQDELETION);
    }

    @Test
    public void testKeywordConstructor() {
         ArrayList<Token> ts = tokenize("consTructor");
         assertEquals(ts.get(0).getType(), LcTrsLexer.CONSTRUCTOR);
    }

    @Test
    public void testKeywordDisprove() {
         ArrayList<Token> ts = tokenize("DisproVE");
         assertEquals(ts.get(0).getType(), LcTrsLexer.DISPROVE);
    }

    @Test
    public void testKeywordCompleteness() {
         ArrayList<Token> ts = tokenize("COMpleteneSS");
         assertEquals(ts.get(0).getType(), LcTrsLexer.COMPLETENESS);
    }

    @Test
    public void testKeywordClear() {
         ArrayList<Token> ts = tokenize("CleAr");
         assertEquals(ts.get(0).getType(), LcTrsLexer.CLEAR);
    }

    @Test
    public void testVarList() {
        ArrayList<Token> ts = tokenize("(VAR x y \n\t z)");
        assertEquals(6, ts.size());
        assertEquals(ts.get(0).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(1).getType(), LcTrsLexer.VARDECSTART);
        assertEquals(ts.get(2).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(3).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(4).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(5).getType(), LcTrsLexer.BRACKETCLOSE);
    }

    @Test
    public void testBasicLcTrs() {
        ArrayList<Token> ts = tokenize("(VAR x y)\n" +
                                          "(RULES\n" +
                                          "plus(x, 0) -> x \n)");
        assertEquals(ts.size(), 16);
        assertEquals(ts.get(0).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(1).getType(), LcTrsLexer.VARDECSTART);
        assertEquals(ts.get(2).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(3).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(4).getType(), LcTrsLexer.BRACKETCLOSE);
        assertEquals(ts.get(5).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(6).getType(), LcTrsLexer.RULEDECSTART);
        assertEquals(ts.get(7).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(8).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(9).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(10).getType(), LcTrsLexer.COMMA);
        assertEquals(ts.get(11).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(12).getType(), LcTrsLexer.BRACKETCLOSE);
        assertEquals(ts.get(13).getType(), LcTrsLexer.ARROW);
        assertEquals(ts.get(14).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(15).getType(), LcTrsLexer.BRACKETCLOSE);
    }

    @Test
    public void testBasicLcTrsWithLogicalConstraint() {
        ArrayList<Token> ts = tokenize("(VAR x y)\n" +
                                          "(RULES\n" +
                                          "plus(x, 0) -> x [a /\\ b]\n" +
                                          ")");
        assertEquals(ts.size(), 21);
        assertEquals(ts.get(0).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(1).getType(), LcTrsLexer.VARDECSTART);
        assertEquals(ts.get(2).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(3).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(4).getType(), LcTrsLexer.BRACKETCLOSE);
        assertEquals(ts.get(5).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(6).getType(), LcTrsLexer.RULEDECSTART);
        assertEquals(ts.get(7).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(8).getType(), LcTrsLexer.BRACKETOPEN);
        assertEquals(ts.get(9).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(10).getType(), LcTrsLexer.COMMA);
        assertEquals(ts.get(11).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(12).getType(), LcTrsLexer.BRACKETCLOSE);
        assertEquals(ts.get(13).getType(), LcTrsLexer.ARROW);
        assertEquals(ts.get(14).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(15).getType(), LcTrsLexer.SQUAREOPEN);
        assertEquals(ts.get(16).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(17).getType(), LcTrsLexer.CONJUNCTION);
        assertEquals(ts.get(18).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(19).getType(), LcTrsLexer.SQUARECLOSE);
        assertEquals(ts.get(20).getType(), LcTrsLexer.BRACKETCLOSE);
    }

    @Test
    public void testTokenizer() {
        String s = "[1 >= t-3/4]\n";
        ArrayList<Token> ts = tokenize(s);
        Logger.log(ts.toString());
    }
}
