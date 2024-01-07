package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    x("[1,3,6,7,9,4,10,5,6]");

    x("[0,1,0,3,2,3]");
    x("[10,9,2,5,3,7,101,18]");
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

  //------------------------------

  public int NEWlengthOfLIS(int[] nums) {
    // This is a map whose key is a nums[] element, and the value is the 
    // length of the longest subsequence whose first element is that element
    // 
    var bestResultsMap = new TreeMap<Integer, Integer>();
    pr(nums);
    for (int i = nums.length - 1; i >= 0; i--) {
      int val = nums[i];
      pr(VERT_SP, "val:", val);

      // Let len0 length of longest subsequence in map whose first value = val (or 0)
      // Let len1 be key in map of longest subsequence whose first value > val (or 0)
      // Store (val, max(len0, 1+len1))
      int len0 = 0;
      int len1 = 0;

      len0 = bestResultsMap.getOrDefault(val, 0);

      var query = bestResultsMap.tailMap(val + 1);
      if (!query.isEmpty()) {
        len1 = query.get(query.firstKey());
      }

      var len = Math.max(len0, 1 + len1);

      pr("storing", val, len);
      // Store our result in the map
      bestResultsMap.put(val, len);

    }
    int best = 0;
    for (var x : bestResultsMap.values())
      best = x > best ? x : best;
    return best;
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
    
    // Length of longest subsequence whose last element is nums[i]
    var tbl = new int[nn];

    for (int i = 0; i < nn; i++) {

      // Choose longest compatible subsequence before i to concatenate with nums[i]
      // ...this will produce an n^2 algorithm
      int val = nums[i];
      int c = 0;

      for (int j = 0; j < i; j++) {
        var numj = nums[j];
        if (numj < val) {
          var tblVal = tbl[j];
          if (tblVal > c)
            c = tblVal;
        }
      }
      tbl[i] = 1 + c;
    }
    var max = 0;
    for (var x : tbl) {
      max = max < x ? x : max;
    }
    return max;
  }

}
