package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {
    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]",    3394);
    x("[1,5]", 10);
    x("[3,1,5,8]", 167);
    x("[5,6,7,1,2,3,0,6,12,20]");

  }

  private void x(String a) {
    x(a, null);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);
    x(nums, expected);
  }

  private void x(int[] nums, Integer expected) {
    db = nums.length < 12;

    Alg alg1 = new RecursionMemo();

    pr(toStr(nums));
    var res = alg1.maxCoins(nums);
    pr(INDENT, res);

    if (expected == null) {
      db = false;
      expected = new Recursion().maxCoins(nums);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxCoins(int[] nums);

  }

  // ------------------------------------------------------------------

  class Recursion extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      return aux(nums, 0, nums.length);
    }

    private int aux(int[] nums, int start, int stop) {
      if (start == stop)
        return 0;

      pushIndent();
      db("aux", toStr(nums, start, stop));

      int bestAmt = -1;
      for (int i = start; i < stop; i++) {
        var amt = nums[i];
        db("candidate", i, amt);
        if (i > start) {
          db("mult left:", nums[i - 1], "*", amt);
          amt *= nums[i - 1];
        }
        if (i + 1 < stop) {
          db("mult right: * ", nums[i + 1]);
          amt *= nums[i + 1];
        }
        db("...pop amount", amt);

        var work = new int[stop - start - 1];
        int w = 0;
        for (int j = start; j < i; j++)
          work[w++] = nums[j];
        for (int j = i + 1; j < stop; j++)
          work[w++] = nums[j];

        amt += aux(work, 0, work.length);
        bestAmt = Math.max(amt, bestAmt);
      }
      db(INDENT, bestAmt);
      popIndent();
      return bestAmt;
    }

  }

  class RecursionMemo extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      var result = aux(nums, 0, nums.length);
      db("calls:", calls, "miss %:", (misses * 100.0) / calls);
      return result;
    }

    private StringBuilder sb = new StringBuilder();
    private int calls;
    private int misses;

    private int aux(int[] nums, int start, int stop) {
      if (start == stop)
        return 0;

      sb.setLength(0);
      for (int j = start; j < stop; j++) {
        sb.append(nums[j]);
        sb.append(' ');
      }
      var key = sb.toString();
      var result = mMemo.getOrDefault(key, -1);
      calls++;
      if (result < 0) {
        misses++;
        pushIndent();
        db("aux", toStr(nums, start, stop));

        int bestAmt = -1;
        for (int i = start; i < stop; i++) {
          var amt = nums[i];
          if (i > start) {
            amt *= nums[i - 1];
          }
          if (i + 1 < stop) {
            amt *= nums[i + 1];
          }

          var work = new int[stop - start - 1];
          int w = 0;
          for (int j = start; j < i; j++)
            work[w++] = nums[j];
          for (int j = i + 1; j < stop; j++)
            work[w++] = nums[j];

          amt += aux(work, 0, work.length);
          bestAmt = Math.max(amt, bestAmt);
        }
        db(INDENT, bestAmt);
        popIndent();
        result = bestAmt;
        mMemo.put(key, result);
      }
      return result;
    }

    private Map<String, Integer> mMemo = new HashMap<>();

  }

}
