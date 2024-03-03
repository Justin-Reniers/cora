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

package cora.smt;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.TreeSet;

public class SmtProblemTest {
  @Test
  public void testToString() {
    // x > 1 ∨ x < 0
    // y = 3 ∧ (y != x ∨ z)
    // y = 9
    SmtProblem problem = new SmtProblem();
    IVar x = problem.createIntegerVariable(1);
    IVar y = problem.createIntegerVariable(2);
    BVar z = problem.createBooleanVariable(3);
    problem.require(SmtFactory.createDisjunction(SmtFactory.createGreater(x,
      SmtFactory.createValue(1)), SmtFactory.createSmaller(x, SmtFactory.createValue(0))));
    problem.require(SmtFactory.createConjunction(SmtFactory.createEqual(y,
      SmtFactory.createValue(3)), SmtFactory.createDisjunction(SmtFactory.createUnequal(y, x), z)));
    problem.require(SmtFactory.createEqual(y, SmtFactory.createValue(9)));
    assertTrue(problem.toString().equals(
      "(or (> i1 1) (> 0 i1))\n" +
      "(and (= i2 3) (or (distinct i2 i1) b3))\n" +
      "(= i2 9)\n"));
    assertTrue(problem.toString(1).equals(
      "(or (> i1 1) (> 0 i1))\n"));
    assertTrue(problem.toString(3).equals(problem.toString()));
    assertTrue(problem.toString(4).equals(problem.toString()));
    assertTrue(problem.toString(-2).equals(
      "(and (= i2 3) (or (distinct i2 i1) b3))\n" +
      "(= i2 9)\n"));
    assertTrue(problem.toString(-3).equals(problem.toString()));
    assertTrue(problem.toString(-4).equals(problem.toString()));
    assertTrue(problem.toString(0).equals(""));
  }

  @Test
  public void testCreateWithoutIndex() {
    SmtProblem problem = new SmtProblem();
    IVar a = problem.createIntegerVariable();
    IVar b = problem.createIntegerVariable();
    BVar c = problem.createBooleanVariable();
    IVar d = problem.createIntegerVariable(5);
    IVar e = problem.createIntegerVariable(-1);
    IVar f = problem.createIntegerVariable();
    BVar g = problem.createBooleanVariable();
    assertTrue(a.queryIndex() == 1);
    assertTrue(b.queryIndex() == 2);
    assertTrue(c.queryIndex() == 1);
    assertTrue(d.queryIndex() == 5);
    assertTrue(e.queryIndex() == -1);
    assertTrue(f.queryIndex() == 6);
    assertTrue(g.queryIndex() == 2);
  }
}

