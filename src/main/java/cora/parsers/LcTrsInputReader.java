package cora.parsers;

import cora.exceptions.*;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.smt.UserCommand;
import cora.interfaces.terms.*;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.rewriting.TermRewritingSystem;
import cora.terms.Subst;
import cora.usercommands.*;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.terms.positions.ArgumentPosition;
import cora.terms.positions.EmptyPosition;
import cora.types.ArrowType;
import cora.types.Sort;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * This class reads text from a string or file written in the LcTrs input format.
 * This is intended to read input by the users, which can be both as a complete
 * LCTRS, or as user commands intended for equivalence proofs of LCTRSs.
 */

public class LcTrsInputReader extends InputReader{
    private static final Type unitSort = Sort.unitSort;
    private static final Type boolSort = Sort.boolSort;
    private static final Type intSort = Sort.intSort;
    private static final ArrayList<String> unaryBoolOperators = new ArrayList<>(Arrays.asList("~"));
    private static final ArrayList<String> binaryBoolOperators = new ArrayList<>(Arrays.asList("/\\", "\\/", "-->", "<-->", "==b", "!=b"));
    private static final ArrayList<String> unaryIntOperators = new ArrayList<>(Arrays.asList("-"));
    private static final ArrayList<String> binaryIntOperators = new ArrayList<>(Arrays.asList("*", "/", "%", "+"));
    private static final ArrayList<String> binaryIntComparison = new ArrayList<>(Arrays.asList("<", "<=", ">", ">=", "==i", "!=i"));


    public LcTrsInputReader() { super(cora.parsers.LcTrsParser.VOCABULARY, cora.parsers.LcTrsParser.ruleNames); }

    /* ========= READING FUNCTION AND VARIABLE DECLARATIONS ========= */

    /**
     * Given that the tree represents a varlist, this function updates data with all the provided
     * variable declarations.
     * Since varlists can only occur in UNSORTED LCTRSs, the variables are all stored to have the unit
     * sort ("o") as their type.
     * (Note: only public for unit testing.)
     */
    public void handleVarList(ParseTree tree, ParseData data) throws ParserException {
        int k = tree.getChildCount()-1;
        verifyChildIsToken(tree, 0, "BRACKETOPEN", "opening bracket '('");
        verifyChildIsToken(tree, 1, "VARDECSTART", "Start of a variable list: VAR");
        verifyChildIsToken(tree, k, "BRACKETCLOSE", "closing bracket ')'");
        for (int i = 2; i < k; i++) {
            verifyChildIsRule(tree, i, "identifier", "variable name (identifier)");
            String n = tree.getChild(i).getText();
            verifyChildIsToken(tree.getChild(i), 0, "WORD", "word identifier");
            if (data.lookupVariable(n) != null) {
                throw new ParserException(firstToken(tree), "Double declaration of variable" + n);
            }
            data.addVariable(new Var(n, unitSort));
        }
    }

    /**
     * This function takes an IDENTIFIER, which must be a integer; if the integer is k, then the type
     * o->...->o->o is returned, with in total k+1 "o"s (so k input arguments), where "o" is the unit
     * sort.
     * If the identifier is not an integer, a ParserException is thrown.
     */
    private Type readIntegerType(ParseTree tree) throws ParserException {
        int k;
        try {k = Integer.parseInt(tree.getText());}
        catch (NumberFormatException e) {
            throw new ParserException(firstToken(tree), "Unexpected identifier'" + tree.getText() +
                                                        "; expected an integer!");
        }
        Type ret = unitSort;
        for (int i = 0; i < k; i++) ret = new ArrowType(unitSort, ret);
        return ret;
    }

    /**
     * Reads a type of the form sort1...sortN -> outputsort, or an integer k; the integer is turned
     * into the type o1...ok -> o, where "o" is a sort.
     * (Note: only public for unit testing.)
     */
    public Type readTypeOrArity(ParseTree tree) throws ParserException {
        if (tree.getChildCount() == 1) {
            verifyChildIsToken(tree, 0, "NUM", "integer identifier");
            return readIntegerType(tree.getChild(0));
        }
        int k = tree.getChildCount()-2;
        verifyChildIsToken(tree, k, "ARROW", "type arrow");
        verifyChildIsRule(tree, k+1, "identifier", "identifier");
        String output = tree.getChild(k+1).getText();
        Type res;
        if (output.toLowerCase().equals("int") || output.toLowerCase().equals("integer")) res = intSort;
        else if (output.toLowerCase().equals("bool") || output.toLowerCase().equals("boolean")) res = boolSort;
        else res = new Sort(output);
        for (int i = k-1; i >= 0; i--) {
            verifyChildIsRule(tree, i, "identifier", "identifier (argument)");
            String arg = tree.getChild(i).getText();
            res = new ArrowType(new Sort(arg), res);
        }
        return res;
    }

    /**
     * Given that tree represents a single function declaration, this function updates data with the
     * new declaration.
     */
    public void handleDeclaration(ParseTree tree, ParseData data) throws ParserException {
        verifyChildIsToken(tree,0,"BRACKETOPEN", "opening bracket '('");
        verifyChildIsRule(tree, 1, "identifier", "identifier (function name)");
        verifyChildIsRule(tree, 2, "typeorarity", "integer or sort declaration");
        verifyChildIsToken(tree, 3, "BRACKETCLOSE", "closing bracket ')'");

        verifyChildIsToken(tree.getChild(1), 0, "WORD", "word identifier");
        String funcname = tree.getChild(1).getText();
        Type type = readTypeOrArity(tree.getChild(2));

        if (data.lookupFunctionSymbol(funcname) != null) {
            throw new ParserException(firstToken(tree), "Double declaration of " + funcname);
        }
        if (data.lookupVariable(funcname) != null) {
            throw new ParserException(firstToken(tree), "Function symbol " + funcname +
                                                        " was previously declared as variable");
        }
        data.addFunctionSymbol(new Constant(funcname, type));
    }

    /**
     * Given that the tree represents a siglist, this function updates data with all the provided
     * declarations.
     */
    public void handleSignature(ParseTree tree, ParseData data) throws ParserException {
        int k = tree.getChildCount()-1;
        verifyChildIsToken(tree, 0, "BRACKETOPEN", "Opening bracket ')'");
        verifyChildIsToken(tree, 1, "SIGSTART", "Start of a signature: (SIG");
        verifyChildIsToken(tree, k, "BRACKETCLOSE", "Closing bracket ')'");
        for (int i = 2; i < k; i++) {
            verifyChildIsRule(tree, i, "fundec", "Function declaration");
            handleDeclaration(tree.getChild(i), data);
        }
    }

    /**
     * Adds all the basic signature declarations built into the lexer and parser for boolean operations,
     * integer operations and integer comparisons to the data.
     * @param data The data to be updated with all the basic signature declarations.
     */
    public void handleBasicSignature(ParseData data) {
        boolean infix = true;
        Type unaryBool = new ArrowType(boolSort, boolSort);
        Type unaryInt = new ArrowType(intSort, intSort);
        Type boolOperation = new ArrowType(boolSort, new ArrowType(boolSort, boolSort));
        Type intOperation = new ArrowType(intSort, new ArrowType(intSort, intSort));
        Type intComparison = new ArrowType(intSort, new ArrowType(intSort, boolSort));
        data.addFunctionSymbol(new Constant("~", unaryBool, infix, 2));
        data.addFunctionSymbol(new Constant("/\\", boolOperation, true, 11));
        data.addFunctionSymbol(new Constant("\\/", boolOperation, true, 12));
        data.addFunctionSymbol(new Constant("-->", boolOperation, true, 13));
        data.addFunctionSymbol(new Constant("<-->", boolOperation, true, 14));
        data.addFunctionSymbol(new Constant("-", unaryInt, true, 2));
        data.addFunctionSymbol(new Constant("*", intOperation, true, 3));
        data.addFunctionSymbol(new Constant("/", intOperation, true, 3));
        data.addFunctionSymbol(new Constant("%", intOperation, true, 3));
        data.addFunctionSymbol(new Constant("+", intOperation, true, 4));
        data.addFunctionSymbol(new Constant("<", intComparison, true, 6));
        data.addFunctionSymbol(new Constant("<=", intComparison, true, 6));
        data.addFunctionSymbol(new Constant(">", intComparison, true, 6));
        data.addFunctionSymbol(new Constant(">=", intComparison, true, 6));
        data.addFunctionSymbol(new Constant("==i", intComparison, true, 7));
        data.addFunctionSymbol(new Constant("!=i", intComparison, true, 7));
        data.addFunctionSymbol(new Constant("==b", boolOperation, true, 7));
        data.addFunctionSymbol(new Constant("!=b", boolOperation, true, 7));
        addBooleanConstantsToData(data);
    }

    private void addBooleanConstantsToData(ParseData data) {
        data.addFunctionSymbol(new Constant("TRUE", boolSort));
        data.addFunctionSymbol(new Constant("FALSE", boolSort));
    }

    /**
     * @param data
     * @param functionSymbols
     * @param functionType
     * @param infix
     */
    public void addFunctionsToData(ParseData data, ArrayList<String> functionSymbols, Type functionType,
                                   boolean infix, int precedence) {
        for (String symbol : functionSymbols) {
            data.addFunctionSymbol(new Constant(symbol, functionType, infix, precedence));
        }
    }

    /* ========= READING TERMS AND RULES ========= */

    /**
     * This function reads an identifier from the parse tree that should be either a variable or a
     * constant function symbol with the expected type.
     * If the symbol is already declared as a variable or function symbol, then the corresponding
     * symbol will be returned (if this does not give a type conflict with expectedType).  Otherwise:
     * - if mstrs is true, then all function symbols should have been declared; therefore, the symbol
     *   will then be considered a variable, and declared to have the expected type (if expectedType
     *   is null, a DeclarationException is thrown);
     * - if mstrs is false, then all variables should have been declared; the symbol will be
     *   considered a constant function symbol, and declared to have type o (the unit type).
     */
    private Term readConstantOrVariable(ParseTree tree, ParseData data, Type expectedType,
                                        boolean mstrs) throws ParserException {
        String n = tree.getText();
        Term ret = data.lookupVariable(n);
        if (ret == null) ret = data.lookupFunctionSymbol(n);
        if (ret != null) {
            if (expectedType != null && !ret.queryType().equals(expectedType)) {
                throw new TypingException(firstToken(tree), n, ret.queryType().toString(),
                                            expectedType.toString());
            }
            return ret;
        }
        try {
            Integer.parseInt(n);
            if (expectedType == boolSort) throw new TypingException(firstToken(tree), n, intSort.toString(),
                                            expectedType.toString());
            return new Constant(n, intSort);
        } catch (NumberFormatException ignored) {}
        if (mstrs) {
            if (expectedType == null) throw new DeclarationException(firstToken(tree), n);
            Var x = new Var(n, expectedType);
            data.addVariable(x);
            return x;
        } else {
            if (expectedType != null && !expectedType.equals(unitSort)) {
                throw new TypingException(firstToken(tree), n, unitSort.toString(), expectedType.toString());
            }
            Constant f = new Constant(n, unitSort);
            data.addFunctionSymbol(f);
            return f;
        }
    }

    /**
     * This function reads a termlist into an arraylist of parse trees, each of which points to a
     * parsetree of kind "term".
     * All encountered terms are added onto the back of lst.
     */
    private void readTermList(ParseTree termlisttree, ArrayList<ParseTree> l) {
        while(true) {
            verifyChildIsRule(termlisttree, 0, "term", "Term");
            l.add(termlisttree.getChild(0));
            if (termlisttree.getChildCount() == 1) return;
            verifyChildIsToken(termlisttree, 1, "COMMA", "Comma ','");
            verifyChildIsRule(termlisttree, 2, "termlist", "List of terms");
            termlisttree = termlisttree.getChild(2);
        }
    }

    /**
     * This determines the FunctionSymbol associated with the given parse tree (which should be an
     * IDENTIFIER), provided that it occurs in a context with numberOfArguments arguments.
     * If we are considering an mstrs, the symbol MUST be declared in the parsing data; if this is
     * not the case, a ParserException is thrown. If we are considering an unsorted trs, a function
     * symbol with a type given by the arity (and using only unit types) is declared and returned.
     */
    private FunctionSymbol readFunctionSymbol(ParseTree tree, ParseData data, int numberOfArgs,
                                              boolean mstrs) throws ParserException {
        String n = tree.getText();
        FunctionSymbol f = data.lookupFunctionSymbol(n);

        if (f != null) {
            if (f.queryType().queryArity() != numberOfArgs) {
                throw new TypingException(firstToken(tree), n, f.queryType().toString(),
                                        "Type with arity " + numberOfArgs);
            }
            return f;
        }
        if (data.lookupVariable(n) != null) {
            throw new ParserException(firstToken(tree), "Declared variable " + n + "used as function");
        }
        if (mstrs) {
            throw new DeclarationException(firstToken(tree), n);
        }

        Type type = unitSort;
        for (int i = 0; i < numberOfArgs; i++) type = new ArrowType(unitSort, type);
        FunctionSymbol ret = new Constant(n, type);
        data.addFunctionSymbol(ret);
        return ret;
    }

    /**
     * This reads the given term from the parse tree, and throws a parser exception if for example
     * typing does not check out.
     * If we are parsing a mstrs, then all unknown symbols are expected to be variables; if it is an
     * unsorted trs, then all unknown symbols are expected to be function symbols.
     */
    private Term readTermType(ParseTree tree, ParseData data, Type expectedType,
                                boolean mstrs) throws ParserException {
        if (expectedType != null && !expectedType.isBaseType()) {
            throw buildError(tree, "Trying to read a term of a non-basic type!");
        }
        if (tree.getChildCount() == 1) {
            return readConstantOrVariable(tree.getChild(0), data, expectedType, mstrs);
        }
        String kind;
        Term ret = null;
        ArrayList<String> symbols = new ArrayList<>(Arrays.asList("token CONJUNCTION", "token DISJUNCTION",
                "token CONDITIONAL", "token BICONDITIONAL", "token MULT", "token DIV", "token MOD",
                "token PLUS", "token LT", "token LTEQ", "token GT", "token GTEQ", "token EQUALITYI", "token NEQI",
                        "token EQUALITYB", "token NEQB"));
        if (tree.getChildCount() == 4 && (checkChild(tree, 0).equals("token NEGATION") ||
                checkChild(tree, 0).equals("token MINUS"))) {
            ret = getNewFunctionalTermArityOne(tree, data, tree.getChild(0).getText(), mstrs);
        }
        else if (tree.getChildCount() == 2) {
            kind = checkChild(tree, 0);
            if (kind.equals("token NEGATION") || kind.equals("token MINUS")) {
                ret = getNewFunctionalTermArityOne(tree, data, tree.getChild(0).getText(), mstrs);
            }
        }
        else if (tree.getChildCount() >= 3) {
            kind = checkChild(tree, 1);
            if (kind.equals("token MINUS")) {
                verifyChildIsToken(tree, 1, "MINUS", "Minus operator '-'");
                ret = subtractionOperation(tree, data, mstrs);
            }
            else if (symbols.contains(kind)) {
                verifyChildIsRule(tree, 0, "term", "a term");
                verifyChildIsRule(tree, 2, "term", "a term");
                ret = getNewFunctionalTermArityTwo(tree, data, tree.getChild(1).getText(), mstrs);
            } else {
                ret = getNewFunctionalTermHighArity(tree, data, mstrs);
                if (expectedType != null && !ret.queryType().equals(expectedType)) {
                    throw new TypingException(firstToken(tree), ret.toString(), ret.queryType().toString(),
                            expectedType.toString());
                }
            }
        }
        return ret;
    }

    /**
     * This constructs a subtraction term from the parse tree. It recursively constructs (and type checks)
     * the two children of which the subtraction operation is comprised. Also handles nested unary subtraction
     * terms.
     */
    private Term subtractionOperation(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        Term child1, child2;
        FunctionSymbol f = data.lookupFunctionSymbol("+");
        child1 = readTermType(tree.getChild(0), data, intSort, mstrs);
        child2 = new FunctionalTerm(data.lookupFunctionSymbol("-"), readTermType(tree.getChild(2), data,
                intSort, mstrs));
        return new FunctionalTerm(f, child1, child2);
    }

    /**
     * This constructs the given term from the parse tree, assuming that it a predefined term with
     * arity one (which are "-" and "~"). Recursively constructs the child of the unary operator.
     */
    private Term getNewFunctionalTermArityOne(ParseTree tree, ParseData data, String functionSymbol,
                                              boolean mstrs) throws ParserException {
        Term child1 = null;
        FunctionSymbol f;
        f = data.lookupFunctionSymbol(functionSymbol);
        if (tree.getChildCount() == 2) {
            if (unaryBoolOperators.contains(f.queryName())) {
                child1 = readTermType(tree.getChild(1), data, boolSort, mstrs);
            }
            if (unaryIntOperators.contains(f.queryName())) {
                child1 = readTermType(tree.getChild(1), data, intSort, mstrs);
            }
        } else {
            verifyChildIsToken(tree, 1, "BRACKETOPEN", "BRACKETOPEN");
            verifyChildIsToken(tree, 3, "BRACKETCLOSE", "BRACKETCLOSE");
            if (unaryBoolOperators.contains(f.queryName())) {
                child1 = readTermType(tree.getChild(2), data, boolSort, mstrs);
            }
            if (unaryIntOperators.contains(f.queryName())) {
                child1 = readTermType(tree.getChild(2), data, intSort, mstrs);
            }
        }
        return new FunctionalTerm(f, child1);
    }

    /**
     * This constructs the given term from the parse tree, assuming that it a predefined term with
     * arity two. Recursively constructs the two children of the binary operator. Also ensures that
     * the expected type corresponds with the function symbol (e.g. two children of type intSort for
     * the "+" operator etc.)
     */
    private Term getNewFunctionalTermArityTwo(ParseTree tree, ParseData data, String functionSymbol,
                                              boolean mstrs) throws ParserException {
        Term child1, child2;
        FunctionSymbol f;
        f = data.lookupFunctionSymbol(functionSymbol);
        if (binaryBoolOperators.contains(f.queryName())){
            child1 = readTermType(tree.getChild(0), data, boolSort, mstrs);
            child2 = readTermType(tree.getChild(2), data, boolSort, mstrs);
        } else {
            child1 = readTermType(tree.getChild(0), data, intSort, mstrs);
            child2 = readTermType(tree.getChild(2), data, intSort, mstrs);
        }
        return new FunctionalTerm(f, child1, child2);
    }

    /**
     * This constructs the given term from the parse tree with arity two or higher.
     * Recursively constructs the two children of the binary operator.
     */
    private Term getNewFunctionalTermHighArity(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        verifyChildIsToken(tree, 1, "BRACKETOPEN", "Opening bracket '('");
        verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "Closing bracket ')'");
        ArrayList<ParseTree> args = new ArrayList<>();
        if (tree.getChildCount() > 3) readTermList(tree.getChild(2), args);
        FunctionSymbol f = readFunctionSymbol(tree.getChild(0), data, args.size(), mstrs);
        Type type = f.queryType();
        ArrayList<Term> termargs = new ArrayList<>();
        for (ParseTree arg : args) {
            Type input = type.queryArrowInputType();
            termargs.add(readTermType(arg, data, input, mstrs));
            type = type.queryArrowOutputType();
        }
        return new FunctionalTerm(f, termargs);
    }

    /**
     * This reads the given logical constraint from the parse tree.
     */
    private Term readLogicalConstraint(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        verifyChildIsToken(tree, 0, "SQUAREOPEN", "Opening square bracket '['");
        verifyChildIsRule(tree, 1, "term", "Logical Term");
        verifyChildIsToken(tree, 2, "SQUARECLOSE", "Closing square bracket ']'");
        Term t = readTermType(tree.getChild(1), data, boolSort, mstrs);
        return t;
    }

    /** This function reads a trsrule from the given parse tree. If a trsrule does not
     * contain a constraint, the constraint is set to "TRUE".
     */
    private Rule readRule(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        verifyChildIsRule(tree, 0, "term", "a term");
        verifyChildIsToken(tree, 1, "ARROW", "an arrow '->'");
        verifyChildIsRule(tree, 2, "term", "a term");
        FunctionSymbol a;
        Term l = readTermType(tree.getChild(0), data, null, mstrs);
        Term r = readTermType(tree.getChild(2), data, l.queryType(), mstrs);
        String kind = checkChild(tree, 3);
        Term c = null;
        if (kind.equals("rule logicalconstraint")) {
            verifyChildIsRule(tree, 3, "logicalconstraint", "Logical constraint");
            c = readLogicalConstraint(tree.getChild(3), data, mstrs);
        } else {
            FunctionSymbol f;
            c = new Constant("TRUE", boolSort);
        }
        try {
            return new FirstOrderRule(l, r, c);
        } catch (IllegalRuleError e) {
            throw new ParserException(firstToken(tree), e.queryProblem());
        }
    }

    /** This function reads a rule list (RULES...) into a list of rules. */
    private ArrayList<Rule> readRuleList(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        verifyChildIsToken(tree, 0, "BRACKETOPEN", "Opening bracket '('");
        verifyChildIsToken(tree, 1, "RULEDECSTART", "(RULES");
        verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "Closing bracket ')'");
        ArrayList<Rule> ret = new ArrayList<>();
        for (int i = 2; i < tree.getChildCount()-1; i++) {
            if (mstrs) data.clearVariables();
            ret.add(readRule(tree.getChild(i), data, mstrs));
        }
        return ret;
    }

    /* ========= READ FULL LCTRS ========= */

    private TRS readLCTRS(ParseTree tree) throws ParserException {
        ParseData data = new ParseData();
        handleBasicSignature(data);
        int k = 0;
        String kind = checkChild(tree, k);
        if (kind.equals("rule varlist")) {
            handleVarList(tree.getChild(k), data);
            k++;
            kind = checkChild(tree, k);
        }
        if (kind.equals("rule siglist")) {
            handleSignature(tree.getChild(k), data);
            k++;
        }
        verifyChildIsRule(tree, k, "ruleslist", "List of rules");
        ArrayList<Rule> rules = readRuleList(tree.getChild(k), data, true);
        return new TermRewritingSystem(data.queryCurrentAlphabet(), rules);
    }

    /* ========= USER INPUT METHODS ========= */

    /**
     * This function reads a user command from the given parse tree.
     */
    private UserCommand readUserInput(ParseTree tree, TRS lcTrs, TreeSet<Variable> env) throws ParserException{
        ParseData data = new ParseData(lcTrs, env);
        //for (Variable v : env) data.addVariable(v);
        //for (FunctionSymbol f : lcTrs.querySymbols()) data.addFunctionSymbol(f);
        return handleUserInput(tree.getChild(0), data);
    }

    /**
     * This function decides what user command was given from the given parse tree. If a user
     * command is not implemented yet, it throws an UnsupportedRewritingRuleException.
     */
    private UserCommand handleUserInput(ParseTree tree, ParseData data) throws ParserException,
            InvalidPositionException {
        String kind = checkChild(tree, 0);
        if (kind.equals("token SIMPLIFICATION")) {
            verifyChildIsToken(tree, 0, "SIMPLIFICATION", "The simplification rule");
            if (tree.getChildCount() == 1) return new SimplifyCommand();
            verifyChildIsRule(tree, 1, "pos", "Position rule");
            verifyChildIsToken(tree, 2, "NUM", "Rule index numerical");
            Position pos = parsePosition(tree.getChild(1), false);
            if (tree.getChildCount() == 4) {
                Substitution s = parseSubstitution(tree.getChild(3), data);
                return new SimplifyCommand(pos, Integer.parseInt(tree.getChild(2).getText()), s);
            }
            return new SimplifyCommand(pos, Integer.parseInt(tree.getChild(2).getText()));
        } if (kind.equals("token EXPANSION")) {
            verifyChildIsToken(tree, 0, "EXPANSION", "The expand rule");
            verifyChildIsRule(tree, 1, "pos", "Position rule");
            Position pos = parsePosition(tree.getChild(1), false);
            if (tree.getChildCount() > 2) {
                verifyChildIsRule(tree, 2, "termination", "Termination rule");
                Boolean b = readTermination(tree.getChild(2), data);
                return new ExpandCommand(pos, b);
            }
            return new ExpandCommand(pos);
        } if (kind.equals("token DELETION")) {
            verifyChildIsToken(tree, 0, "DELETION", "The delete rule");
            return new DeleteCommand();
        } if (kind.equals("token POSTULATE")) {
            verifyChildIsToken(tree, 0, "POSTULATE", "The postulate rule");
            verifyChildIsRule(tree, 1, "term", "Left proof term");
            verifyChildIsRule(tree, 2, "term", "Right proof term");
            verifyChildIsRule(tree, 3, "logicalconstraint", "Logical constraint");
            Term l = readTermType(tree.getChild(1), data, null, true);
            Term r = readTermType(tree.getChild(2), data, null, true);
            Term c = readLogicalConstraint(tree.getChild(3), data, true);
            return new PostulateCommand(l, r, c);
        } if (kind.equals("token GENERALIZATION")) {
            verifyChildIsToken(tree, 0, "GENERALIZATION", "The generalize rule");
            throw new UnsupportedRewritingRuleException("Generalize rule not yet supported");
        } if (kind.equals("token EQDELETION")) {
            verifyChildIsToken(tree, 0, "EQDELETION", "The eq-delete rule");
            return new EQDeleteCommand();
        } if (kind.equals("token CONSTRUCTOR")) {
            verifyChildIsToken(tree, 0, "CONSTRUCTOR", "The constructor rule");
            return new ConstructorCommand();
        } if (kind.equals("token DISPROVE")) {
            verifyChildIsToken(tree, 0, "DISPROVE", "The disprove rule");
            return new DisproveCommand();
        } if (kind.equals("token COMPLETENESS")) {
            verifyChildIsToken(tree, 0, "COMPLETENESS", "The completeness rule");
            return new CompletenessCommand();
        } if (kind.equals("token CLEAR")) {
            verifyChildIsToken(tree, 0, "CLEAR", "The clear command");
            throw new UnsupportedRewritingRuleException("Clear command not yet supported");
        } if (kind.equals("token SWAP")) {
            verifyChildIsToken(tree, 0, "SWAP", "The swap command");
            if (tree.getChildCount() == 1) return new SwapCommand();
            verifyChildIsToken(tree, 1, "NUM", "Numeral");
            verifyChildIsToken(tree, 2, "NUM", "Numeral");
            return new SwapCommand(Integer.parseInt(tree.getChild(1).getText()),
                    Integer.parseInt(tree.getChild(2).getText()));
        } if (kind.equals("token UNDO")) {
            verifyChildIsToken(tree, 0, "UNDO", "The undo command");
            return new UndoCommand();
        } if (kind.equals("token REWRITE")) {
            verifyChildIsToken(tree, 0, "REWRITE", "The rewrite command");
            verifyChildIsRule(tree, 1, "logicalconstraint", "Logical constraint");
            verifyChildIsRule(tree, 2, "logicalconstraint", "Logical constraint");
            Term old = readLogicalConstraint(tree.getChild(1), data, true);
            Term n = readLogicalConstraint(tree.getChild(2), data, true);
            return new RewriteConstraintCommand(old, n);
        } if (kind.equals("token RENAME")) {
            verifyChildIsRule(tree, 1, "term", "a term");
            verifyChildIsRule(tree, 2, "term", "a term");
            try {
                Type expected = data.lookupVariable(tree.getChild(1).getText()).queryType();
                Term old = data.lookupVariable(tree.getChild(1).getText()); //readTermType(tree.getChild(1), data, expected, true);
                Term n = readTermType(tree.getChild(2), data, expected, true);
                return new RenameCommand(old, n);
            } catch (NullPointerException e) {
                return new RenameCommand();
            }
        }
        return null;
    }

    private Boolean readTermination(ParseTree tree, ParseData data) {
        String kind = checkChild(tree, 0);
        if (kind.equals("token TERMINATING")) return true;
        else if (kind.equals("token NONTERMINATING")) return false;
        return null;
    }

    /**
     *  This function reads a position from the given parse tree. A position is of the
     *  form x or x.a... where x and a are integers.
     */
    /**private Position parsePosition(ParseTree tree, boolean subtree) throws ParserException {
        if (tree.getChild(0).getText().equals("ε")) return new EmptyPosition();
        if (Integer.parseInt(tree.getChild(0).getText()) != 0 && !subtree) {
            throw new InvalidPositionException(tree.getChild(0).getText());
        }
        if (Integer.parseInt(tree.getChild(0).getText()) == 0 && subtree) {
            throw new InvalidPositionException(tree.getChild(0).getText());
        }
        if (tree.getChildCount() > 1) {
            verifyChildIsToken(tree, 1, "DOT", "dot in between position numerals");
            verifyChildIsRule(tree, 2, "pos", "position rule");
            return new ArgumentPosition(Integer.parseInt(tree.getChild(0).getText()),
                    parsePosition(tree.getChild(2), true));
        }
        return new EmptyPosition();
    }**/

    private Position parsePosition(ParseTree tree, boolean subtree) throws ParserException {
        if (tree.getChild(0).getText().equals("ε")) return new EmptyPosition();
        verifyChildIsToken(tree, 0, "NUM", "numeral in position");
        if (tree.getChildCount() > 1) {
            verifyChildIsToken(tree, 1, "DOT", "dot in between position numerals");
            verifyChildIsRule(tree, 2, "pos", "position rule");
            return new ArgumentPosition(Integer.parseInt(tree.getChild(0).getText()),
                    parsePosition(tree.getChild(2), true));
        } else if (Integer.parseInt(tree.getChild(0).getText()) != 0) {
            throw new InvalidPositionException(tree.getText());
        }
        return new EmptyPosition();
    }

    private Substitution parseSubstitution(ParseTree tree, ParseData data) throws ParserException {
        verifyChildIsToken(tree, 0, "SQUAREOPEN", "opening square bracket");
        verifyChildIsToken(tree, tree.getChildCount() - 1, "SQUARECLOSE",
                "closing square bracket");
        Substitution s = new Subst();
        for (int i = 1; i <= tree.getChildCount() - 2; i++) {
            if (i % 2 == 1) {
                verifyChildIsRule(tree, i, "repl", "replacement rule");
                ParseTree t = tree.getChild(i);
                verifyChildIsToken(t, 1, "ASSIGNMENT", "assignment");
                verifyChildIsRule(t, 2, "term", "a term");
                verifyChildIsRule(t, 0, "term", "a term");
                Term r = readTermType(t.getChild(2), data, null, true);
                Term l = readTermType(t.getChild(0), data, r.queryType(), true);
                if (l instanceof Variable) s.extend((Variable) l, r);
                else {
                    throw new TypingException(firstToken(tree), t.getText(), l.queryType().toString(),
                            r.queryType().toString());
                }
            } else {
                verifyChildIsToken(tree, i, "COMMA", "comma");
            }
        }
        return s;
    }

    /* ========= STATIC ACCESS METHODS ========= */

    public static LcTrsParser createLcTrsParserFromString(String s, ErrorCollector collector) {
        LcTrsLexer lexer = new LcTrsLexer(CharStreams.fromString(s));
        lexer.removeErrorListeners();
        lexer.addErrorListener(collector);
        LcTrsParser parser = new LcTrsParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(collector);
        return parser;
    }

    private static LcTrsParser createLcTrsParserFromFile(String filename, ErrorCollector collector) {
        CharStream input = null;
        try {
            input = CharStreams.fromFileName(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LcTrsLexer lexer = new LcTrsLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(collector);
        LcTrsParser parser = new LcTrsParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(collector);
        return parser;
    }

    public static TRS readLcTrsFromString(String s) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(s, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.trs();
        collector.throwCollectedExceptions();

        return reader.readLCTRS(tree);
    }

    public static UserCommand readUserInputFromString(String s, TRS lcTrs, TreeSet<Variable> env) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(s, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.trs();
        collector.throwCollectedExceptions();

        return reader.readUserInput(tree, lcTrs, env);
    }

    public static TRS readLcTrsFromFile(String filename) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromFile(filename, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.trs();
        collector.throwCollectedExceptions();

        return reader.readLCTRS(tree);
    }

    public static Term readTermFromString(String str, TRS trs) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(str, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.term();
        collector.throwCollectedExceptions();

        ParseData data = new ParseData(trs);
        return reader.readTermType(tree, data, null, true);
    }

    public static Term readTermFromStringWithEnv(String str, TRS trs, TreeSet<Variable> env) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(str, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.term();
        collector.throwCollectedExceptions();

        ParseData data = new ParseData(trs, env);
        return reader.readTermType(tree, data, null, true);
    }
}
