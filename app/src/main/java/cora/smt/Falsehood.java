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

public class Falsehood extends Constraint {
  /** Private constructor, as Constraints should be made through the SmtFactory. */
  Falsehood() {}

  public boolean evaluate() { return false; }

  public void addToSmtString(StringBuilder builder) {
    builder.append("false");
  }

  public boolean equals(Constraint other) {
    return other instanceof Falsehood;
  }
}

