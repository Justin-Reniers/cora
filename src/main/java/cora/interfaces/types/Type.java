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

package cora.interfaces.types;

import java.util.List;

/**
 * Types have the form σ1 → ,,, → σk → τ, with all σi Types and τ a BASE type.
 * For inductive reasoning, we split Types in two kinds: the BASE types, and ARROW types σ → τ;
 * here, σ1 → ... → σk → τ should be read in a right-associative way.
 *
 * Note: all instances of Type must (and can be expected to) be immutable.
 */
public interface Type {
  /** Returns true for base types, false for arrow types. */
  boolean isBaseType();
  /** Return false for base types, true for arrow types. */
  boolean isArrowType();
  
  /** Returns a string representation of the current type. */
  String toString();

  /** Returns whether the given Type is equal to us. */
  boolean equals(Type type);

  /** For σ1 → ,,, → σk → τ, returns k */
  int queryArity();
  /** For σ1 → ,,, → σk → τ, adds {σ1,,,σk} to the end of answer. */
  void appendInputTypes(List<Type> answer);
  /** For σ1 → ,,, → σk → τ, returns τ */
  BaseType queryOutputSort();
  /** For σ1 → ,,, → σk → τ, returns max(order(σ1),,,order(σk))+1 */
  int queryTypeOrder();

  /** Throws an InappropriatePatternDataError if called on anything but ARROWTYPE */
  Type queryArrowInputType();
  /** Throws an InappropriatePatternDataError if called on anything but ARROWTYPE */
  Type queryArrowOutputType();
}

