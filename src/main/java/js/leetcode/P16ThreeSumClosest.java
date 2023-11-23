
package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

// Once you know the trick for P15, this is easily adapted
//
public class P16ThreeSumClosest {

  public static void main(String[] args) {
    new P16ThreeSumClosest().run();
  }

  private void run() {
    x(1, 4, 0, 5, -5, 3, 3, 0, -4, -5);
  }

  private void x(int target, int... nums) {
    var result = threeSumClosest(nums, target);
    pr(nums, "target:", target, "sum:", result);
  }

  public int threeSumClosest(int[] nums, int target) {
    Arrays.sort(nums);

    Integer bestDifference = null;
    int bestSum = 0;

    for (int i = 0; i < nums.length - 2; i++) {
      var a = nums[i];
      int j = i + 1;
      int b = nums[j];
      int k = nums.length - 1;
      int c = nums[k];
      while (j < k) {
        var sum = a + b + c;
        var diff = Math.abs(sum - target);

        if (bestDifference == null || bestDifference > diff) {
          bestDifference = diff;
          bestSum = sum;
        }
        if (sum < target) {
          b = nums[++j];
        } else {
          c = nums[--k];
        }
      }
    }

    return bestSum;
  }

}