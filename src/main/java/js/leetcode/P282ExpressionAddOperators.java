package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {
    x(123, 6, "1*2*3", "1+2+3");
    //x(232, 8, "2*3+2", "2+3*2");
    //x(702, 2, "7*0+2");
    xx(735, 9, "7-3+5");

    x(9999999999L, 1409865409);
    x(123456789, 45, "1*2*3*4*5-6-78+9", "1*2*3*4+5+6-7+8+9", "1*2*3+4+5+6+7+8+9", "1*2*3+4+5-6*7+8*9",
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

  // ------------------------------------------------------------------

  public List<String> addOperators(String num, int target) {
    var digitExprList = new Expr[num.length()];
    for (int i = 0; i < num.length(); i++)
      digitExprList[i] = DIGIT_EXP[num.charAt(i) - '0'];

    List<String> stringResults = new ArrayList<>();
    var sb = new StringBuilder();

    int operCount = num.length() - 1;
    int operBitsMax = (1 << (operCount * 2));

    Stack<Expr> args = new Stack<>();
    Stack<Integer> ops = new Stack<>();

    for (int operCodes = 0; operCodes < operBitsMax; operCodes++) {
      int exprCursor = 0;
      args.clear();
      ops.clear();
      args.push(digitExprList[exprCursor++]);

      var accum = operCodes;
      for (int operIndex = 0; operIndex < operCount; operIndex++) {
        var operator = accum & 0x3;
        while (!ops.empty() && ops.peek() <= operator) {
          var b = args.pop();
          var a = args.pop();
          var oper = ops.pop();
          var c = new Expr(oper, a, b);
          args.push(c);
        }
        ops.push(operator);
        args.push(digitExprList[exprCursor++]);
        accum >>= 2;
      }

      while (!ops.empty()) {
        var b = args.pop();
        var a = args.pop();
        var oper = ops.pop();
        var c = new Expr(oper, a, b);
        args.push(c);
      }
      var expr = args.pop();
      if (expr.evaluate() == target) {
        sb.setLength(0);
        expr.render(sb);
        stringResults.add(sb.toString());
      }
    }
    return stringResults;
  }

  private static final int OPER_CONCAT = 0, OPER_MULT = 1, OPER_SUB = 2, OPER_ADD = 3;

  private static class Expr {

    public Expr(int operation, Expr a, Expr b) {
      oper = operation;
      child1 = a;
      child2 = b;
    }

    private static final char operChars[] = { 0, '*', '-', '+', };

    public void render(StringBuilder sb) {
      if (child1 == null) {
        if (Double.isNaN(value))
          sb.append("**NaN**");
        else
          sb.append((char) ('0' + value.intValue()));
        return;
      }
      child1.render(sb);
      char c = operChars[oper];
      if (c != 0)
        sb.append(c);
      if (child2 != null)
        child2.render(sb);
    }

    public double evaluate() {
      if (value != null)
        return value;
      double val;
      switch (oper) {
      default:
        throw new IllegalStateException();
      case OPER_CONCAT:
        // If the left child is the digit 0, the value is that of the right child.
        // This avoids leading zeros.
        if (child1 == DIGIT_EXP[0]) {
          val = child2.evaluate();
          digitCount = child2.digitCount;
        } else {
          digitCount = child1.digitCount + child2.digitCount;
          val = child1.evaluate() * powers10[child2.digitCount] + child2.evaluate();
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

    // Store the value as a double, so it has the precision to check for overflow, but
    // also so that we can set invalid values to 'NaN' and have such values naturally propagate
    // up to parent nodes
    Double value;
    Expr child1, child2;
    int oper;
    int digitCount;

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append("{\"");
      render(sb);
      sb.append("\" ");
      if (child1 != null) {
        if (Double.isNaN(value))
          sb.append("**NaN**");
        else
          sb.append(value);
      }
      sb.append(" }");
      return sb.toString();
    }

  }

  private static final Expr[] DIGIT_EXP = new Expr[10];
  private static final double powers10[] = { 1, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10 };

  static {
    for (int i = 0; i < 10; i++) {
      var e = new Expr(-1, null, null);
      e.digitCount = 1;
      e.value = (double) i;
      DIGIT_EXP[i] = e;
    }
  }

}
