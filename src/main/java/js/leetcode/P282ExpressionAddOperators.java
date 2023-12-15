package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {
    // x(123, 6, "1*2*3", "1+2+3");
    //x(232, 8, "2*3+2", "2+3*2");
    x(223434, 24, "2+2*3+4+3*4","2+2*3+4*3+4");
  }

  private void x(int num, int target, String... results) {
    var res = addOperators(Integer.toString(num), target);
    var exp = new HashSet<String>(arrayList(results));
    var got = new HashSet<String>(res);
    pr("Number:", num, "Target:", target, INDENT, got);
    verify(got, exp);
  }

  public List<String> slowAdOperators(String num, int target) {
    slowTarget = target;
    //  var results = new ArrayList<String>();
    var exprs = new ArrayList<Expr>();
    for (int i = 0; i < num.length(); i++) {
      exprs.add(DIGIT_EXP[num.charAt(i) - '0']);
    }
    pr(exprs);

    slowExprs = exprs;
    slowResults = new ArrayList<String>();

    int[] operCodes = new int[exprs.size() - 1];
    slowAux(operCodes, 0);

    return slowResults;
  }

  private List<Expr> slowExprs;
  private List<String> slowResults;
  private int slowTarget;

  private void slowAux(int[] codes, int cursor) {
    if (cursor == codes.length) {
      slowEvaluate(codes);
    } else {
      for (int i = OPER_CONCAT; i <= OPER_SUB; i++) {
        codes[cursor] = i;
        slowAux(codes, cursor + 1);
      }
    }
  }

  private void slowEvaluate(int[] operCodes) {
    var args = new Stack<Expr>();

    var ops = new Stack<Integer>();

    int orandCursor = 0;

    args.push(slowExprs.get(orandCursor++));

    for (var operator : operCodes) {
      while (ops.nonEmpty() && ops.peek() <= operator) {
        var b = args.pop();
        var a = args.pop();
        var oper = ops.pop();
        var c = applyOperation(oper, a, b);
        if (c == null) return;
        args.push(c);
      }
      ops.push(operator);
      args.push(slowExprs.get(orandCursor++));
    }
    while (ops.nonEmpty()) {
      var b = args.pop();
      var a = args.pop();
      var oper = ops.pop();
      var c = applyOperation(oper, a, b);
      if (c == null) return;
      args.push(c);
    }
    var finalArg = args.pop();
    if (finalArg.buildValue() && finalArg.evaluate() == slowTarget) {
      slowResults.add(finalArg.str);
    }

  }

  private Expr applyOperation(int oper, Expr a, Expr b) {
    Expr c;
    switch (oper) {
    case OPER_CONCAT:
      c = concat(a, b);
      break;
    case OPER_MULT:
      c = productExpr(a, b);
      break;
    case OPER_ADD:
      c = additionExpr(a, b);
      break;
    case OPER_SUB:
      c = subtractionExpr(a, b);
      break;
    default:
      throw badArg();
    }
    return c;
  }

  private Expr productExpr(Expr a, Expr b) {
    Expr p = new Expr();
    p.precedence = PRODUCT;
    p.oper = OPER_MULT;
    p.child1 = a;
    p.child2 = b;
    return p;
  }

  private Expr subtractionExpr(Expr a, Expr b) {
    Expr p = new Expr();
    p.precedence = ADDSUB;
    p.oper = OPER_SUB;
    p.child1 = a;
    p.child2 = b;
    return p;
  }

  private Expr additionExpr(Expr a, Expr b) {
    Expr p = new Expr();
    p.precedence = ADDSUB;
    p.oper = OPER_ADD;
    p.child1 = a;
    p.child2 = b;
    return p;
  }

  public List<String> addOperators(String num, int target) {
    return slowAdOperators(num, target);
  }

  private Expr concat(Expr left, Expr right) {
    Expr result = null;
    do {
      if (left.precedence != DIGITS || right.precedence != DIGITS)
        break;
      if (left.str.charAt(0) == '0')
        break;
      long combinedVal = left.value * powers10[left.str.length()] + right.value;
      if (!integer(combinedVal))
        break;
      result = new Expr();
      result.precedence = DIGITS;
      result.str = left.str + right.str;
      result.value = (int) combinedVal;
    } while (false);
    return result;
  }

  private static boolean integer(long val) {
    return val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE;
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

  private static final int DIGITS = 0, PRODUCT = 1, ADDSUB = 2;
  private static final int OPER_CONCAT = 0, OPER_MULT = 1, OPER_ADD = 2, OPER_SUB = 3;

  private static class Expr {
    int precedence;
    String str;
    Integer value;
    Expr child1, child2;
    int oper;
    boolean invalidValue;

    private static String[] name = { "DIG", " X ", "+/-", };

    @Override
    public String toString() {
      return "{\"" + str + "\" " + value + (invalidValue ? "! " : " ") + name[precedence] + "}";
    }

    public boolean buildValue() {
      evaluate();
      checkState(!invalidValue, "failed to build value for:", oper);
      return !invalidValue;
    }

    public int evaluate() {
      if (value != null)
        return value;
      pr("evaluating:", this, "oper:", oper);
      long vl = Long.MAX_VALUE;
      switch (oper) {
      default:
        throw new IllegalStateException("oper:" + oper);

      case OPER_CONCAT: {
        var left = child1;
        var right = child2;
        if (!left.buildValue())
          break;
        if (!right.buildValue())
          break;
        if (left.str.charAt(0) == '0')
          break;
        vl = left.evaluate() * powers10[left.str.length()] + right.evaluate();
        str = left.str + right.str;
      }
        break;
      case OPER_MULT: {
        var left = child1;
        var right = child2;
        if (!left.buildValue())
          break;
        if (!right.buildValue())
          break;
        vl = left.evaluate() * right.evaluate();
        str = left.str + "*" + right.str;
      }
        break;

      case OPER_ADD: {
        var left = child1;
        var right = child2;
        if (!left.buildValue())
          break;
        if (!right.buildValue())
          break;
        vl = left.evaluate() + right.evaluate();
        str = left.str + "+" + right.str;
      }
        break;
      case OPER_SUB: {
        var left = child1;
        var right = child2;
        if (!left.buildValue())
          break;
        if (!right.buildValue())
          break;
        vl = left.evaluate() - right.evaluate();
        str = left.str + "-" + right.str;
      }
        break;
      }
      if (integer(vl)) {
        value = (int) vl;
      } else {
        value = Integer.MIN_VALUE;
        invalidValue = true;
      }

      pr("...evaluated;", this);
      checkState(!invalidValue);
      return value;
    }
  }

  private static final Expr[] DIGIT_EXP;
  static {
    DIGIT_EXP = new Expr[10];
    for (int i = 0; i < 10; i++) {
      var e = new Expr();
      e.precedence = DIGITS;
      e.str = Character.toString('0' + i);
      e.value = i;
      DIGIT_EXP[i] = e;
    }
  }

}
