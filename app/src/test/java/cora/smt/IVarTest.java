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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IVarTest {
  @Test
  public void testBasics() {
    IVar x = new IVar(12);
    assertTrue(x.toString().equals("i12"));
  }

  @Test
  public void testComparison() {
    IVar x = new IVar(12);
    assertTrue(x.compareTo(new IVar(12)) == 0);
    assertTrue(x.equals(new IVar(12)));
    assertTrue(x.compareTo(new IVar(13)) < 0);
    assertTrue(x.compareTo(new IVar(3)) > 0);
    // we don't test here against other kinds; this is handled in ConstantMultiplicationTest
  }
}
