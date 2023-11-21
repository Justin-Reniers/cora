/**************************************************************************************************
 Copyright 2023 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package cora.terms;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import cora.exceptions.*;
import cora.types.TypeFactory;

public class ValueTest extends TermTestFoundation {
  @Test
  public void testValueBasics() {
    Value v = new IntegerValue(-37);
    Value b = new BooleanValue(true);
    Value s = new StringValue("Hello\nworld");
    assertTrue(v.queryType().toString().equals("Int"));
    assertTrue(b.queryType().toString().equals("Bool"));
    assertTrue(s.queryType().toString().equals("String"));
    assertFalse(v.isVariable());
    assertTrue(b.isConstant());
    assertTrue(s.isFunctionalTerm());
    assertFalse(v.isVarTerm());
    assertFalse(b.isApplication());
    assertFalse(s.isAbstraction());
    assertFalse(v.isMetaApplication());
    assertFalse(b.isBetaRedex());
    assertTrue(s.isGround());
    assertTrue(v.isClosed());
    assertTrue(b.isTrueTerm());
    assertTrue(s.isTheoryTerm());
    assertTrue(v.isValue());
    assertTrue(b.numberArguments() == 0);
    assertTrue(s.numberMetaArguments() == 0);
    assertTrue(v.queryImmediateHeadSubterm(0) == v);
    assertTrue(b.queryHead() == b);
    assertTrue(s.queryRoot() == s);
    assertTrue(v.toValue() == v);
    assertTrue(b.isFirstOrder());
    assertTrue(s.isPattern());
    assertTrue(v.isApplicative());
    assertTrue(b.queryPositions().size() == 1);
    assertTrue(s.queryHeadPositions().size() == 1);
    assertTrue(v.vars().size() == 0);
    assertTrue(b.mvars().size() == 0);
    assertTrue(s.freeReplaceables().size() == 0);
    assertTrue(b.boundVars().size() == 0);
    assertTrue(v.replaceSubterm(new EmptyPosition(), new IntegerValue(20)).toString().equals("20"));
    assertTrue(s.querySubterm(new EmptyPosition()) == s);
    assertTrue(v.toString().equals("-37"));
    assertTrue(b.toString().equals("true"));
    assertTrue(s.toString().equals("\"Hello\\nworld\""));
    assertTrue(v.getUniqueNaming().size() == 0);
    assertTrue(b.refreshBinders() == b);
    assertTrue(v.apply(new ArrayList<Term>()) == v);
  }

  @Test(expected = ArityError.class)
  public void testValueApply() {
    Value v = new IntegerValue(13);
    v.apply(new Constant("a", baseType("o")));
  }

  @Test
  public void testEquality() {
    Value n = new IntegerValue(42);
    Value b = new BooleanValue(false);
    Value s = new StringValue("test");
    assertTrue(n.equals(n));
    assertTrue(n.equals(new IntegerValue(42)));
    assertFalse(n.equals(new IntegerValue(-42)));
    assertFalse(n.equals(b));
    assertTrue(b.equals(b));
    assertTrue(b.equals(new BooleanValue(false)));
    assertFalse(b.equals(new BooleanValue(true)));
    assertFalse(b.equals(s));
    FunctionSymbol f = new Constant("42", TypeFactory.intSort);
    Term bb = new Constant("false", TypeFactory.boolSort);
    Term ss = new Constant("\"test\"", TypeFactory.stringSort);
    assertFalse(n.equals(f));
    assertFalse(f.equals(n));
    assertFalse(b.equals(bb));
    assertFalse(bb.equals(b));
    assertFalse(s.equals(ss));
    assertFalse(ss.equals(s));
  }

  @Test
  public void testEscapedString() throws IncorrectStringException {
    StringValue v = StringValue.parseUserStringValue("\"this\\\\is\\nokay\\\"!\"");
    assertTrue(v.toUniqueString().equals("\"this\\\\is\\nokay\\\"!\""));
    assertTrue(v.getString().equals("this\\is\nokay\"!"));
  }

  @Test(expected = InappropriatePatternDataError.class)
  public void testVariableRequest() {
    Term v = new IntegerValue(-12);
    Term x = v.queryVariable();
  }

  @Test(expected = InappropriatePatternDataError.class)
  public void testAbstractionSubtermRequest() {
    Term v = new BooleanValue(true);
    v.queryAbstractionSubterm();
  }

  @Test(expected = IndexingError.class)
  public void testArgumentPositionRequest() {
    Term v = new StringValue("333");
    v.querySubterm(PositionFactory.createArg(1, PositionFactory.empty));
  }

  @Test(expected = IndexingError.class)
  public void testHeadPositionRequest() {
    Term v = new IntegerValue(31);
    v.querySubterm(new HeadPosition(PositionFactory.empty, 1));
  }

  @Test(expected = IndexingError.class)
  public void testBadPositionReplacement() {
    Term v = new BooleanValue(true);
    v.replaceSubterm(PositionFactory.createArg(1, PositionFactory.empty),
                     new Constant("a", baseType("a")));
  }
}