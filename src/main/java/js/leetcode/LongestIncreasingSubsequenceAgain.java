package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LongestIncreasingSubsequenceAgain extends LeetCode {

  public static void main(String[] args) {
    new LongestIncreasingSubsequenceAgain().run();
  }

  public void run() {
    x("[5,6,1,7]", 3);
    x("[-5]", 1);
    x("[-2,1,1,1,0,2,2,-2,0]", 3);
    x("[1,1,1,1,1,0,2,2,2,2,0,3,3,0,4]");
    x("[-2,0,-1]", 2);
    x(1965, 100);
  }

  private void x(int seed, int count) {
    int[] nums = new int[count];
    rand(seed);
    for (int i = 0; i < count; i++)
      nums[i] = rand().nextInt(20001) - 10000;
    x(nums, null);
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

    Alg alg1 = new DP();

    pr(toStr(nums));
    var res = alg1.lengthOfLIS(nums);
    pr(INDENT, res);

    if (expected == null) {
      db = false;
      expected = new Recursion().lengthOfLIS(nums);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int lengthOfLIS(int[] nums);

  }

  // ------------------------------------------------------------------

  class Recursion extends Alg {

    @Override
    public int lengthOfLIS(int[] nums) {
      mMemo.clear();

      return aux(nums, 0, Integer.MIN_VALUE);
    }

    private int aux(int[] nums, int cursor, int prevValue) {
      if (cursor == nums.length)
        return 0;

      var firstValue = nums[cursor];

      // Use a string key for debug purposes (it is quite slow)
      var key = "" + cursor + " " + prevValue;
      var resultObj = mMemo.get(key);
      if (resultObj != null) {
        db("...found value in memo");
        return resultObj;
      }

      pushIndent();
      db("aux", toStr(nums, cursor, nums.length), "p", prevValue);

      // Calculate result for skipping cursor value
      int result = aux(nums, cursor + 1, prevValue);
      if (firstValue > prevValue)
        result = Math.max(result, 1 + aux(nums, cursor + 1, firstValue));

      popIndent();
      mMemo.put(key, result);
      db("...", key, "=>", result);

      return result;
    }

    private Map<String, Integer> mMemo = new HashMap<>();
  }

  class DP extends Alg {

    public int lengthOfLIS(int[] nums) {

      // The grid has columns = position of cursor within nums array;
      //                 rows = length of subsequence
      //
      // and its cell value represents the minimum value of subsequence element[row_num] 
      //
      var minVal = Integer.MIN_VALUE;
      var maxVal = Integer.MAX_VALUE;
      var glen = nums.length + 1;

      var g = new int[glen][glen];

      for (var row : g)
        Arrays.fill(row, maxVal);

      // The initial state is an empty subsequence, with the cursor at zero
      g[0][0] = minVal;

      var result = 0;

      for (int y = 1; y < glen; y++) {
        for (int x = y; x < glen; x++) {
          var cursorNum = nums[x - 1];
          var prev = g[y - 1][x - 1];

          // Is cursor number greater than previous value added? If so, we can 
          // append this character; expand up and to the right
          if (cursorNum > prev) {
            result = y;
            update(g, x, y, cursorNum);
          }

          // Take branch for *not* using the cursor number.
          update(g, x, y - 1, prev);
        }
        if (db)
          db("y:", y, INDENT, strTable(g));
      }

      return result;
    }

    private void update(int[][] grid, int x, int y, int value) {
      if (value < grid[y][x])
        grid[y][x] = value;
    }
  }

}
