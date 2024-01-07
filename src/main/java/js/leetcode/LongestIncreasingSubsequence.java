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
    db = nums.length < 20;
    var res = lengthOfLIS(nums);
    pr("Result:", res);
    if (expected < 0)
      expected = new SLOW().lengthOfLIS(nums);
    verify(res, expected);
  }

  //------------------------------

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

    // This is a map whose key is a nums[] element, and the value is the 
    // length of the longest subsequence whose last element is <= key
    // 
    var bestResultsMap = new TreeMap<Integer, Integer>();

    db(nums);
    for (int i = 0; i < nn; i++) {
      int val = nums[i];
      db(VERT_SP, "value:", val);
      db("...map:", bestResultsMap);
      int cCurr = bestResultsMap.getOrDefault(val, 1);
      db("...seq len <=", val, ":", cCurr);

      var headMap = bestResultsMap.headMap(val);
      int cPrev = 0;
      if (!headMap.isEmpty()) {
        var key = headMap.lastKey();
        cPrev = bestResultsMap.get(key);
        db("...seq len <= predecessor", key, ":", cPrev);
      }

      var cNew = Math.max(cCurr, 1 + cPrev);
      db("...storing:", val, "=>", cNew);
      bestResultsMap.put(val, cNew);

      // Delete any subsumed results
      var subsumeEnd = val;
      var tailMap = bestResultsMap.tailMap(val + 1);
      for (var ent : tailMap.entrySet()) {
        if (ent.getValue() > cNew)
          break;
        subsumeEnd = ent.getKey();
      }
      if (subsumeEnd > val) {
        var subsumed = bestResultsMap.subMap(val + 1, subsumeEnd + 1);
        db("...deleting subsumed:", subsumed);
        subsumed.clear();
      }
    }
    var max = 0;
    for (var x : bestResultsMap.values()) {
      max = max < x ? x : max;
    }
    return max;
  }

}
