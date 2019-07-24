/**************************************************************************************************
 Copyright 2019 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

import org.junit.Test;
import static org.junit.Assert.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.util.ArrayList;
import cora.parsers.CoraLexer;
import cora.parsers.CoraParser;
import cora.parsers.CoraParserBaseListener;
import cora.parsers.ErrorCollector;

public class CoraParserTest {
  private String toStringParseTree(ParseTree t) {
    if (t instanceof TerminalNode) {
      Token token = ((TerminalNode)t).getSymbol();
      String tokenname = CoraParser.VOCABULARY.getSymbolicName(token.getType());
      if (tokenname.equals("IDENTIFIER") || tokenname.equals("STRING"))
        return tokenname + "(" + t.getText() + ")";
      else return tokenname;
    }
    else if (t instanceof ParserRuleContext) {
      String ret = CoraParser.ruleNames[((ParserRuleContext)t).getRuleIndex()] + "(";
      for (int i = 0; i < t.getChildCount(); i++) {
        if (i != 0) ret += ",";
        ret += toStringParseTree(t.getChild(i));
      }
      return ret + ")";
    }
    else return "ERROR";
  }

  private CoraParser createParser(String str, ErrorCollector collector) {
    CoraLexer lexer = new CoraLexer(CharStreams.fromString(str));
    CoraParser parser = new CoraParser(new CommonTokenStream(lexer));
    if (collector != null) {
      parser.removeErrorListeners();
      parser.addErrorListener(collector);
    }
    return parser;
  }

  @Test
  public void testBaseType() {
    String str = "aKKaO";
    String expected = "onlytype(type(constant(IDENTIFIER(aKKaO))),EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testArrowType() {
    String str = "xx -> yy";
    String expected = "onlytype(type(lowarrowtype(constant(IDENTIFIER(xx))," +
                        "ARROW,type(constant(IDENTIFIER(yy))))),EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testRightAssociativeType() {
    String str = "xx -> yy -> zz";
    String expected = "onlytype(type(lowarrowtype(constant(IDENTIFIER(xx))," +
                        "ARROW,type(lowarrowtype(constant(IDENTIFIER(yy))," +
                        "ARROW,type(constant(IDENTIFIER(zz))))))),EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testHigherArrowType() {
    String str = "(xx -> yy) -> zz";
    String expected = "onlytype(type(higherarrowtype(BRACKETOPEN,type(" +
                        "lowarrowtype(constant(IDENTIFIER(xx)),ARROW,type(" +
                        "constant(IDENTIFIER(yy))))),BRACKETCLOSE,ARROW,type(" +
                        "constant(IDENTIFIER(zz))))),EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testUnnecessaryHigherType() {
    String str = "(a) -> b";
    String expected = "onlytype(type(higherarrowtype(BRACKETOPEN,type(" +
                        "constant(IDENTIFIER(a))),BRACKETCLOSE,ARROW,type(" +
                        "constant(IDENTIFIER(b))))),EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testCombinedType() {
    String str = "a -> (b -> cd) -> e";
    String expected = "onlytype(" +
                        "type(lowarrowtype(constant(IDENTIFIER(a)),ARROW," +
                          "type(higherarrowtype(BRACKETOPEN," +
                            "type(lowarrowtype(constant(IDENTIFIER(b)),ARROW," +
                            "type(constant(IDENTIFIER(cd)))))," +
                          "BRACKETCLOSE,ARROW,type(constant(IDENTIFIER(e)))))))," +
                        "EOF)";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 0);
    assertTrue(toStringParseTree(tree).equals(expected));
  }

  @Test
  public void testTypeMissingArrow() {
    String str = "a b";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 1);
  }

  @Test
  public void testTypeMissingInput() {
    String str = "-> b";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 1);
  }

  @Test
  public void testTypeMissingOutput() {
    String str = "b ->";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 1);
  }

  @Test
  public void testMultipleTypeErrors() {
    String str = "x -> ((a -> b c) ->) ->";
    ErrorCollector collector = new ErrorCollector();
    CoraParser parser = createParser(str, collector);
    ParseTree tree = parser.onlytype();
    assertTrue(collector.queryErrorCount() == 3);
  }
}

