package cora.parsers;

import cora.exceptions.DeclarationException;
import cora.exceptions.IllegalRuleError;
import cora.exceptions.ParserException;
import cora.exceptions.TypingException;
import cora.interfaces.rewriting.Rule;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.FunctionSymbol;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.interfaces.types.Type;
import cora.rewriting.AtrsRule;
import cora.rewriting.FirstOrderRule;
import cora.rewriting.TermRewritingSystem;
import cora.terms.Constant;
import cora.terms.FunctionalTerm;
import cora.terms.Var;
import cora.terms.VarTerm;
import cora.types.ArrowType;
import cora.types.Sort;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class reads text from a string or file written in the LcTrs input format.
 * This is intended to read input by the users, which can be both as a complete
 * LCTRS, or as user commands intended for equivalence proofs of LCTRSs.
 */

public class LcTrsInputReader extends InputReader{
    private static Type unitSort = Sort.unitSort;

    public LcTrsInputReader() {
        super(LcTrsParser.VOCABULARY, LcTrsParser.ruleNames);
    }

    public void handleVarList(ParseTree tree, ParseData data) throws ParserException {
        int k = tree.getChildCount()-1;
        verifyChildIsToken(tree, 0, "VARDECSTART", "Start of a variable list: (VAR");
        verifyChildIsToken(tree, k, "BRACKETCLOSE", "closing bracket ')'");
        for (int i = 1; i < k; i++) {
            verifyChildIsToken(tree, i, "IDENTIFIER", "variable name (identifier)");
            String n = tree.getChild(i).getText();
            if (data.lookupVariable(n) != null) {
                throw new ParserException(firstToken(tree), "Double declaration of variable" + n);
            }
            data.addVariable(new Var(n, unitSort));
        }
    }

    /* ========= READING FUNCTION AND VARIABLE DECLARATIONS ========= */

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

    public void handleDeclaration(ParseTree tree, ParseData data) throws ParserException {
        verifyChildIsToken(tree,0,"BRACKETOPEN", "opening bracket '('");
        verifyChildIsToken(tree, 1, "IDENTIFIER", "identifier (function name)");
        verifyChildIsToken(tree, 2, "typeorarity", "integer or sort declaration");
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

    public void handleSignature(ParseTree tree, ParseData data) throws ParserException {
        int k = tree.getChildCount()-1;
        verifyChildIsToken(tree, 0, "SIGSTART", "Start of a signature: (SIG");
        verifyChildIsToken(tree, 1, "BRACKETCLOSE", "Closing bracket ')'");
        for (int i = 1; i < k; i++) {
            verifyChildIsToken(tree, i, "fundec", "Function declaration");
            handleDeclaration(tree.getChild(i), data);
        }
    }

    /* ========= READING TERMS AND RULES ========= */

    private Term readConstantOrVariable(ParseTree tree, ParseData data, Type expectedType) throws ParserException {
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
        if (expectedType != null && !expectedType.equals(unitSort)) {
            throw new TypingException(firstToken(tree), n, unitSort.toString(), expectedType.toString());
        }
        Constant f = new Constant(n, unitSort);
        data.addFunctionSymbol(f);
        return f;
    }

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

    private FunctionSymbol readFunctionSymbol(ParseTree tree, ParseData data, int numberOfArgs) throws ParserException {
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

        Type type = unitSort;
        for (int i = 0; i < numberOfArgs; i++) type = new ArrowType(unitSort, type);
        FunctionSymbol ret = new Constant(n, type);
        data.addFunctionSymbol(ret);
        return ret;
    }

    private Term readTerm(ParseTree tree, ParseData data, Type expectedType) throws ParserException {
        if (expectedType != null && !expectedType.isBaseType()) {
            throw buildError(tree, "Trying to read a term of non-basic type!");
        }
        verifyChildIsToken(tree, 0, "IDENTIFIER", "Function name or variable (identifier)");

        if (tree.getChildCount() == 1) {
            return readConstantOrVariable(tree.getChild(0), data, expectedType);
        }

        verifyChildIsToken(tree, 1, "BRACKETOPEN", "Opening bracket '('");
        verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "Closing bracket ')'");

        ArrayList<ParseTree> args = new ArrayList<ParseTree>();
        if (tree.getChildCount() > 3) readTermList(tree.getChild(2), args);

        FunctionSymbol f = readFunctionSymbol(tree.getChild(0), data, args.size());
        Type type = f.queryType();
        ArrayList<Term> termargs = new ArrayList<Term>();
        for (int i = 0; i < args.size(); i++) {
            Type input = type.queryArrowInputType();
            termargs.add(readTerm(args.get(i), data, input));
            type = type.queryArrowOutputType();
        }
        Term ret = new FunctionalTerm(f, termargs);
        if (expectedType != null && !ret.queryType().equals(expectedType)) {
            throw new TypingException(firstToken(tree), ret.toString(), ret.queryType().toString(),
                                        expectedType.toString());
        }
        return ret;
    }

    private Term readLogicalTerm(ParseTree tree, ParseData data, Type expectedType) throws ParserException {
        if (expectedType != null && !expectedType.isBaseType()) {
            throw buildError (tree, "Trying to read a term of a non-basic type!");
        }
        if (tree.getChildCount() == 1) {
            return readConstantOrVariable(tree.getChild(0), data, expectedType);
        }
        FunctionSymbol f;
        Type type;
        ArrayList<Term> termArgs = new ArrayList<Term>();
        if (tree.getChildCount() == 2) {
            verifyChildIsToken(tree, 0, "NEGATION", "boolean negation sign");

        }
        if (tree.getChildCount() == 3) {
            verifyChildIsRule(tree, 0, "term", "a term");
            verifyChildIsRule(tree, 2, "term", "a term");
            String kind = checkChild(tree, 2);
            if (kind.equals("token CONJUNCTION")) {

            } else if (kind.equals("token DISJUNCTION")) {

            } else if (kind.equals("token CONDITIONAL")) {

            } else if (kind.equals("token BICONDITIONAL")) {

            }
        }
        return null;
    }

    private Rule readRule(ParseTree tree, ParseData data) throws ParserException {
        verifyChildIsRule(tree, 0, "term", "a term");
        verifyChildIsToken(tree, 1, "ARROW", "an arrow '->'");
        verifyChildIsRule(tree, 2, "term", "a term");
        String kind = checkChild(tree, 3);
        Term c = null;
        if (kind.equals("token SQUAREOPEN")) {
            verifyChildIsToken(tree, 3, "SQUAREOPEN", "Opening square bracket '['");
            verifyChildIsRule(tree, 4, "term", "Logical Term");
            verifyChildIsToken(tree, 5, "SQUARECLOSE", "Closing square bracket ']'");
            c = readTerm(tree.getChild(4), data, null);
        }
        Term l = readTerm(tree.getChild(0), data, null);
        Term r = readTerm(tree.getChild(2), data, l.queryType());
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

    private ArrayList<Rule> readRuleList(ParseTree tree, ParseData data) throws ParserException {
        verifyChildIsToken(tree, 0, "RULEDECSTART", "(RULES");
        verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "Closing bracket ')'");
        ArrayList<Rule> ret = new ArrayList<Rule>();
        for (int i = 1; i < tree.getChildCount()-1; i++) {
            ret.add(readRule(tree.getChild(i), data));
        }
        return ret;
    }

    /* ========= READ FULL LCTRS ========= */

    private TRS readLCTRS(ParseTree tree) throws ParserException {
        ParseData data = new ParseData();
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
            kind = checkChild(tree, k);
        }
        verifyChildIsRule(tree, k, "ruleslist", "List of rules");
        ArrayList<Rule> rules = readRuleList(tree.getChild(k), data);
        return new TermRewritingSystem(data.queryCurrentAlphabet(), rules);
    }

    private void readUserInput(ParseTree tree) throws ParserException {
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
        return reader.readTerm(tree, data, null);
    }
}
