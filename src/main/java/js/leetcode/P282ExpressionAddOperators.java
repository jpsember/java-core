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

  // ------------------------------------------------------------------

  public List<String> addOperators(String num, int target) {
    targetValue = target;
    var exprs = new ArrayList<Expr>();
    for (int i = 0; i < num.length(); i++)
      exprs.add(DIGIT_EXP[num.charAt(i) - '0']);

    exprList = exprs;
    results = new ArrayList<Expr>();

    // There is an operation between each adjacent pair of expressions
    int[] operCodes = new int[exprs.size() - 1];
    aux(operCodes, 0);

    List<String> stringResults = new ArrayList<>();
    {
      var sb = new StringBuilder();
      for (var exp : results) {
        sb.setLength(0);
        exp.render(sb);
        stringResults.add(sb.toString());
      }
    }
    return stringResults;
  }

  private List<Expr> exprList;
  //  private List<String> results;
  private List<Expr> results;
  private int targetValue;

  private Stack<Expr> args = new Stack<>();
  private Stack<Integer> ops = new Stack<>();

  private void aux(int[] codes, int cursor) {
    if (cursor < codes.length) {
      for (int i = OPER_CONCAT; i < OPER_TOTAL; i++) {
        codes[cursor] = i;
        aux(codes, cursor + 1);
      }
      return;
    }

    int exprCursor = 0;
    args.clear();
    ops.clear();
    args.push(exprList.get(exprCursor++));

    for (var operator : codes) {
      while (!ops.empty() && ops.peek() <= operator) {
        var b = args.pop();
        var a = args.pop();
        var oper = ops.pop();
        var c = new Expr(oper, a, b);
        args.push(c);
      }
      ops.push(operator);
      args.push(exprList.get(exprCursor++));
    }
    while (!ops.empty()) {
      var b = args.pop();
      var a = args.pop();
      var oper = ops.pop();
      var c = new Expr(oper, a, b);
      if (c == null)
        return;
      args.push(c);
    }
    var finalArg = args.pop();
    if (finalArg.evaluate() == targetValue)
      results.add(finalArg);

  }

  private static final long powers10[];
  static {
    powers10 = new long[10 + 1];
    long x = 1;
    for (int i = 0; i <= 10; i++) {
      powers10[i] = x;
      x *= 10;
    }
  }

  private static final int OPER_CONCAT = 0, OPER_MULT = 1, OPER_SUB = 2, OPER_ADD = 3, OPER_TOTAL = 4;

  private static class Expr {
    Double dvalue;
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
        if (Double.isNaN(dvalue))
          sb.append("**NaN**");
        else
          sb.append(dvalue);
      }
      sb.append(" }");
      return sb.toString();
    }

    public Expr(int operation, Expr a, Expr b) {
      oper = operation;
      child1 = a;
      child2 = b;
    }

    private static final char operChars[] = { 0, '*', '-', '+', };

    public void render(StringBuilder sb) {
      if (child1 == null) {
        if (Double.isNaN(dvalue))
          sb.append("**NaN**");
        else
          sb.append((char) ('0' + dvalue.intValue()));
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
      if (dvalue != null)
        return dvalue;

      double val;

      switch (oper) {
      default:
        throw new IllegalStateException();

      case OPER_CONCAT: {
        var left = child1;
        var right = child2;
        if (left == DIGIT_EXP[0]) {
          val = right.evaluate();
          digitCount = right.digitCount;
        } else {
          digitCount = left.digitCount + right.digitCount;
          val = left.evaluate() * powers10[right.digitCount] + right.evaluate();
        }
      }
        break;

      case OPER_MULT: {
        var left = child1;
        var right = child2;
        val = left.evaluate() * right.evaluate();
      }
        break;

      case OPER_ADD: {
        var left = child1;
        var right = child2;
        val = left.evaluate() + right.evaluate();
      }
        break;
      case OPER_SUB: {
        var left = child1;
        var right = child2;
        val = left.evaluate() - right.evaluate();
      }
        break;
      }
      //      if (integer(vl)) {
      //        value = (int) vl;
      //      } else {
      //        value = Integer.MIN_VALUE;
      //        invalidValue = true;
      //      }

      if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
        val = Double.NaN;
      dvalue = val;
      return dvalue;
    }

  }

  private static final Expr[] DIGIT_EXP;
  static {
    DIGIT_EXP = new Expr[10];
    for (int i = 0; i < 10; i++) {
      var e = new Expr(-1, null, null);
      e.digitCount = 1;
      e.dvalue = (double) i;
      DIGIT_EXP[i] = e;
    }
  }

}
