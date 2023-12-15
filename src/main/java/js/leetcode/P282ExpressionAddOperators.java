package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {
    // x(123, 6, "1*2*3", "1+2+3");
    x(232, 8, "2*3+2", "2+3*2");
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
    //    slowAux(target, results, exprs.get(0), exprs, 1);
    //    return results;
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
    int otorCursor = 0;

    args.push(slowExprs.get(orandCursor++));

    for (var operator : operCodes) {
      while (ops.nonEmpty() && ops.peek() <= operator) {
        var b = args.pop();
        var a = args.pop();
        var oper = ops.pop();
        var c = applyOperation(oper, a, b);
        //        Expr c;
        //        switch (oper) {
        //        case OPER_CONCAT:
        //          c = concat(a, b);
        //          break;
        //        case OPER_MULT:
        //          c = productExpr(a, b);
        //          break;
        //        case OPER_ADD:
        //          c = additionExpr(a, b);
        //          break;
        //        case OPER_SUB:
        //          c = subtractionExpr(a, b);
        //          break;
        //        default:
        //          throw badArg();
        //        }
        args.push(c);
      }
      ops.push(operator);
      args.push(slowExprs.get(orandCursor++));
      //       
      //      var operand = slowExprs.get(i);
      //      operandStack.add(operand);
      //      int operCode = operCodes[i];

    }
    checkState(ops.size() <= 2);
    while (ops.nonEmpty()) {
      var b = args.pop();
      var a = args.pop();
      var oper = ops.pop();
      var c = applyOperation(oper, a, b);
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

  private void slowAux(int target, List<String> results, Expr expr, List<Expr> exprs, int cursor) {
    if (cursor == exprs.size()) {
      if (expr.buildValue() && expr.evaluate() == target) {
        results.add(expr.str);
      }
      return;
    }

    var expr2 = exprs.get(cursor);

    var combined = concat(expr, expr2);
    if (combined != null)
      slowAux(target, results, combined, exprs, cursor + 1);

    combined = productExpr(expr, expr2);
    combined.evaluate();
    pr("built product expr:", combined);
    slowAux(target, results, combined, exprs, cursor + 1);

    combined = additionExpr(expr, expr2);
    //new Expr(ADDSUB, expr.str + "+" + expr2.str, 0);
    slowAux(target, results, combined, exprs, cursor + 1);
    combined = subtractionExpr(expr, expr2);
    //new Expr(ADDSUB, expr.str + "-" + expr2.str, 0);
    slowAux(target, results, combined, exprs, cursor + 1);
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

  private static List<Expr> addResult(List<Expr> list, Expr expr) {
    if (list == null)
      list = new ArrayList<>();
    list.add(expr);
    return list;
  }

  private static boolean nonEmpty(List<Expr> list) {
    return list != null && !list.isEmpty();
  }

  private static List<Expr> addResults(List<Expr> list, List<Expr> exprs) {
    if (nonEmpty(list)) {
      if (list == null)
        list = new ArrayList<>();
      list.addAll(exprs);
    }
    return list;
  }

  private static long buildKey(int targetValue, int cursorStart, int cursorEnd, int precedence) {
    // cursor start: 4
    // cursor end:   4
    // precedence:   2
    // target:       32
    // 
    long key = cursorStart | (cursorEnd << 4) | (precedence << (4 + 4)) | (targetValue << (4 + 4 + 2));
    return key;
  }

  private Map<Long, List<Expr>> mExprMap = new HashMap<>();

  //  /**
  //   * Search for a combination of expressions that equate to a target value
  //   */
  //  private List<Expr> aux(int target, int cursorStart, int cursorEnd, int precedence, List<Expr> exprs) {
  //    db("aux target:", target, "cursor:", cursorStart, "..", cursorEnd, "prec:", precedence);
  //
  //    var key = buildKey(target, cursorStart, cursorEnd, precedence);
  //
  //    List<Expr> results = mExprMap.get(key);
  //    if (results != null)
  //      return results;
  //
  //    int size = cursorEnd - cursorStart;
  //
  //    db("exprs:", exprs.subList(cursorStart, cursorEnd));
  //
  //    var exp = exprs.get(cursorEnd - 1);
  //
  //    // Base case: only a single expr
  //    if (size == 1) {
  //      db("...final:", exp);
  //      if (exp.value == target) {
  //        db("......got solution!", exp.str);
  //        results = addResult(results, exp);
  //      }
  //    } else {
  //      // Consider concatenating the rightmost two digits together
  //      var expNext = exprs.get(cursorEnd - 2);
  //      var concat = concat(expNext, exp);
  //      // Try multiplying the first two together
  //
  //      var expNext = exprs.get(cursor + 1);
  //
  //      do {
  //        long combinedVal = exp.value * (long) expNext.value;
  //        if (!integer(combinedVal))
  //          break;
  //        var combined = new Expr(PRODUCT, exp.str + "*" + expNext.str, (int) combinedVal);
  //
  //        results = addResults(results, aux(target, combined, rightExprs, cursor + 1));
  //      } while (false);
  //
  //      // Can we concatenate two strings of digits together?
  //      do {
  //        if (exp.precedence != DIGITS)
  //          break;
  //        if (exp.str.charAt(0) == '0')
  //          break;
  //        long combinedVal = exp.value * powers10[expNext.str.length()] + expNext.value;
  //        if (!integer(combinedVal))
  //          break;
  //        var combined = new Expr(DIGITS, exp.str + expNext.str, (int) combinedVal);
  //        results = addResults(results, aux(target, combined, rightExprs, cursor + 1));
  //      } while (false);
  //
  //      // Can we multiply two values together?
  //      do {
  //        if (exp.precedence >= ADDSUB || expNext.precedence >= ADDSUB)
  //          break;
  //        long combinedVal = exp.value * (long) expNext.value;
  //        if (!integer(combinedVal))
  //          break;
  //        var combined = new Expr(PRODUCT, exp.str + "*" + expNext.str, (int) combinedVal);
  //        results = addResults(results, aux(target, combined, rightExprs, cursor + 1));
  //      } while (false);
  //
  //      // Can we add or subtract two values together?
  //
  //      do {
  //        long auxTarget = target - (long) exp.value;
  //        if (!integer(auxTarget))
  //          break;
  //        List<Expr> auxResults = aux((int) auxTarget, expNext, rightExprs, cursor + 1);
  //        if (nonEmpty(auxResults)) {
  //          for (var aux : auxResults) {
  //            var combined = new Expr(ADDSUB, exp.str + "+" + aux, target);
  //            results = addResult(results, combined);
  //          }
  //        }
  //      } while (false);
  //
  //      do {
  //        long auxTarget = target + (long) exp.value;
  //        if (!integer(auxTarget))
  //          break;
  //        List<Expr> auxResults = aux((int) auxTarget, expNext, rightExprs, cursor + 1);
  //        if (nonEmpty(auxResults)) {
  //          for (var aux : auxResults) {
  //            var combined = new Expr(ADDSUB, exp.str + "-" + aux, target);
  //            results = addResult(results, combined);
  //          }
  //        }
  //      } while (false);
  //
  //      //    
  //      //    do {
  //      //      long combinedVal = exp.value - (long) expNext.value;
  //      //      if (!integer(combinedVal))
  //      //        break;
  //      //      var combined = new Expr(ADDSUB, exp.str + "-" + expNext.str, (int) combinedVal);
  //      //      aux(target, combined, rightExprs, cursor + 1);
  //      //    } while (false);
  //      return results;
  //    }
  //  }

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
  private static final int OPER_NONE = 0, OPER_CONCAT = 1, OPER_MULT = 2, OPER_ADD = 3, OPER_SUB = 4;

  private static class Expr {
    int precedence;
    String str;
    Integer value;
    Expr child1, child2;
    int oper;
    boolean invalidValue;

    //    Expr(int precedence, String str, int value) {
    //      this.precedence = precedence;
    //      this.str = str;
    //      this.value = value;
    //    }

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
