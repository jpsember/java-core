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
    x(232, 8, "2*3+2", "2+3*2");
  }

  private void x(int num, int target, String... results) {
    var res = addOperators(Integer.toString(num), target);
    var exp = new HashSet<String>(arrayList(results));
    var got = new HashSet<String>(res);
    pr("Number:", num, "Target:", target, INDENT, got);
    verify(got, exp);
  }

  public List<String> addOperators(String num, int target) {
//    results = new ArrayList<String>();

    var exprs = new ArrayList<Expr>();
    for (int i = 0; i < num.length(); i++) {
      exprs.add(new Expr(DIGITS, num.substring(i, i + 1), num.charAt(i) - '0'));
    }
    pr(exprs);

    var results = new ArrayList<String>();
    var solutions = aux(target, exprs.get(0), exprs, 1);
    if (solutions != null) {
      for (var s : solutions)
        results.add(s.str);
    }
    return results;
  }

  //  private List<String> results;

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

  private List<Expr> aux(int target, Expr exp, List<Expr> rightExprs, int cursor) {
    List<Expr> results = null;

    int remain = rightExprs.size() - cursor;

    db("exp:", exp, "remaining:", rightExprs.subList(cursor, rightExprs.size()));

    // Base case: only a single expr
    if (remain == 0) {
      db("...final:", exp);
      if (exp.value == target) {
        db("......got solution!", exp.str);
        return addResult(results, exp);
        //results.add(exp.str);
      }
      return results;
    }

    var expNext = rightExprs.get(cursor);

    // Can we concatenate two strings of digits together?
    do {
      if (exp.precedence != DIGITS)
        break;
      if (exp.str.charAt(0) == '0')
        break;
      long combinedVal = exp.value * powers10[expNext.str.length()] + expNext.value;
      if (!integer(combinedVal))
        break;
      var combined = new Expr(DIGITS, exp.str + expNext.str, (int) combinedVal);
      results = addResults(results, aux(target, combined, rightExprs, cursor + 1));
    } while (false);

    // Can we multiply two values together?
    do {
      if (exp.precedence >= ADDSUB || expNext.precedence >= ADDSUB)
        break;
      long combinedVal = exp.value * (long) expNext.value;
      if (!integer(combinedVal))
        break;
      var combined = new Expr(PRODUCT, exp.str + "*" + expNext.str, (int) combinedVal);
      results = addResults(results, aux(target, combined, rightExprs, cursor + 1));
    } while (false);

    // Can we add or subtract two values together?

    do {
      long auxTarget = target - (long) exp.value;
      if (!integer(auxTarget))
        break;
      List<Expr> auxResults = aux((int) auxTarget, expNext, rightExprs, cursor + 1);
      if (nonEmpty(auxResults)) {
        for (var aux : auxResults) {
          var combined = new Expr(ADDSUB, exp.str + "+" + aux, target);
          results = addResult(results, combined);
        }
      }
    } while (false);
    
    do {
      long auxTarget = target + (long) exp.value;
      if (!integer(auxTarget))
        break;
      List<Expr> auxResults = aux((int) auxTarget, expNext, rightExprs, cursor + 1);
      if (nonEmpty(auxResults)) {
        for (var aux : auxResults) {
          var combined = new Expr(ADDSUB, exp.str + "-" + aux, target);
          results = addResult(results, combined);
        }
      }
    } while (false);
    
//    
//    do {
//      long combinedVal = exp.value - (long) expNext.value;
//      if (!integer(combinedVal))
//        break;
//      var combined = new Expr(ADDSUB, exp.str + "-" + expNext.str, (int) combinedVal);
//      aux(target, combined, rightExprs, cursor + 1);
//    } while (false);
return results;
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

  private static class Expr {
    int precedence;
    String str;
    int value;

    Expr(int precedence, String str, int value) {
      this.precedence = precedence;
      this.str = str;
      this.value = value;
    }

    private static String[] name = { "DIG", " X ", "+/-", };

    @Override
    public String toString() {
      return "{\"" + str + "\" " + value + " " + name[precedence] + "}";
    }

  }

}
