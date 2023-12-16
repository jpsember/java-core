package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Ok, it works, but is again 5% of accepted solutions.
 * 
 * I think I need some memoization with recursive calls, based on the intuition
 * that +/- operations are lowest precedence and thus good 'splitting' points
 * 
 * How about this:
 * 
 * For input S=s1s2..sn:
 * 
 * Construct suffixes of length 1...n that have the structure:
 * 
 * d{OP}d{OP}d{OP}...{OP}d
 * 
 * Where OP is either concatenation or multiplication.
 * 
 * Observe that every nonempty input S will have a nonempty suffix of this form.
 * 
 * For a given suffix length y, we construct the set of possible suffixes. Then,
 * evaluating each of the suffixes, we look at the structure of the remaining
 * prefix. It has the form
 * 
 * d{OP}d{OP}...{OP}d
 * 
 * where OP can be concatenation, multiplication, +, or -.
 * 
 * The connector between the prefix and suffix must be either + or -. We
 * recursively solve for the prefix given the target derived from the operation
 * and the suffix. This returns a (possibly empty) set of valid prefixes. For
 * each of these, we concatenate the suffix and add it to the output.
 * 
 * We use memoization to speed up the recursive calls.
 * 
 * The reason why we can't include + and - in the suffixes is because the
 * precedence of those operations means we can't know the value of the suffix,
 * and hence what value to search for recursively.
 */
public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {

    y(1051, 106);
    y(105, 5);
    y(123, 6);
    y(232, 8);
    y(702, 2);
    y(735, 9);

    y(9999999999L, 1409865409);
    y(123456789, 45);
  }

  private void y(long numExpr, int target) {
    var res = addOperators(Long.toString(numExpr), target);
    var slow = SLOWaddOperators(Long.toString(numExpr), target);
    var exp = new HashSet<String>(slow);
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

  private List<String> SLOWaddOperators(String num, int target) {
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

  public List<String> addOperators(String num, int target) {
    memoMap.clear();
    mDigitExprs = new Expr[num.length()];
    for (int i = 0; i < num.length(); i++)
      mDigitExprs[i] = DIGIT_EXP[num.charAt(i) - '0'];
    return auxAddOperators(target, mDigitExprs.length);
  }

  private List<String> auxAddOperators(int target, int exprCount) {
    long key = (target & 0x7fffffff) | (((long) exprCount) << 32);
    var results = memoMap.get(key);
    if (results != null)
      return results;

    results = new ArrayList<>();
    double targetD = target;

    // Generate suffixes

    // We will generate n suffix sets, each set built on the last i digits.
    // In each suffix set, we have every possible way of combining the i digits
    // into concatenated groups.
    for (int suffixPos = exprCount - 1; suffixPos >= 0; suffixPos--) {
      var set = buildSuffixSet(suffixPos);

      // For each element of this set, recursively see if we can combine the prefix
      // digit sequence with an ADD or SUBTRACT sequence to reach the target.
      for (var suffixExpr : set) {
        var suffixValueD = suffixExpr.evaluate();
        String suffixString = null;
        if (suffixPos == 0) {
          if (suffixValueD == targetD) {
            suffixString = renderIfNec(suffixString, suffixExpr);
            results.add(suffixString);
          }
        } else {
          do {
            double targ1 = targetD - suffixValueD;
            if (!fitsWithinInt(targ1))
              break;
            var auxResults = auxAddOperators(mDigitExprs, suffixPos, (int) targ1);
            if (auxResults.isEmpty())
              break;
            suffixString = renderIfNec(suffixString, suffixExpr);
            for (var prefixStr : auxResults) {
              results.add(prefixStr + '+' + suffixString);
            }
          } while (false);

          do {
            var targ2 = targetD + suffixValueD;
            if (!fitsWithinInt(targ2))
              break;
            var auxResults = auxAddOperators(mDigitExprs, suffixPos, (int) targ2);
            if (auxResults.isEmpty())
              break;
            suffixString = renderIfNec(suffixString, suffixExpr);
            for (var prefixStr : auxResults) {
              results.add(prefixStr + '-' + suffixString);
            }
          } while (false);
        }
      }
    }
    memoMap.put(key, results);
    return results;
  }

  private String renderIfNec(String str, Expr expr) {
    if (str == null) {
      var sb = sbWork;
      sb.setLength(0);
      expr.render(sb);
      str = sb.toString();
    }
    return str;
  }

  private static boolean fitsWithinInt(double x) {
    return (x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE);
  }

  private List<Expr> buildSuffixSet(int exprIndex) {
    // There are d = (n-exprIndex) digits to examine.
    // There are k = (d-1) choices of whether to concatenate each pair of digits,
    // and we want to generate all 2^k such subsets.
    int exprEnd = mDigitExprs.length;
    int exprTotal = exprEnd - exprIndex;
    int choiceCount = exprTotal - 1;
    int setSize = 1 << choiceCount;
    var output = new ArrayList<Expr>(setSize);
    elementLoop: for (int i = 0; i < setSize; i++) {
      var bitFlags = i;
      Expr prevExpr = null;
      int mergeStart = 0;
      for (int j = 0; j <= choiceCount; j++, bitFlags >>= 1) {
        int mergeCursor = j + 1;
        if (j == choiceCount || ((bitFlags & 1) == 1)) {
          var newExpr = concatenateSequence(mergeStart + exprIndex, mergeCursor + exprIndex);
          // If this is not a legal concatenation sequence, skip this element of the set
          if (Double.isNaN(newExpr.evaluate2())) {
            continue elementLoop;
          }
          if (prevExpr != null)
            newExpr = new Expr(OPER_MULT, prevExpr, newExpr);
          prevExpr = newExpr;
          mergeStart = mergeCursor;
        }
      }
      if (!Double.isNaN(prevExpr.evaluate()))
        output.add(prevExpr);
    }
    return output;
  }

  private Expr concatenateSequence(int digitStart, int digitEnd) {
    // We must concatenate so that the leading digit is a leaf node, for leading zero detection to work 
    var expr = mDigitExprs[digitEnd - 1];
    for (int i = digitEnd - 2; i >= digitStart; i--)
      expr = new Expr(OPER_CONCAT, mDigitExprs[i], expr);
    return expr;
  }

  private List<String> auxAddOperators(Expr[] digitExprs, int digitTotal, int target) {
    List<String> stringResults = new ArrayList<>();
    var sb = new StringBuilder();

    int operCount = digitTotal - 1;
    int operBitsMax = (1 << (operCount * 2));

    // Stacks for operations and arguments
    var args = new Expr[digitTotal];
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

  private Expr[] mDigitExprs;
  private Map<Long, List<String>> memoMap = new HashMap<>();
  private StringBuilder sbWork = new StringBuilder();

  private static final int OPER_CONCAT = 0, OPER_MULT = 1, OPER_SUB = 2, OPER_ADD = 3;

  private static class Expr {

    public Expr(int operation, Expr a, Expr b) {
      oper = operation;
      child1 = a;
      child2 = b;
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

    public double evaluate() {
      if (value == null) {
        double val;
        switch (oper) {
        default: // OPER_CONCAT:
        {
          // Evaluate the children FIRST, so their digit counts are valid
          var v1 = child1.evaluate();
          var v2 = child2.evaluate();
          digitCount = child1.digitCount + child2.digitCount;
          val = v1 * powers10[child2.digitCount] + v2;
        }
          break;
        case OPER_MULT:
          val = child1.evaluate2() * child2.evaluate2();
          break;
        case OPER_ADD:
          val = child1.evaluate2() + child2.evaluate2();
          break;
        case OPER_SUB:
          val = child1.evaluate2() - child2.evaluate2();
          break;
        }

        if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
          val = Double.NaN;
        value = val;
      }
      return value;
    }

    // A special form of evaluate() that returns NaN if this is an OPER_CONCAT that has a leading zero.
    // This only works if the LEFT child is a the single leading digit!
    private double evaluate2() {
      evaluate();
      if (oper == OPER_CONCAT) {
        if (child2 != null && child1 == DIGIT_EXP[0])
          return Double.NaN;

      }
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
        evaluate();
        if (Double.isNaN(value))
          sb.append("**NaN**");
        else
          sb.append("i:" + value.intValue());
      }
      sb.append(" }");
      return sb.toString();
    }

  }

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
