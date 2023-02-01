package cora.parsers;

import cora.exceptions.DeclarationException;
import cora.exceptions.IllegalRuleError;
import cora.exceptions.ParserException;
import cora.exceptions.TypingException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Term;
import cora.interfaces.types.Type;
import cora.rewriting.FirstOrderRule;
import cora.rewriting.TermRewritingSystem;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.types.ArrowType;
import cora.types.Sort;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class reads text from a string or file written in the LcTrs input format.
 * This is intended to read input by the users, which can be both as a complete
 * LCTRS, or as user commands intended for equivalence proofs of LCTRSs.
 */

public class LcTrsInputReader extends InputReader{
    private static final Type unitSort = Sort.unitSort;
    private static final Type boolSort = Sort.boolSort;
    private static final Type intSort = Sort.intSort;

    public LcTrsInputReader() { super(LcTrsParser.VOCABULARY, LcTrsParser.ruleNames); }

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
        verifyChildIsToken(tree, 0, "BRACKETOPEN", "opening bracket ')'");
        verifyChildIsToken(tree, 1, "VARDECSTART", "Start of a variable list: (VAR");
        verifyChildIsToken(tree, k, "BRACKETCLOSE", "closing bracket ')'");
        for (int i = 2; i < k; i++) {
            verifyChildIsToken(tree, i, "IDENTIFIER", "variable name (identifier)");
            String n = tree.getChild(i).getText();
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
            verifyChildIsToken(tree, 0, "IDENTIFIER", "integer identifier");
            return readIntegerType(tree.getChild(0));
        }
        int k = tree.getChildCount()-2;
        verifyChildIsToken(tree, k, "ARROW", "type arrow");
        verifyChildIsToken(tree, k+1, "IDENTIFIER", "identifier");
        String output = tree.getChild(k+1).getText();
        Type res = new Sort(output);
        for (int i = k-1; i >= 0; i--) {
            verifyChildIsToken(tree, i, "IDENTIFIER", "identifier (argument)");
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
        verifyChildIsToken(tree, 1, "IDENTIFIER", "identifier (function name)");
        verifyChildIsRule(tree, 2, "typeorarity", "integer or sort declaration");
        verifyChildIsToken(tree, 3, "BRACKETCLOSE", "closing bracket ')'");

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
        ArrayList<String> unaryBoolOperators = new ArrayList<>(){{add("~");}};
        ArrayList<String> binaryBoolOperators = new ArrayList<>(Arrays.asList("/\\", "\\/", "-->", "<-->"));
        ArrayList<String> unaryIntOperators = new ArrayList<>(){{add("-");}};
        ArrayList<String> binaryIntOperators = new ArrayList<>(Arrays.asList("*", "/", "%", "+"));
        ArrayList<String> binaryIntComparison = new ArrayList<>(Arrays.asList("<", "<=", ">", ">=", "==", "!="));
        Type unaryBool = new ArrowType(boolSort, boolSort);
        Type unaryInt = new ArrowType(intSort, intSort);
        Type boolOperation = new ArrowType(boolSort, new ArrowType(boolSort, boolSort));
        Type intOperation = new ArrowType(intSort, new ArrowType(intSort, intSort));
        Type intComparison = new ArrowType(intSort, new ArrowType(intSort, boolSort));
        addFunctionsToData(data, unaryBoolOperators, unaryBool);
        addFunctionsToData(data, binaryBoolOperators, boolOperation);
        addFunctionsToData(data, unaryIntOperators, unaryInt);
        addFunctionsToData(data, binaryIntOperators, intOperation);
        addFunctionsToData(data, binaryIntComparison, intComparison);
    }

    /**
     *
     * @param data
     * @param functionSymbols
     * @param functionType
     */
    public void addFunctionsToData(ParseData data, ArrayList<String> functionSymbols, Type functionType) {
        for (String symbol : functionSymbols) {
            data.addFunctionSymbol(new Constant(symbol, functionType));
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
    private Term readTerm(ParseTree tree, ParseData data, Type expectedType,
                          boolean mstrs) throws ParserException {
        if (expectedType != null && !expectedType.isBaseType()) {
            throw buildError(tree, "Trying to read a term of non-basic type!");
        }
        verifyChildIsToken(tree, 0, "IDENTIFIER", "Function name or variable (identifier)");

        if (tree.getChildCount() == 1) {
            return readConstantOrVariable(tree.getChild(0), data, expectedType, mstrs);
        }

        verifyChildIsToken(tree, 1, "BRACKETOPEN", "Opening bracket '('");
        verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "Closing bracket ')'");

        ArrayList<ParseTree> args = new ArrayList<>();
        if (tree.getChildCount() > 3) readTermList(tree.getChild(2), args);

        FunctionSymbol f = readFunctionSymbol(tree.getChild(0), data, args.size(), mstrs);
        Type type = f.queryType();
        ArrayList<Term> termargs = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            Type input = type.queryArrowInputType();
            termargs.add(readTerm(args.get(i), data, input, mstrs));
            type = type.queryArrowOutputType();
        }
        Term ret = new FunctionalTerm(f, termargs);
        if (expectedType != null && !ret.queryType().equals(expectedType)) {
            throw new TypingException(firstToken(tree), ret.toString(), ret.queryType().toString(),
                                        expectedType.toString());
        }
        return ret;
    }

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
        if (tree.getChildCount() == 2) {
            kind = checkChild(tree, 0);
            verifyChildIsToken(tree, 1, "IDENTIFIER", "");
            if (kind.equals("token NEGATION") || kind.equals("token MINUS")) {
                ret = getNewFunctionalTermArityOne(tree, data, tree.getChild(1).getText(), mstrs);
            }
        }
        ArrayList<String> symbols = new ArrayList<>(Arrays.asList("token CONJUNCTION", "token DISJUNCTION",
                "token CONDITIONAL", "token BICONDITIONAL", "token MULT", "token DIV", "token MOD",
                "token PLUS", "token LT", "token LTEQ", "token GT", "token GTEQ", "token EQUALITY", "token NEQ"));
        if (tree.getChildCount() >= 3) {
            kind = checkChild(tree, 1);
            if (symbols.contains(kind)) {
                System.out.println(tree.getChild(1).getText());
                verifyChildIsRule(tree, 0, "term", "a term");
                verifyChildIsRule(tree, 2, "term", "a term");
                ret = getNewFunctionalTermArityTwo(tree, data, tree.getChild(1).getText(), mstrs);
            } else {
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
                ret = new FunctionalTerm(f, termargs);
                if (expectedType != null && !ret.queryType().equals(expectedType)) {
                    throw new TypingException(firstToken(tree), ret.toString(), ret.queryType().toString(),
                            expectedType.toString());
                }
            }
        }
        return ret;
    }

    private Term getNewFunctionalTermArityOne(ParseTree tree, ParseData data, String functionSymbol,
                                              boolean mstrs) throws ParserException{
        Term child1;
        FunctionSymbol f;
        Type type;
        f = data.lookupFunctionSymbol(functionSymbol);
        type = f.queryType();
        child1 = readTermType(tree.getChild(1), data, type, mstrs);
        return new FunctionalTerm(f, child1);
    }

    private Term getNewFunctionalTermArityTwo(ParseTree tree, ParseData data, String functionSymbol,
                                              boolean mstrs) throws ParserException{
        Term child1, child2;
        FunctionSymbol f;
        Type type;
        f = data.lookupFunctionSymbol(functionSymbol);
        type = f.queryType();
        child1 = readTermType(tree.getChild(0), data, type, mstrs);
        child2 = readTermType(tree.getChild(2), data, type, mstrs);
        return new FunctionalTerm(f, child1, child2);
    }

    private Term readLogicalTerm(ParseTree tree, ParseData data, Type expectedType,
                                 boolean mstrs) throws ParserException {
        if (expectedType != null && !expectedType.isBaseType()) {
            throw buildError (tree, "Trying to read a term of a non-basic type!");
        }
        if (tree.getChildCount() == 1) {
            return readConstantOrVariable(tree.getChild(0), data, expectedType, mstrs);
        }
        FunctionSymbol f;
        Type type;
        ArrayList<Term> termArgs = new ArrayList<>();
        if (tree.getChildCount() == 2) {
            verifyChildIsToken(tree, 0, "NEGATION", "boolean negation sign");

        }
        if (tree.getChildCount() == 3) {
            verifyChildIsRule(tree, 0, "term", "a term");
            verifyChildIsRule(tree, 2, "term", "a term");
            String kind = checkChild(tree, 1);
            if (kind.equals("token CONJUNCTION")) {
                Type t = null;
            } else if (kind.equals("token DISJUNCTION")) {

            } else if (kind.equals("token CONDITIONAL")) {

            } else if (kind.equals("token BICONDITIONAL")) {

            }
        }
        return null;
    }

    /** This function reads a trsrule from the given parse tree. */
    private Rule readRule(ParseTree tree, ParseData data, boolean mstrs) throws ParserException {
        verifyChildIsRule(tree, 0, "term", "a term");
        verifyChildIsToken(tree, 1, "ARROW", "an arrow '->'");
        verifyChildIsRule(tree, 2, "term", "a term");
        String kind = checkChild(tree, 3);
        Term c = null;
        if (kind.equals("token SQUAREOPEN")) {
            verifyChildIsToken(tree, 3, "SQUAREOPEN", "Opening square bracket '['");
            verifyChildIsRule(tree, 4, "term", "Logical Term");
            verifyChildIsToken(tree, 5, "SQUARECLOSE", "Closing square bracket ']'");
            c = readTermType(tree.getChild(4), data, null, mstrs);
        }
        Term l = readTermType(tree.getChild(0), data, null, mstrs);
        Term r = readTermType(tree.getChild(2), data, l.queryType(), mstrs);
        if (c != null) {
            try {
                return new FirstOrderRule(l, r, c);
            } catch (IllegalRuleError e) {
                throw new ParserException(firstToken(tree), e.queryProblem());
            }
        } else {
            try {
                return new FirstOrderRule(l, r);
            } catch (IllegalRuleError e) {
                throw new ParserException(firstToken(tree), e.queryProblem());
            }
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

    private void readUserInput(ParseTree tree){
        ParseData data = new ParseData();
        int k = 0;
        handleUserInput(tree.getChild(k), data);
    }

    private void handleUserInput(ParseTree tree, ParseData data) {
        String kind = checkChild(tree, 0);
        if (kind.equals("token SIMPLIFICATION")) {
            verifyChildIsToken(tree, 0, "SIMPLIFICATION", "The simplification rule");
        } if (kind.equals("token EXPANSION")) {
            verifyChildIsToken(tree, 0, "EXPANSION", "The expand rule");
        } if (kind.equals("token DELETION")) {
            verifyChildIsToken(tree, 0, "DELETION", "The delete rule");
        } if (kind.equals("token POSTULATE")) {
            verifyChildIsToken(tree, 0, "POSTULATE", "The postulate rule");
        } if (kind.equals("token GENERALIZATION")) {
            verifyChildIsToken(tree, 0, "GENERALIZATION", "The generalize rule");
        } if (kind.equals("token GQDELETION")) {
            verifyChildIsToken(tree, 0, "GQDELETION", "The gq-delete rule");
        } if (kind.equals("token CONSTRUCTOR")) {
            verifyChildIsToken(tree, 0, "CONSTRUCTOR", "The constructor rule");
        } if (kind.equals("token DISPROVE")) {
            verifyChildIsToken(tree, 0, "DISPROVE", "The disprove rule");
        } if (kind.equals("token COMPLETENESS")) {
            verifyChildIsToken(tree, 0, "COMPLETENESS", "The completeness rule");
        } if (kind.equals("token CLEAR")) {
            verifyChildIsToken(tree, 0, "CLEAR", "The clear command");
        }
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

    public static void readUserInputFromString(String s) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(s, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.trs();
        collector.throwCollectedExceptions();

        reader.readUserInput(tree);
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
        return reader.readTerm(tree, data, null, false);
    }

    public static Term readLogicalTermFromString(String str, TRS trs) throws ParserException {
        ErrorCollector collector = new ErrorCollector();
        LcTrsParser parser = createLcTrsParserFromString(str, collector);
        LcTrsInputReader reader = new LcTrsInputReader();
        ParseTree tree = parser.term();
        collector.throwCollectedExceptions();

        ParseData data = new ParseData(trs);
        return reader.readTerm(tree, data, null, false);
    }
}
