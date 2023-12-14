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

  public List<String> addOperators(String num, int tar) {
    results = new ArrayList<String>();
    target = tar;

    var exprs = new ArrayList<Expr>();
    for (int i = 0; i < num.length(); i++) {
      exprs.add(new Expr(DIGITS, num.substring(i, i + 1), num.charAt(i) - '0'));
    }
    pr(exprs);

    aux(exprs.get(0), exprs, 1);
    return results;
  }

  private List<String> results;
  private int target;

  private void aux(Expr exp, List<Expr> exprs, int cursor) {
    int remain = exprs.size() - cursor;

    db("exp:", exp, "remaining:", exprs.subList(cursor, exprs.size()));

    // Base case: only a single expr
    if (remain == 0) {
      db("...final:", exp);
      if (exp.value == target) {
        db("......got solution!", exp.str);
        results.add(exp.str);
      }
      return;
    }

    var expNext = exprs.get(cursor);

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
      aux(combined, exprs, cursor + 1);
    } while (false);

    // Can we multiply two values together?
    do {
      if (exp.precedence >= ADDSUB || expNext.precedence >= ADDSUB)
        break;
      long combinedVal = exp.value * (long) expNext.value;
      if (!integer(combinedVal))
        break;
      var combined = new Expr(PRODUCT, exp.str + "*" + expNext.str, (int) combinedVal);
      aux(combined, exprs, cursor + 1);
    } while (false);

    // Can we add or subtract two values together?

    do {
      long combinedVal = exp.value + (long) expNext.value;
      if (!integer(combinedVal))
        break;
      var combined = new Expr(ADDSUB, exp.str + "+" + expNext.str, (int) combinedVal);
      aux(combined, exprs, cursor + 1);
    } while (false);
    do {
      long combinedVal = exp.value - (long) expNext.value;
      if (!integer(combinedVal))
        break;
      var combined = new Expr(ADDSUB, exp.str + "-" + expNext.str, (int) combinedVal);
      aux(combined, exprs, cursor + 1);
    } while (false);

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
