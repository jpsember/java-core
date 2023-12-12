package js.leetcode;

import static js.base.Tools.*;

/**
 * First and second attempts failed, first with memory, second with time.
 * 
 * Now thinking dynamic programming is unnecessary.
 * 
 * Ok, linear time solution is easy. Build a solution that goes from [0,...,k],
 * then look for other solutions by removing a single element from the front and
 * appending additional elements from the back until the target is reached.
 * Stop when the end of the array is reached.
 */
public class P209MinimumSizeSubarraySum extends LeetCode {

  public static void main(String[] args) {
    new P209MinimumSizeSubarraySum().run();
  }

  public void run() {
    x(11, "[1,2,3,4,5]");
    x(7, "[2,3,1,2,4,3]");
  }

  private void x(int target, String numsStr) {
    var nums = extractNums(numsStr);
    var result = minSubArrayLen(target, nums);
    pr("target:", target, "nums:", nums, "result:", result);
    var exp = slow(target, nums);
    verify(result, exp);
  }

  private int slow(int target, int[] nums) {
    var a0 = new int[nums.length];
    var a1 = new int[nums.length];

    for (int y = 0; y < nums.length; y++) {
      for (int x = 0; x < nums.length - y; x++) {
        var prevSum = 0;
        if (y > 0)
          prevSum = a0[x];
        var total = prevSum + nums[x + y];
        if (total >= target)
          return y + 1;
        a1[x] = total;
      }
      var tmp = a0;
      a0 = a1;
      a1 = tmp;
    }
    return 0;
  }

  public int minSubArrayLen(int target, int[] nums) {
    int a = 0;
    int b = 0;
    int sum = 0;
    while (sum < target && b < nums.length) {
      sum += nums[b];
      b++;
    }
    if (sum < target)
      return 0;
    int minLength = b - a;
    while (true) {
      sum -= nums[a];
      a++;
      while (sum < target && b < nums.length) {
        sum += nums[b];
        b++;
      }
      if (sum < target)
        break;
      minLength = Math.min(minLength, b - a);
    }
    return minLength;
  }
}
