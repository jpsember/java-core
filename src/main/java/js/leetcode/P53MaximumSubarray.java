package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

import js.json.JSList;

// 53. Maximum Subarray
//
// Key property is that the maximum subarray (subarray with maximum sum) must end at some element n_i;
// and such a subarray either consists of the single element [n_i], or a subarray [...n_(i-1)] + n_i,
// where the subarray ending at (i-1) is the maximum such subarray ending there.
//
// So we record the maximum subarray ending at each value, and choose the maximum.
//
// I tried and failed to get a divide-and-conquer approach working.
// Here is a counterexample that fails:
//
// (1,1,1,1,-1,2,-5,4)
//
// Dividing this into left and right halves, the right half (-1,2,-5,4) returns (4) as the best result,
// whereas the left result (1,1,1,1) can be appended to the (-1,2) to get the optimal sum 5.

public class P53MaximumSubarray {

  public static void main(String[] args) {
    new P53MaximumSubarray().run();
  }

  private void run() {
    //    go(-2, 1, -3, 4, -1, 2, 1, -5, 4);
    //    go(5, 4, -1, 7, 8);
    //    go(1);
    //    go(-1);
    //  go(-2, 1, -3, 4, -1, 2, 1, -5, 4);
    go(1, 1, 1, 1, -1, 2, -5, 4);

  }

  private void go(int... nums) {
    pr("nums:", nums, "max sum:", maxSubArray(nums));
  }

  public int maxSubArray(int[] nums) {
    if (true)
      return maxSubArray2(nums);
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

  private static class Result {
    int i0;
    int i1;
    int sum;

    Result(int i0, int i1, int sum) {
      this.i0 = i0;
      this.i1 = i1;
      this.sum = sum;
    }

    @Override
    public String toString() {
      return "<sum:" + sum + " " + i0 + ".." + i1 + ">";
    }

  }

  private static Result mergeResults(Result left, Result right) {
    return new Result(left.i0, right.i1, left.sum + right.sum);
  }

  public int maxSubArray2(int[] nums) {
    var res = auxMaxSubArray(nums, 0, nums.length);
    return res[0].sum;
  }

  private static String str(int i0, int i1, int[] nums) {
    return JSList.with(Arrays.copyOfRange(nums, i0, i1)).toString();
  }

  private static String str(Result[] results) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (var x : results) {
      sb.append(' ');
      sb.append(x.toString());
      sb.append(' ');
    }
    sb.append(']');
    return sb.toString();
  }

  private Result[] auxMaxSubArray(int[] nums, int i0, int i1) {
    pr("maxSubArray", str(i0, i1, nums));
    // Base case: a single element?
    if (i0 + 1 == i1) {
      Result[] res = new Result[3];
      var r = new Result(i0, i1, nums[i0]);
      res[0] = r;
      res[1] = r;
      res[2] = r;
      pr("base case:", str(res));
      return res;
    }

    // Subdivide array into two roughly equal halves
    int split = (i0 + i1) >> 1;
    var resLeft = auxMaxSubArray(nums, i0, split);
    var resRight = auxMaxSubArray(nums, split, i1);

    Result[] res = new Result[3];

    Result newMid = resLeft[1];
    var cand = resRight[2];
    if (cand.sum > newMid.sum)
      newMid = cand;

    Result newLeft = resLeft[0];
    Result newRight = resRight[1];

    // Consider the sum of the ..R]+[L... subarray
    var merged = mergeResults(resLeft[2], resRight[0]);

    if (merged.sum > newMid.sum) {
      newMid = merged;
    }

    // If the new middle expanded to the leftmost element is an improvement, use it
    {
      var oldLeft = resLeft[0];
      if (oldLeft.i0 < newMid.i0) {
//        checkState(oldLeft.i0 == newMid.i0 || oldLeft.i1 < newMid.i0);

        var sum = oldLeft.sum + newMid.sum;
        for (int k = oldLeft.i1; k < newMid.i0; k++)
          sum += nums[k];
        if (sum > oldLeft.sum) {
          newLeft = new Result(oldLeft.i0, newMid.i1, sum);
        }
      }
    }

    // If the new middle expanded to the rightmost element is an improvement, use it
    {
      var oldRight = resRight[1];
 if (oldRight.i1 > newMid.i1) {
//        if (!(oldRight.i1 == newMid.i1 || oldRight.i0 > newMid.i1)) {
//        pr("oldRight:",oldRight);
//        pr("newMid:",newMid);
//        halt();
//        }

        var sum = oldRight.sum + newMid.sum;
        for (int k = newMid.i1; k < oldRight.i0; k++)
          sum += nums[k];
        if (sum > oldRight.sum)
          newRight = new Result(newMid.i0, oldRight.i1, sum);
      }
    }

    res[0] = newLeft;
    res[1] = newMid;
    res[2] = newRight;
    pr("case for", str(i0, i1, nums), str(res));
    return res;
  }

}
