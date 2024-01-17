package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class MaximumProductSubarray extends LeetCode {

  public static void main(String[] args) {
    new MaximumProductSubarray().run();
  }

  public void run() {
    x("[-2,1,1,1,0,2,2,-2,0]");
    x("[1,1,1,1,1,0,2,2,2,2,0,3,3,0,4]", 16);
    x("[2,3,-2,4]", 6);
    x("[-2,0,-1]", 0);
    x("[-5]", -5);
  }

  private void x(String a) {
    x(a, null);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);

    db = nums.length < 20;

    Alg alg1 = new DP();

    pr(toStr(nums));
    var res = alg1.maxProduct(nums);
    pr(INDENT, res);

    if (expected == null)
      expected = new Recursion().maxProduct(nums);

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
      var cells = new int[len][len];

      for (int y = 0; y < len; y++) {
        for (int x = y; x < len; x++) {
          var prev = 1;
          if (y > 0)
            prev = cells[y - 1][x - 1];
          cells[y][x] = prev * nums[x];
        }
      }

      db("max:");
      db(strTable(cells));

      var result = Integer.MIN_VALUE;
      for (int y = 0; y < len; y++) {
        var row = cells[y];
        for (int x = y; x < len; x++)
          result = result < row[x] ? row[x] : result;
      }
      return result;
    }

  }

}
