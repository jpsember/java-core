package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class MaximumProductSubarray extends LeetCode {

  public static void main(String[] args) {
    new MaximumProductSubarray().run();
  }

  public void run() {
    x("[1,1,1,1,1,0,2,2,2,2,0,3,3,0,4]", 16);
    x("[2,3,-2,4]", 6);
    x("[-2,0,-1]", 0);
    x("[-5]", -5);
  }

  private void x(String a, int expected) {
    var nums = extractNums(a);

    db = nums.length < 20;

    Alg alg1 = new DP();

    pr(toStr(nums));
    var res = alg1.maxProduct(nums);
    pr(INDENT, res);

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxProduct(int[] nums);

  }

  // ------------------------------------------------------------------

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
