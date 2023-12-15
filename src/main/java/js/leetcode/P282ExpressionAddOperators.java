package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Ok, it works, but is again 5% of accepted solutions.
 * 
 * I think I need some memoization with recursive calls, based on the intuition
 * that +/- operations are lowest precedence and thus good 'splitting' points
 * 
 * How about this:
 * 
 * Work from the right. Consider a string of digits S=s1s2...sn.
 * 
 * This can be expressed as [s1s2...sn-1] OP [sn]
 * 
 * If OP = + or - then we recursively look for expressions in [s1...n-1] that
 * evaluate to target-sn, or target+sn resp.
 * 
 * If OP is concatenation, then we recursively look for expressions in
 * [s1...n-2] that evaluate to corresponding variations of [sn-1sn].
 */
public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {
    x(1051, 106, "105+1");
    // if (true) return;
    xx(105, 5, "1*0+5", "10-5");
    xx(123, 6, "1*2*3", "1+2+3");
    //x(232, 8, "2*3+2", "2+3*2");
    //x(702, 2, "7*0+2");
    xx(735, 9, "7-3+5");

    xx(9999999999L, 1409865409);
    xx(123456789, 45, "1*2*3*4*5-6-78+9", "1*2*3*4+5+6-7+8+9", "1*2*3+4+5+6+7+8+9", "1*2*3+4+5-6*7+8*9",
        "1*2*3+4-5*6+7*8+9", "1*2*3+4-5*6-7+8*9", "1*2*3-4*5+6*7+8+9", "1*2*3-4*5-6+7*8+9",
        "1*2*3-4*5-6-7+8*9", "1*2*3-45+67+8+9", "1*2*34+56-7-8*9", "1*2*34-5+6-7-8-9", "1*2+3*4-56+78+9",
        "1*2+3+4+5*6+7+8-9", "1*2+3+4-5+6*7+8-9", "1*2+3+4-5-6+7*8-9", "1*2+3+45+67-8*9", "1*2+3-45+6+7+8*9",
        "1*2+34+5-6-7+8+9", "1*2+34+56-7*8+9", "1*2+34-5+6+7-8+9", "1*2+34-56+7*8+9", "1*2+34-56-7+8*9",
        "1*2-3*4+5+67-8-9", "1*2-3+4-5-6*7+89", "1*2-3-4*5+67+8-9", "1*2-3-4+56-7-8+9", "1*2-34+5*6+7*8-9",
        "1*23+4*5-6+7-8+9", "1*23-4-56-7+89", "1+2*3*4*5+6+7-89", "1+2*3*4+5*6+7-8-9", "1+2*3*4-5+6*7-8-9",
        "1+2*3+4*5*6+7-89", "1+2*3+4*5-6+7+8+9", "1+2*3-4-5-6*7+89", "1+2*34-5*6+7+8-9", "1+2+3*4*5+6-7-8-9",
        "1+2+3*4+5+6*7-8-9", "1+2+3*45-6-78-9", "1+2+3+4+5+6+7+8+9", "1+2+3+4+5-6*7+8*9", "1+2+3+4-5*6+7*8+9",
        "1+2+3+4-5*6-7+8*9", "1+2+3-4*5+6*7+8+9", "1+2+3-4*5-6+7*8+9", "1+2+3-4*5-6-7+8*9", "1+2+3-45+67+8+9",
        "1+2-3*4*5+6+7+89", "1+2-3*4+5*6+7+8+9", "1+2-3*4-5+6*7+8+9", "1+2-3*4-5-6+7*8+9",
        "1+2-3*4-5-6-7+8*9", "1+2-3+4*5+6*7-8-9", "1+2-3+45+6-7-8+9", "1+2-3+45-6+7+8-9", "1+2-3-4-5*6+7+8*9",
        "1+2-3-45-6+7+89", "1+2-34+5+6+7*8+9", "1+2-34+5+6-7+8*9", "1+2-34-5-6+78+9", "1+23*4+5-6-7*8+9",
        "1+23*4-5-6*7+8-9", "1+23*4-56+7-8+9", "1+23+4+5+6+7+8-9", "1+23+4-5*6+7*8-9", "1+23+4-5-67+89",
        "1+23-4*5+6*7+8-9", "1+23-4*5-6+7*8-9", "1+23-4-5+6+7+8+9", "1+23-4-5-6*7+8*9", "1+23-45+67+8-9",
        "1-2*3*4+5-6+78-9", "1-2*3*4-5-6+7+8*9", "1-2*3+4*5+6+7+8+9", "1-2*3+4*5-6*7+8*9",
        "1-2*3+4+5+6*7+8-9", "1-2*3+4+5-6+7*8-9", "1-2*3+4+56+7-8-9", "1-2*3+45-67+8*9", "1-2*3-4+5*6+7+8+9",
        "1-2*3-4-5+6*7+8+9", "1-2*3-4-5-6+7*8+9", "1-2*3-4-5-6-7+8*9", "1-2*34+5*6-7+89", "1-2+3*4*5-6-7+8-9",
        "1-2+3+4-5*6+78-9", "1-2+3+45+6-7+8-9", "1-2+3-4*5-6+78-9", "1-2+3-45+6-7+89", "1-2-3*4+5+6+7*8-9",
        "1-2-3*4-5-6+78-9", "1-2-3+4-5+67-8-9", "1-2-3+45-6-7+8+9", "1-2-34+5+6+78-9", "1-2-34+56+7+8+9",
        "1-2-34-5+6+7+8*9", "1-23*4+5+6*7+89", "1-23+4*5-6*7+89", "1-23+4-5+67-8+9", "1-23+45-67+89",
        "1-23-4+5+67+8-9", "1-23-4-5-6-7+89", "12*3*4-5*6-78+9", "12*3+4+5+6-7-8+9", "12*3+4+5-6+7+8-9",
        "12*3-4-5-6+7+8+9", "12*3-4-56+78-9", "12+3*4+5+6-7+8+9", "12+3*45-6-7-89", "12+3+4-56-7+89",
        "12+3-4*5+67-8-9", "12+3-45+6+78-9", "12+34-5-6-7+8+9", "12-3*4*5+6+78+9", "12-3*4-5+67-8-9",
        "12-3+4*5+6-7+8+9", "12-3+4+56-7-8-9", "12-3-4+5*6-7+8+9", "12-3-4-56+7+89", "12-3-45-6+78+9");
    //  x(223434, 24, "2+2*3+4+3*4", "2+2*3+4*3+4");
  }

  private void x(long numExpr, int target, String... results) {
    var res = addOperators(Long.toString(numExpr), target);
    var exp = new HashSet<String>(arrayList(results));
    var got = new HashSet<String>(res);
    pr("Number:", numExpr, "Target:", target, INDENT, got);

    {
      var compare = new HashSet<String>(exp);
      compare.removeAll(got);
      if (!compare.isEmpty()) {
        pr("missing elements:", compare);
      }
    }
    {
      var compare = new HashSet<String>(got);
      compare.removeAll(exp);
      if (!compare.isEmpty()) {
        pr("extraneous elements:", compare);
      }

    }

    verify(got, exp);
  }

  private static String[] operNames = { "CONCAT", "MULT", "SUB", "ADD" };

  private static String dbOper(int oper) {
    return operNames[oper];
  }

  /* private */ static String dbOper(Collection<Integer> ops) {
    var sb = new StringBuilder("[ ");
    for (var x : ops) {
      sb.append(dbOper(x));
      sb.append(' ');
    }
    sb.append(']');
    return sb.toString();
  }

  /* private */ List<String> SLOWaddOperators(String num, int target) {
    var digitExprs = new Expr[num.length()];
    for (int i = 0; i < num.length(); i++)
      digitExprs[i] = DIGIT_EXP[num.charAt(i) - '0'];
    List<String> stringResults = new ArrayList<>();
    var sb = new StringBuilder();

    int operCount = num.length() - 1;
    int operBitsMax = (1 << (operCount * 2));

    // Stacks for operations and arguments
    var args = new Expr[num.length()];
    var ops = new int[operCount];

    for (int operCodes = 0; operCodes < operBitsMax; operCodes++) {
      int cargs = 1; // argument stack size
      args[0] = digitExprs[0];

      int cops = 0; // operation stack size

      var accum = operCodes;
      for (int operIndex = 0;; operIndex++) {
        // The last iteration acts as if there is a very low precedence operation coming up,
        // so any stacked operations are evaluated
        var operator = Integer.MAX_VALUE;
        if (operIndex < operCount)
          operator = accum & 0x3;

        // If stacked operator (+ arguments) has higher precedence (i.e., a lower index), evaluate it
        while (cops != 0 && ops[cops - 1] <= operator) {
          var c = new Expr(ops[cops - 1], args[cargs - 2], args[cargs - 1]);
          cargs--;
          args[cargs - 1] = c;
          cops--;
        }
        if (operIndex == operCount)
          break;

        ops[cops++] = operator;
        args[cargs++] = digitExprs[operIndex + 1];
        accum >>= 2;
      }

      var expr = args[0];
      if (expr.evaluate() == target) {
        sb.setLength(0);
        expr.render(sb);
        stringResults.add(sb.toString());
      }
    }
    return stringResults;
  }

  // ------------------------------------------------------------------

  private Expr[] mDigitExprs;

  public List<String> addOperators(String num, int target) {
    if (false && alert("doing slow"))
      return SLOWaddOperators(num, target);

    mDigitExprs = new Expr[num.length()];
    for (int i = 0; i < num.length(); i++)
      mDigitExprs[i] = DIGIT_EXP[num.charAt(i) - '0'];

    pr("num:", num);
    // Generate suffixes
    int suffixPos = mDigitExprs.length - 1;
    var suffix = mDigitExprs[suffixPos];
    procSuffix(suffix, suffixPos);
    return SLOWaddOperators(num, target);
  }

  private void procSuffix(Expr suffixNode, int suffixPos) {
    pr("proc suffix at pos:", suffixPos, suffixNode);
    if (suffixPos != 0) {
      suffixPos--;
      procSuffix(new Expr(OPER_MULT, mDigitExprs[suffixPos], suffixNode), suffixPos);
      // If the old suffix is a concat, and is invalid, discard that value
      // since a leading zero will no longer be leading
      if (suffixNode.oper == OPER_CONCAT) {
        suffixNode.discardNaN();
        pr("after discarding nan:", suffixNode);
      }
      var x = new Expr(OPER_CONCAT, mDigitExprs[suffixPos], suffixNode);
      procSuffix(x, suffixPos);
    }
  }

  private static final int OPER_CONCAT = 0, OPER_MULT = 1, OPER_SUB = 2, OPER_ADD = 3;

  private static class Expr {

    public Expr(int operation, Expr a, Expr b) {
      oper = operation;
      child1 = a;
      child2 = b;
      debugId = UNIQUE++;
    }

    public void discardNaN() {

      if (child1 == null || child2 == null)
        return;
      value = null;
      child1.discardNaN();
      child2.discardNaN();
      todo("do I need to discard NaN for children too?");
    }

    public void render(StringBuilder sb) {
      if (child1 == null) {
        evaluate();
        if (Double.isNaN(value))
          sb.append("**NaN**");
        else
          sb.append((char) ('0' + value.intValue()));
        return;
      }
      child1.render(sb);
      if (oper != OPER_CONCAT)
        sb.append("*-+".charAt(oper - 1));
      if (child2 != null)
        child2.render(sb);
    }

    public void renderForDebug(StringBuilder sb) {
      if (child1 == null) {
        var value = evaluateDebug(true);
        if (Double.isNaN(value))
          sb.append("**NaN**");
        else
          sb.append((char) ('0' + (int) value));
        return;
      }
      child1.renderForDebug(sb);
      if (oper != OPER_CONCAT)
        sb.append("*-+".charAt(oper - 1));
      if (child2 != null)
        child2.renderForDebug(sb);
    }

    private double evaluateConcat() {
      if (value != null)
        return value;
      digitCount = child1.digitCount + child2.digitCount;
      value = child1.evaluateConcat() * powers10[child2.digitCount] + child2.evaluate();
      return value;
    }

    public double evaluate() {
      if (value != null)
        return value;
      double val;
      switch (oper) {
      default: // OPER_CONCAT:
        //  If the left child is the digit 0, we shouldn't attempt a concatenation.
        //  Set the value to NaN so any expression using this will not be a solution.
        if (child1 == DIGIT_EXP[0]) {
          val = Double.NaN;
        } else {
          digitCount = child1.digitCount + child2.digitCount;
          val = child1.evaluateConcat() * powers10[child2.digitCount] + child2.evaluate();
        }
        break;
      case OPER_MULT:
        val = child1.evaluate() * child2.evaluate();
        break;
      case OPER_ADD:
        val = child1.evaluate() + child2.evaluate();
        break;
      case OPER_SUB:
        val = child1.evaluate() - child2.evaluate();
        break;
      }

      if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
        val = Double.NaN;
      value = val;
      return value;
    }

    public double evaluateDebug(boolean checkLeadingZero) {
      double val;
      switch (oper) {
      default: // OPER_CONCAT:
        //  If the left child is the digit 0, we shouldn't attempt a concatenation.
        //  Set the value to NaN so any expression using this will not be a solution.
        if (checkLeadingZero && child1 == DIGIT_EXP[0]) {
          val = Double.NaN;
        } else {
          if (child1 == null && child2 == null) {
            return value;
          }
          digitCount = child1.digitCount + child2.digitCount;
          val = child1.evaluateDebug(false) * powers10[child2.digitCount] + child2.evaluateDebug(false);
        }
        break;
      case OPER_MULT:
        val = child1.evaluateDebug(true) * child2.evaluateDebug(true);
        break;
      case OPER_ADD:
        val = child1.evaluateDebug(true) + child2.evaluateDebug(true);
        break;
      case OPER_SUB:
        val = child1.evaluateDebug(true) - child2.evaluateDebug(true);
        break;
      }
      if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
        val = Double.NaN;
      return val;
    }

    // Store the value as a double, so it has the precision to check for overflow, but
    // also so that we can set invalid values to 'NaN' and have such values naturally propagate
    // up to parent nodes
    Double value;
    Expr child1, child2;
    int oper;
    int digitCount;
    int debugId;

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append("{\"");
      renderForDebug(sb);
      sb.append("\" ");
      if (child1 != null) {
        var val = evaluateDebug(true);
        if (Double.isNaN(val))
          sb.append("**NaN**");
        else
          sb.append(val);
      }
      sb.append("id:" + debugId);
      sb.append(" }");
      return sb.toString();
    }

  }

  private static int UNIQUE = 100;
  private static final Expr[] DIGIT_EXP = new Expr[10];
  private static final double powers10[] = { 1, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10 };

  static {
    for (int i = 0; i < 10; i++) {
      var e = new Expr(OPER_CONCAT, null, null);
      e.digitCount = 1;
      e.value = (double) i;
      DIGIT_EXP[i] = e;
    }
  }

}
