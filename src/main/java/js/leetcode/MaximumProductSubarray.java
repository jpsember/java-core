package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MaximumProductSubarray extends LeetCode {

  public static void main(String[] args) {
    new MaximumProductSubarray().run();
  }

  public void run() {
    x("[-5]", -5);
    x("[-2,1,1,1,0,2,2,-2,0]");
    x("[1,1,1,1,1,0,2,2,2,2,0,3,3,0,4]", 16);
    x("[2,3,-2,4]", 6);
    x("[-2,0,-1]", 0);

  }

  private void x(String a) {
    x(a, null);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);

    db = nums.length < 20;

    Alg alg1 = new Linear();

    pr(toStr(nums));
    var res = alg1.maxProduct(nums);
    pr(INDENT, res);

    if (expected == null) {
      db = false;
      expected = new Recursion().maxProduct(nums);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxProduct(int[] nums);

  }

  // ------------------------------------------------------------------

  class Recursion extends Alg {

    @Override
    public int maxProduct(int[] nums) {
      mMemo.clear();
      int val = aux(nums, 0, true);
      for (int c = 1; c < nums.length; c++)
        val = Math.max(val, aux(nums, c, true));
      return val;
    }

    /**
     * Determine maximal subarray product that starts at cursor position
     * 
     * @param nums
     * @param cursor
     * @param max
     *          true to return maximum value, false for minimum
     */
    private int aux(int[] nums, int cursor, boolean max) {
      var firstValue = nums[cursor];
      if (cursor == nums.length - 1)
        return firstValue;

      // Use a string key for debug purposes (it is quite slow)
      var key = "" + cursor + " " + (max ? "+++" : "---");
      var resultObj = mMemo.get(key);
      if (resultObj != null) {
        db("...found value in memo");
        return resultObj;
      }

      pushIndent();
      db("aux", toStr(nums, cursor, nums.length));

      // We want to flip the 'max' flag for recursive calls if the first value is negative

      var combined = firstValue * aux(nums, cursor + 1, max ^ (firstValue <= 0));
      var result = max ? Math.max(firstValue, combined) : Math.min(firstValue, combined);

      popIndent();
      mMemo.put(key, result);
      db("...", key, "=>", result);

      return result;
    }

    private Map<String, Integer> mMemo = new HashMap<>();
  }

  class DP extends Alg {

    @Override
    public int maxProduct(int[] nums) {

      // There isn't really any decision being made... we are using the table
      // to extend every sequence to its maximum length, then looking through the
      // table at the end to get the maximum value prefix

      var len = nums.length;

      var rowU = new int[len + 1];
      var rowV = new int[len + 1];
      Arrays.fill(rowV, 1);

      var result = Integer.MIN_VALUE;
      for (int y = 0; y < len; y++) {
        for (int x = y; x < len; x++) {
          var prev = rowV[x];
          var nv = prev * nums[x];
          rowU[x + 1] = nv;
          result = result < nv ? nv : result;
        }
        var tmp = rowU;
        rowU = rowV;
        rowV = tmp;
      }

      return result;
    }

  }

  class Linear extends Alg {

    @Override
    public int maxProduct(int[] nums) {

      int result = Integer.MIN_VALUE;
      boolean hasZero = false;

      // Look for maximal subarrays that don't contain a zero
      int prevZeroPos = -1;
      for (int i = 0; i <= nums.length; i++) {
        var x = 0;
        if (i < nums.length) {
          x = nums[i];
          if (x == 0)
            hasZero = true;
        }
        if (x == 0) {
          if (i - prevZeroPos > 1) {
            var product = auxMaxOfNonZeroSubArray(nums, prevZeroPos + 1, i);
            result = result > product ? result : product;
          }
          prevZeroPos = i;
        }
      }
      if (hasZero && result < 0)
        return 0;
      return result;
    }

    private int product(int[] nums, int start, int end) {
      int p = nums[start];
      for (int i = start + 1; i < end; i++)
        p *= nums[i];
      return p;
    }

    private int auxMaxOfNonZeroSubArray(int[] nums, int start, int end) {
      // If single value, no splitting possible
      if (end - start == 1)
        return product(nums, start, end);

      int negCount = 0;
      int firstNeg = -1;
      int lastNeg = -1;

      for (int i = start; i < end; i++) {
        if (nums[i] < 0) {
          negCount++;
          if (firstNeg < 0)
            firstNeg = i;
          lastNeg = i;
        }
      }

      if ((negCount & 1) == 0) {
        return product(nums, start, end);
      } else {
        // Choose maximum of values to right of first negative, or left of last negative
        var left = Integer.MIN_VALUE;
        if (firstNeg + 1 != end)
          left = product(nums, firstNeg + 1, end);
        var right = Integer.MIN_VALUE;
        if (lastNeg != start)
          right = product(nums, start, lastNeg);
        return left > right ? left : right;
      }
    }
  }
}
