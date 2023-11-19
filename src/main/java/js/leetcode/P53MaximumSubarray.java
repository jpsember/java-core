package js.leetcode;

import static js.base.Tools.*;

// 53. Maximum Subarray
//
// Key property is that the maximum subarray (subarray with maximum sum) must end at some element n_i;
// and such a subarray either consists of the single element [n_i], or a subarray [...n_(i-1)] + n_i,
// where the subarray ending at (i-1) is the maximum such subarray ending there.
//
// So we record the maximum subarray ending at each value, and choose the maximum.
//

public class P53MaximumSubarray {

  public static void main(String[] args) {
    new P53MaximumSubarray().run();
  }

  private void run() {
    go(-2, 1, -3, 4, -1, 2, 1, -5, 4);
    go(5, 4, -1, 7, 8);
    go(1);
    go(-1);
  }

  private void go(int... nums) {
    pr("nums:", nums, "max sum:", maxSubArray(nums));
  }

  public int maxSubArray(int[] nums) {
    int maxSum = 0; // The maximum subarray sum 
    int prevSum = 0; // The maximum subarray sum that ends at num_(i-1)
    int newSum; // The maximum subarray sum that ends at num_i
    boolean first = true;

    for (var v : nums) {
      newSum = max(prevSum + v, v);
      // If there is no previous maximum sum, always update it.
      if (first) {
        first = false;
        maxSum = newSum;
      }
      maxSum = max(maxSum, newSum);
      prevSum = newSum;
    }
    return maxSum;
  }
  
  // Using Math.max() adds a lot to the memory usage
  //
  private static int max(int a, int b) {
    return (a >= b) ? a : b;
  }

}
