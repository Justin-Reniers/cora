package z3;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import cora.exceptions.ParserException;
import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;
import cora.interfaces.terms.Variable;
import cora.parsers.LcTrsInputReader;
import cora.z3.Z3Helper;
import cora.z3.Z3TermHandler;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;
import static cora.z3.Z3Helper.*;
public class Z3TermHandlerTest {
    private static Context _ctx = new Context();
    private static Solver _s = _ctx.mkSolver();
    private static Z3Helper _z3Helper = new Z3Helper();
    private static TRS lcTrs = null;
    private static Z3TermHandler z3 = new Z3TermHandler(_ctx, _s, _z3Helper, lcTrs);

    @Test
    public void testANegAContextSatisfiability() {
        Expr<BoolSort> e1 = getBoolVar(_ctx, "x");
        Expr<BoolSort> e2 = getNot(_ctx, e1);
        Expr<BoolSort> e3 = getNot(_ctx, e2);
        assertTrue(z3.satisfiable(e2));
    }

    @Test
    public void testConflictingExpressionSatisfiability() {
        Expr<BoolSort> e1 = getBoolVar(_ctx, "x");
        Expr<BoolSort> e2 = getNot(_ctx, e1);
        Expr<BoolSort> e3 = getAnd(_ctx, e1, e2);
        assertFalse(z3.satisfiable(e3));
    }
}
