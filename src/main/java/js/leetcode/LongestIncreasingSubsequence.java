package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

/**
 * I think this is a dynamic programming problem, working from the end of the
 * list backward.
 * 
 * First attempt works, but runs out of time with long sequences.
 * 
 * I think the "min value" argument for the memo key is part of the problem...
 * there can be a lot of min values producing the same result
 *
 * Second attempt works and beats 72% of the other answers.
 */
public class LongestIncreasingSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestIncreasingSubsequence().run();
  }

  public void run() {
    x("[10,9,2,5,3,7,101,18]");
    x("[0,1,0,3,2,3]");
    x("[7,7,7,7,7,7,7]");
    int[] n = new int[2500];
    for (int i = 0; i < n.length; i++)
      n[i] = i + 1;
    x(n, n.length);
  }

  private void x(String s) {
    x(extractNums(s), -1);
  }

  private void x(int[] nums, int expected) {
    db = true;
    var res = lengthOfLIS(nums);
    pr("Result:", res);
    if (expected < 0)
      expected = new SLOW().lengthOfLIS(nums);
    verify(res, expected);
  }

  private class SLOW {
    public int lengthOfLIS(int[] nums) {
      mMemo.clear();
      return aux(nums, 0, Integer.MIN_VALUE);
    }

    private int aux(int[] nums, int start, int minValue) {
      long key = (((long) minValue) << 32) | start;
      int value = mMemo.getOrDefault(key, -1);
      if (value >= 0)
        return value;

      // Base case
      if (start == nums.length) {
        value = 0;
      } else {
        // Can we include the start value?
        int currentValue = nums[start];
        if (minValue <= currentValue) {
          value = 1 + aux(nums, start + 1, currentValue + 1);
        }

        // Determine result for omitting this value
        int alternateValue = aux(nums, start + 1, minValue);
        value = Math.max(value, alternateValue);
      }
      mMemo.put(key, value);
      return value;
    }

    private Map<Long, Integer> mMemo = new HashMap<>();

  }

  public int lengthOfLIS(int[] nums) {
    final int nn = nums.length;

    // We reuse the nums[] array to store the longest length in the upper 16 bits.

    for (int i = 0; i < nn; i++) {
      // Make all numbers positive (as we process them) to avoid problems with sign bit interfering with upper bits
      nums[i] += 10000;
      // Choose longest compatible subsequence before i to concatenate with nums[i]
      // ...this will produce an n^2 algorithm
      int val = nums[i];
      int c = 0;

      for (int j = 0; j < i; j++) {
        var numj = (nums[j] & 0xffff);
        if (numj < val) {
          var tblVal = nums[j] >> 16;
          if (tblVal > c)
            c = tblVal;
        }
      }
      nums[i] |= (1 + c) << 16;
    }
    var max = 0;
    for (var x : nums) {
      var len = x >> 16;
      max = max < len ? len : max;
    }
    return max;
  }

}
