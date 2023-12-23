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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

import cora.exceptions.*;
import cora.types.Type;
import cora.types.TypeFactory;

public class MetaApplicationTestOld extends TermTestFoundation {
  @Test(expected = NullInitialisationError.class)
  public void testUnaryWithNullArg() {
    MetaVariable z = TermFactory.createMetaVar("z", baseType("a"), baseType("b"));
    Term arg = null;
    Term t = TermFactory.createMeta(z, arg);
  }

  @Test(expected = NullInitialisationError.class)
  public void testBinaryWithNullHead() {
    TermFactory.createMeta(null, constantTerm("a", baseType("b")),
                                 constantTerm("a", baseType("c")));
  }

  @Test(expected = NullInitialisationError.class)
  public void testNullArgs() {
    MetaVariable z = TermFactory.createMetaVar("z", baseType("a"), baseType("b"));
    ArrayList<Term> args = null;
    TermFactory.createMeta(z, args);
  }

  @Test(expected = ArityError.class)
  public void testNotEnoughArgs() {
    ArrayList<Type> inputs = new ArrayList<Type>();
    inputs.add(baseType("a"));
    inputs.add(baseType("a"));
    inputs.add(baseType("a"));
    MetaVariable z = TermFactory.createMetaVar("z", inputs, baseType("a"));
    Term s = constantTerm("s", baseType("a"));
    TermFactory.createMeta(z, s, s);
  }

  @Test(expected = ArityError.class)
  public void testTooManyArgs() {
    ArrayList<Type> inputs = new ArrayList<Type>();
    inputs.add(baseType("a"));
    inputs.add(baseType("a"));
    MetaVariable z = TermFactory.createMetaVar("z", inputs, arrowType("a", "a"));
    ArrayList<Term> args = new ArrayList<Term>();
    args.add(constantTerm("a", baseType("a")));
    args.add(constantTerm("a", baseType("a")));
    args.add(constantTerm("a", baseType("a")));
    TermFactory.createMeta(z, args);
  }

  @Test(expected = IllegalTermError.class)
  public void testNoArgs() {
    MetaVariable z = new Var("z", arrowType("a", "a"));
    ArrayList<Term> args = new ArrayList<Term>();
    new MetaApplication(z, args);
  }

  @Test(expected = TypingError.class)
  public void testBadArgumentType() {
    ArrayList<Type> inputs = new ArrayList<Type>();
    inputs.add(baseType("a"));
    inputs.add(baseType("a"));
    MetaVariable z = TermFactory.createMetaVar("z", inputs, arrowType("a", "a"));
    ArrayList<Term> args = new ArrayList<Term>();
    args.add(constantTerm("a", baseType("a")));
    args.add(constantTerm("b", baseType("b")));
    TermFactory.createMeta(z, args);
  }

  private Term makeMeta(String name, Term arg, Type output) {
    MetaVariable z = TermFactory.createMetaVar(name, arg.queryType(), output);
    return TermFactory.createMeta(z, arg);
  }

  private Term makeMeta(String name, Term arg1, Term arg2, Type output) {
    MetaVariable z = TermFactory.createMetaVar(name, arg1.queryType(), arg2.queryType(), output);
    return TermFactory.createMeta(z, arg1, arg2);
  }

  private Term makeSample() {
    // create: Z[f(x), λy.f(y)]
    Variable x = TermFactory.createVar("x", baseType("A"));
    Variable y = TermFactory.createBinder("y", baseType("A"));
    Term arg1 = unaryTerm("f", baseType("B"), x);
    Term arg2 = TermFactory.createAbstraction(y, unaryTerm("f", baseType("B"), y));
    return makeMeta("Z", arg1, arg2, baseType("A"));
  }

  @Test(expected = IndexingError.class)
  public void testImmediateHeadSubterm() {
    Term t = makeSample();
    t.queryImmediateHeadSubterm(1);
  }

  @Test(expected = IndexingError.class)
  public void testArgument() {
    Term t = makeSample();
    t.queryArgument(1);
  }

  @Test(expected = InappropriatePatternDataError.class)
  public void testRoot() {
    Term t = makeSample();
    t.queryRoot();
  }

  @Test(expected = InappropriatePatternDataError.class)
  public void testVariable() {
    Term t = makeSample();
    t.queryVariable();
  }

  @Test
  public void testAppliedMetaApp() {
    Variable x = TermFactory.createBinder("x", baseType("o"));
    Variable y = TermFactory.createBinder("x", baseType("a"));
    Variable z = TermFactory.createBinder("z", baseType("a"));
    Type output = arrowType(baseType("a"), arrowType("a", "b"));
    MetaVariable m = TermFactory.createMetaVar("Z", baseType("o"), output);
    Term zx = TermFactory.createMeta(m, x);
    Term total = TermFactory.createApp(zx, y, z);
    assertTrue(total.toString().equals("Z⟨x__1⟩(x__2, z)"));
  }

  @Test
  public void testAlphaEquality() {
    // M[λx.x, λx.f(x), u]
    Variable x = TermFactory.createBinder("x", baseType("o"));
    Variable y = TermFactory.createBinder("y", baseType("o"));
    Variable z = TermFactory.createBinder("z", baseType("o"));
    Variable u = TermFactory.createBinder("u", baseType("o"));
    Type o = baseType("o");
    Term fx = unaryTerm("f", o, x);
    ArrayList<Type> inputs = new ArrayList<Type>();
    inputs.add(arrowType("o", "o"));
    inputs.add(arrowType("o", "o"));
    inputs.add(baseType("o"));
    MetaVariable m = TermFactory.createMetaVar("M", inputs, baseType("o"));
    ArrayList<Term> args1 = new ArrayList<Term>();
    args1.add(TermFactory.createAbstraction(x, x));
    args1.add(TermFactory.createAbstraction(x, unaryTerm("f", o, x)));
    args1.add(u);
    Term s = TermFactory.createMeta(m, args1);
    ArrayList<Term> args2 = new ArrayList<Term>();
    args2.add(TermFactory.createAbstraction(y, y));
    args2.add(TermFactory.createAbstraction(z, unaryTerm("f", o, z)));
    args2.add(u);
    Term t = TermFactory.createMeta(m, args1);
    assertTrue(s.equals(t));
    assertTrue(t.equals(s));
  }

  @Test
  public void testInequalityDifferentMetaVariables() {
    // M⟨x⟩
    Variable x = TermFactory.createBinder("x", baseType("o"));
    MetaVariable m1 = TermFactory.createMetaVar("M", arrowType("o", "o"), 1);
    MetaVariable m2 = TermFactory.createMetaVar("M", arrowType("o", "o"), 1);
    Term term1 = TermFactory.createMeta(m1, x);
    Term term2 = TermFactory.createMeta(m2, x);
    assertFalse(term1.equals(term2));
  }

  @Test
  public void testInequalityDifferentArguments() {
    // M⟨x⟩
    Variable x1 = TermFactory.createBinder("x", baseType("o"));
    Variable x2 = TermFactory.createBinder("x", baseType("o"));
    MetaVariable m = TermFactory.createMetaVar("M", arrowType("o", "o"), 1);
    Term term1 = TermFactory.createMeta(m, x1);
    Term term2 = TermFactory.createMeta(m, x2);
    assertFalse(term1.equals(term2));
  }

  @Test
  public void testSubstituteCorrectly() {
    // Z⟨g(x), c⟩ [x:=0, Z := λy,w.f(w, y)]
    Type type = arrowType(baseType("a"), arrowType(baseType("b"), arrowType("a", "b")));
    MetaVariable z = TermFactory.createMetaVar("Z", type, 2);
    Variable x = new Var("x", baseType("b"));
    Term arg1 = unaryTerm("g", baseType("a"), x);
    Term arg2 = constantTerm("c", baseType("b"));
    Term term = TermFactory.createMeta(z, arg1, arg2);

    Substitution gamma = new Subst();
    gamma.extend(x, constantTerm("0", baseType("b")));
    Variable y = new Binder("y", baseType("a"));
    Variable w = new Binder("w", baseType("b"));
    Term f = constantTerm("f",
      arrowType(baseType("b"), arrowType(baseType("a"), arrowType("a", "b"))));
    gamma.extend(z, TermFactory.createAbstraction(y, TermFactory.createAbstraction(w,
      TermFactory.createApp(f, w, y))));

    Term result = term.substitute(gamma);
    assertTrue(result.toString().equals("f(c, g(0))"));
  }

  @Test
  public void testDifficultSubstitution() {
    // Z⟨λx.a(x,y),F⟩
    Variable x = new Binder("x", baseType("A"));
    Variable y = new Binder("y", baseType("B"));
    Term a = constantTerm("a", arrowType(baseType("A"), arrowType("B", "A")));
    Term abs = new Abstraction(x, TermFactory.createApp(a, x, y));
    Variable f = new Binder("F", arrowType("A", "A"));
    MetaVariable z = TermFactory.createMetaVar("Z", arrowType(abs.queryType(),
      arrowType(f.queryType(), baseType("A"))), 2);
    Term term = TermFactory.createMeta(z, abs, f);
    // [x:=0, y:=1, F:=λz.h(z, x), Z := λF, G.F(G(0))]
    Substitution gamma = new Subst();
    gamma.extend(x, constantTerm("0", baseType("A")));
    gamma.extend(y, constantTerm("1", baseType("B")));
    Variable z2 = new Binder("z", baseType("A"));
    Term h = constantTerm("h", arrowType(baseType("A"), arrowType("A", "A")));
    Term abs1 = new Abstraction(z2, TermFactory.createApp(h, z2, x));
    gamma.extend(f, abs1);
    Variable g = new Binder("G", arrowType("A", "A"));
    Term zero = constantTerm("0", baseType("A"));
    Term abs2 = new Abstraction(f, new Abstraction(g, f.apply(g.apply(zero))));
    gamma.extend(z, abs2);
    // result of substituting: [λF, G.F(G(0))]⟨λx.a(x,1), λz.h(z, x)⟩⟩ = (λx.a(x,1))(λz.h(z, x)(0))
    // (note that it isn't normalised beyond that)
    assertTrue(term.substitute(gamma).toString().equals("(λx1.a(x1, 1))((λz.h(z, x))(0))"));
  }

  @Test
  public void testRenaming() {
    // Z⟨λx.a(x,y),F⟩ -- except all variables and meta-variables are called "v"
    Variable x = new Binder("x", baseType("A"));
    Variable f = new Binder("x", arrowType("A", "A"));
    Variable y = new Binder("x", baseType("B"));
    Term a = constantTerm("a", arrowType(baseType("A"), arrowType("B", "A")));
    Term abs = new Abstraction(x, TermFactory.createApp(a, x, y));
    MetaVariable z = TermFactory.createMetaVar("x", arrowType(abs.queryType(),
      arrowType(f.queryType(), baseType("A"))), 2);
    Term term = TermFactory.createMeta(z, abs, f);
    assertTrue(term.toString().equals("x__1⟨λx1.a(x1, x__3), x__2⟩"));
  }

  @Test
  public void testReplaceables() {
    // let's create: Z⟨Z⟨x,h(λz.c(z))⟩,g(y,x)⟩, where x and y are variables
    MetaVariable z = TermFactory.createMetaVar("Z", arrowType(baseType("a"),arrowType("b","a")), 2);
    FunctionSymbol g = new Constant("g", arrowType(baseType("b"),arrowType("a","b")));
    FunctionSymbol c = new Constant("c", arrowType("o", "o"));
    FunctionSymbol h = new Constant("h", arrowType(arrowType("o", "o"), baseType("b")));
    Variable x = new Binder("x", baseType("a"));
    Variable y = new Var("y", baseType("b"));
    Variable z2 = new Binder("z", baseType("o"));
    Term hlambdazcz = new Application(h, new Abstraction(z2, c.apply(z2)));
    Term t = TermFactory.createMeta(z, x, hlambdazcz);
    Term s = TermFactory.createMeta(z, t, new Application(g, y, x));
    ReplaceableList lst = s.freeReplaceables();
    assertTrue(lst.contains(x));
    assertTrue(lst.contains(y));
    assertTrue(lst.contains(z));

    Environment<Variable> env = s.vars();
    assertTrue(env.contains(x));
    assertTrue(env.contains(y));
    assertTrue(env.size() == 2);

    Environment<MetaVariable> menv = s.mvars();
    assertTrue(menv.contains(z));
    assertTrue(menv.size() == 2);
    assertTrue(t.mvars().size() == 1);
    assertTrue(hlambdazcz.mvars().size() == 0);
  }

  @Test
  public void testReplaceablesReuse() {
    // let's create: Z⟨g(x), Z⟨x,y⟩⟩
    Variable x = new Var("x", baseType("o"));
    Variable y = new Var("x", baseType("o"));
    Term gx = unaryTerm("g", baseType("o"), x);
    MetaVariable z =
      TermFactory.createMetaVar("Z", arrowType(baseType("o"), arrowType("o", "o")), 2);
    Term zxy = TermFactory.createMeta(z, x, y);
    Term term = TermFactory.createMeta(z, gx, zxy);
    assertTrue(gx.freeReplaceables() == x.freeReplaceables());
    assertTrue(zxy.freeReplaceables() == term.freeReplaceables());
    assertTrue(term.freeReplaceables().size() == 3);
  }

  @Test
  public void testBoundVars() {
    // let's create: F⟨λx.Z(x), Y, G⟨λz.z, λx,u.h(x,y)⟩⟩
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    Variable u = new Binder("u", baseType("o"));
    Variable bZ = new Var("Z", arrowType("o", "o"));
    Variable bY = new Var("Y", baseType("o"));
    FunctionSymbol h = new Constant("h", arrowType(baseType("o"), arrowType("o", "o")));
    MetaVariable g = TermFactory.createMetaVar("G", arrowType(arrowType("o", "o"),
      arrowType(arrowType(baseType("o"), arrowType("o", "o")), baseType("o"))), 2);
    MetaVariable f = TermFactory.createMetaVar("F",
      arrowType(arrowType("o", "o"), arrowType(baseType("o"), arrowType("o", "o"))), 3);
    Term ahxy = new Abstraction(x, new Abstraction(u, new Application(h, x, y)));
    Term az = new Abstraction(z, z);
    Term gterm = TermFactory.createMeta(g, az, ahxy);
    Term aZx = new Abstraction(x, new Application(bZ, x));
    ArrayList<Term> args = new ArrayList<Term>();
    args.add(aZx);
    args.add(bY);
    args.add(gterm);
    Term fterm = new MetaApplication(f, args);

    ReplaceableList frees = fterm.freeReplaceables();
    ReplaceableList boundVars = fterm.boundVars();
    assertTrue(frees.size() == 5);
    assertTrue(boundVars.size() == 3);
    assertTrue(boundVars.contains(x));
    assertTrue(boundVars.contains(z));
    assertTrue(boundVars.contains(u));
    assertTrue(frees.contains(y));
    assertTrue(frees.contains(bY));
    assertTrue(frees.contains(bZ));
    assertTrue(frees.contains(f));
    assertTrue(frees.contains(g));
  }

  @Test
  public void testBoundVarsReuse() {
    // let's create: Z⟨λx.x, λx.x⟩
    Variable x = new Binder("x", baseType("o"));
    MetaVariable z = TermFactory.createMetaVar("Z", arrowType(arrowType("o", "o"),
      arrowType(arrowType("o", "o"), baseType("o"))), 2);
    Term abs1 = new Abstraction(x, x);
    Term abs2 = new Abstraction(x, x);
    Term term = TermFactory.createMeta(z, abs1, abs2);
    assertTrue(term.boundVars() == abs1.boundVars());
  }

  @Test
  public void testCorrectApplication() {
    Type o = baseType("o");
    Type a = baseType("a");
    Type type = arrowType(a, arrowType(o, a));
    MetaVariable x = TermFactory.createMetaVar("X", type, 1);
    Term c = constantTerm("c", arrowType(a, a));
    Term d = constantTerm("d", a);
    Term xc = TermFactory.createMeta(x, c.apply(d));
    Term xcb = xc.apply(constantTerm("b", o));
    assertTrue(xcb.toString().equals("X⟨c(d)⟩(b)"));
  }

  @Test
  public void testRefreshBinders() {
    // Z⟨λx.x, λx.x⟩
    Variable x = new Binder("x", baseType("o"));
    Term xx = new Abstraction(x, x);
    MetaVariable z = TermFactory.createMetaVar("Z", arrowType(arrowType("o", "o"),
      arrowType(arrowType("o", "o"), baseType("a"))), 2);
    Term term = TermFactory.createMeta(z, xx, xx);
    Term t = term.refreshBinders();
    assertTrue(t.equals(term));
    assertTrue(t.queryMetaVariable() == z);
    Variable x1 = t.queryMetaArgument(1).queryVariable();
    Variable x2 = t.queryMetaArgument(2).queryVariable();
    assertTrue(x1 != x2);
  }

  @Test
  public void testWellbehaved() {
    // Z⟨λx.x, x⟩
    Variable x = new Binder("x", baseType("a"));
    Term abs = new Abstraction(x, x);
    MetaVariable z = TermFactory.createMetaVar("Z", arrowType(arrowType("a", "a"),
      arrowType("a", "a")), 2);
    Term term = TermFactory.createMeta(z, abs, x);
    assertTrue(term.toString().equals("Z⟨λx1.x1, x⟩"));
    assertFalse(term.queryMetaArgument(1).queryVariable().equals(
                  term.queryMetaArgument(2).queryVariable()));
  }

  @Test(expected = NullCallError.class)
  public void testNullMatch() {
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    MetaVariable z =
      TermFactory.createMetaVar("Z", arrowType(baseType("o"), arrowType("o", "o")), 2);
    Term t = TermFactory.createMeta(z, x, y);
    Substitution subst = new Subst();
    t.match(null, subst);
  }

  @Test(expected = NullCallError.class)
  public void testNullSubst() {
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    MetaVariable z =
      TermFactory.createMetaVar("Z", arrowType(baseType("o"), arrowType("o", "o")), 2);
    Term t = TermFactory.createMeta(z, x, y);
    Substitution subst = new Subst();
    t.match(x, null);
  }

  private Term createTwoArgMeta(Term arg1, Term arg2) {
    Type type = arrowType(arg1.queryType(), arrowType(arg2.queryType(), baseType("o")));
    MetaVariable f = TermFactory.createMetaVar("F", type, 2);
    return TermFactory.createMeta(f, arg1, arg2);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToNonVariableArg() {
    // F⟨a⟩ matched against a
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term a = constantTerm("a", baseType("o"));
    Term t = TermFactory.createMeta(f, a);
    Substitution subst = new Subst();
    t.match(a, subst);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToVarArg() {
    // F⟨X⟩ matched against a
    Variable x = new Var("X", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term t = TermFactory.createMeta(f, x);
    Term a = constantTerm("a", baseType("o"));
    t.match(a, new Subst());
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToSubstitutedVarArg() {
    // F⟨X⟩ matched against a, with X:=y
    Variable x = new Var("X", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term t = TermFactory.createMeta(f, x);
    Term a = constantTerm("a", baseType("o"));
    Substitution subst = new Subst();
    subst.extend(x, y);
    t.match(a, subst);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToBinderNotSubstituted() {
    // F⟨x,y⟩ matched against a where x:=z, but y is not substituted
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("Z", baseType("o"));
    Term t = createTwoArgMeta(x, y);
    Substitution subst = new Subst();
    subst.extend(x, z);
    t.match(constantTerm("a", baseType("o")), subst);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToBinderArgSubstitutedToVar() {
    // F⟨x,y⟩ matched against a where x:=Z,y:=y
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Var("Z", baseType("o"));
    Term t = createTwoArgMeta(x, y);
    Substitution subst = new Subst();
    subst.extend(x, z);
    subst.extend(y, z);
    t.match(constantTerm("a", baseType("o")), subst);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToDuplicateBinder() {
    // F⟨x,x⟩ matched against x
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Term t = createTwoArgMeta(x, x);
    t.match(x, new Subst());
    Substitution subst = new Subst();
    subst.extend(x, y);
    t.match(constantTerm("a", baseType("o")), subst);
  }

  @Test(expected = PatternRequiredError.class)
  public void testNonPatternDueToNonDistinctSubstitutedArgs() {
    // F⟨x,y⟩ matched against a where x:=z and y:=z
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    Term t = createTwoArgMeta(x, y);
    Substitution subst = new Subst();
    subst.extend(x, z);
    subst.extend(y, z);
    t.match(constantTerm("a", baseType("o")), subst);
  }

  @Test
  public void testProperMatching() {
    // F⟨x,y⟩ matched against h(y, x)
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    Term t = createTwoArgMeta(x, y);
    Substitution subst = new Subst();
    subst.extend(x, x);
    subst.extend(y, y);
    Term result =
      TermFactory.createApp(constantTerm("h", arrowType(baseType("o"), arrowType("o", "o"))), y, x);
    assertTrue(t.match(result, subst) == null);
    assertTrue(subst.get(x) == x);
    assertTrue(subst.get(y) == y);
    assertTrue(subst.get(t.queryMetaVariable()).toString().equals("λx.λy.h(y, x)"));
  }

  @Test
  public void testProperMatchingWithSwitchedVariables() {
    // F⟨x,y⟩ matched against h(y, x) where x:=y and y:=x
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    Term t = createTwoArgMeta(x, y);
    Substitution subst = new Subst();
    subst.extend(x, y);
    subst.extend(y, x);
    Term result =
      TermFactory.createApp(constantTerm("h", arrowType(baseType("o"), arrowType("o", "o"))), y, x);
    assertTrue(t.match(result, subst) == null);
    assertTrue(subst.get(t.queryMetaVariable()).toString().equals("λy.λx.h(y, x)"));
  }

  @Test
  public void testLateAssignmentToArgument() {
    // g(F⟨x⟩, x) against g(x, y)
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("x", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term term = new Application(g, TermFactory.createMeta(f, x), x);

    Term m = new Application(g, x, y);
    Substitution subst = new Subst();
    assertTrue(term.match(m, subst) == null);
    assertTrue(subst.get(x) == y);
    assertTrue(subst.get(f).equals(new Abstraction(y, x)));
  }

  @Test(expected = PatternRequiredError.class)
  public void testTooLateAssignmentToArgument() {
    // g(x, F⟨x⟩) against g(y, x)
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("x", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term term = new Application(g, x, TermFactory.createMeta(f, x));

    Term m = new Application(g, y, x);
    Substitution subst = new Subst();
    assertTrue(term.match(m, subst) == null);
    assertTrue(subst.get(x) == y);
    assertTrue(subst.get(f).equals(new Abstraction(y, y)));
  }

  @Test
  public void testMatchingFailsExistingMapping() {
    // F⟨x,y⟩ against g(x,y) where F is mapped to λxy.g(y,x)
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType(baseType("o"),arrowType("o","o")), 2);
    Term term = TermFactory.createMeta(f, x, y);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term m = new Application(g, x, y);
    Substitution subst = new Subst();
    subst.extend(x, x);
    subst.extend(y, y);
    subst.extend(f, new Abstraction(x, new Abstraction(y, new Application(g, y, x))));
    assertTrue(term.match(m, subst).equals(
      "Meta-variable F is mapped to both λx.λy.g(y, x) and to λx.λy.g(x, y)."));
  }

  @Test
  public void testMatchingCorrespondsExactlyToExistingMapping() {
    // F⟨x,y⟩ against g(x,y) where F is mapped to λxy.g(x,y)
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType(baseType("o"),arrowType("o","o")), 2);
    Term term = TermFactory.createMeta(f, x, y);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term m = new Application(g, x, y);
    Substitution subst = new Subst();
    subst.extend(x, x);
    subst.extend(y, y);
    subst.extend(f, new Abstraction(x, new Abstraction(y, new Application(g, x, y))));
    assertTrue(term.match(m, subst) == null);
  }

  @Test
  public void testMatchingAlphaCorrespondsToExistingMapping() {
  // F⟨x,y⟩ against g(u,h(v)) where x:=v,y:=u,F:=λab.g(b,h(a))
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable u = new Binder("u", baseType("o"));
    Variable v = new Binder("v", baseType("o"));
    Variable a = new Binder("a", baseType("o"));
    Variable b = new Binder("b", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType(baseType("o"),arrowType("o","o")), 2);
    Term term = TermFactory.createMeta(f, x, y);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term h = constantTerm("h", arrowType("o", "o"));
    Term m = new Application(g, u, h.apply(v));
    Substitution subst = new Subst();
    subst.extend(x, v);
    subst.extend(y, u);
    subst.extend(f, new Abstraction(a, new Abstraction(b, new Application(g, b, h.apply(a)))));
    assertTrue(term.match(m, subst) == null);
  }

  @Test
  public void testNonLinearMetaOccurrence() {
    // λx.λy.g(F⟨x⟩, F⟨y⟩) against λa.λb.g(h(z,a), h(z,b))
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    Variable a = new Binder("a", baseType("o"));
    Variable b = new Binder("b", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term h = constantTerm("h", arrowType(baseType("o"), arrowType("o", "o")));
    Term term = new Abstraction(x, new Abstraction(y, new Application(g, TermFactory.createMeta(f, x),
      TermFactory.createMeta(f, y))));
    Term m = new Abstraction(a, new Abstraction(b, new Application(g, new Application(h, z, a),
      new Application(h, z, b))));
    Substitution subst = term.match(m);
    assertTrue(subst.get(f).equals(new Abstraction(x, new Application(h, z, x))));
  }

  @Test
  public void testNonMatchDueToNonLinearity() {
    // λx.g(F⟨x⟩, F⟨x⟩) against λy.g(h(z,y), h(y,z))
    Variable x = new Binder("x", baseType("o"));
    Variable y = new Binder("y", baseType("o"));
    Variable z = new Binder("z", baseType("o"));
    MetaVariable f = TermFactory.createMetaVar("F", arrowType("o", "o"), 1);
    Term g = constantTerm("g", arrowType(baseType("o"), arrowType("o", "o")));
    Term h = constantTerm("h", arrowType(baseType("o"), arrowType("o", "o")));
    Term fx = TermFactory.createMeta(f, x);
    Term term = new Abstraction(x, new Application(g, fx, fx));
    Term m = new Abstraction(y, new Application(g, new Application(h,z,y), new Application(h,y,z)));
    assertTrue(term.match(m, new Subst()) != null);
  }
}