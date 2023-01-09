package parsing;

import org.junit.Test;
import static org.junit.Assert.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import cora.parsers.LcTrsLexer;

public class LcTrsLexerTest {
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
    public void testLexerSimpleIdentifier() {
         ArrayList<Token> ts = tokenize("function");
         assertEquals(ts.size(), 1);
         verifyToken(ts.get(0), LcTrsLexer.IDENTIFIER, "function");
    }

    @Test
    public void testLexerUnicodeIdentifier() {
         ArrayList<Token> ts = tokenize("½ɤ");
         assertEquals(ts.size(), 1);
         verifyToken(ts.get(0), LcTrsLexer.IDENTIFIER, "½ɤ");
    }

    @Test
    public void testLexerWhitespaceBetweenIdentifiers() {
         ArrayList<Token> ts = tokenize(" ½ɤ function a \tɵ");
         assertEquals(ts.size(), 4);
         verifyToken(ts.get(0), LcTrsLexer.IDENTIFIER, "½ɤ");
         verifyToken(ts.get(1), LcTrsLexer.IDENTIFIER, "function");
         verifyToken(ts.get(2), LcTrsLexer.IDENTIFIER, "a");
         verifyToken(ts.get(3), LcTrsLexer.IDENTIFIER, "ɵ");
    }

    @Test
    public void testLexerAllBasicTokens() {
        ArrayList<Token> ts = tokenize("½ɤa(o){,erɷ +_8}[eafɷ ]");
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
        assertEquals(ts.get(1).getType(), LcTrsLexer.IDENTIFIER);
        assertEquals(ts.get(2).getType(), LcTrsLexer.IDENTIFIER);
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
    public void testKeywordSimplify() {
         ArrayList<Token> ts = tokenize("simplifY");
         assertEquals(ts.get(0).getType(), LcTrsLexer.SIMPLIFICATION);
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
}
