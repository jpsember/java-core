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

      var len = nums.length;
      var max = new int[len + 1][len + 1];
      var min = new int[len + 1][len + 1];

//      for (int y = 0; y < len; y++) {
//        for (int x = 0; x < len; x++) {
//          min[y][x] = Integer.MAX_VALUE;
//          max[y][x] = Integer.MIN_VALUE;
//        }
//      }
//
//      for (int x = 0; x < len; x++) {
//        min[0][x] = 1;
//        max[0][x] = 1;
//      }
      for (int y = 1; y <= len; y++) {
        for (int x = y; x <= len; x++) {
          var prev = 1;
          if (y > 1)
            prev = max[y-1][x-1];
          max[y][x] = prev * nums[x-1];
//          var p1 = min[y - 1][x - 1] * nums[x - 1];
//          var p2 = max[y - 1][x - 1] * nums[x - 1];
//          min[y][x] = Math.min(min[y][x], p1);
//          max[y][x] = Math.max(max[y][x], p2);
        }
      }

      db("min:");
      db(strTable(min));
      db("max:");
      db(strTable(max));

      var result = Integer.MIN_VALUE;
      for (int y = 1; y <= len; y++) {
        var row = max[y];
        for (int x = y; x <= len; x++)
          result = Math.max(result, row[x]);
      }
      return result;
    }

  }

}
