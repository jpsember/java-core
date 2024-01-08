package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reworked to use a map to support logarithmic time searches for
 * insertion point.
 * 
 * I suspect the map operations are slowing things down, and a dynamic programming approach
 * that avoids maps might be faster.
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
    db(nums);

    // This is a map (key=>val) where key = nums[i], and the value is the 
    // length of the longest subsequence whose elements are <= val
    // 
    var subseqLenMap = new TreeMap<Integer, Integer>();

    for (var num : nums) {
      db(VERT_SP, "num:", num, INDENT, "map:", subseqLenMap);

      // Let (key => len) be the map entry with the highest key <= num.
      // If no such key exists, then store (num => 1) in the map.
      //
      // Otherwise, if key = num, do nothing (a sequence <= num already exists,
      // and therefore has a length >= 1).
      //
      // Otherwise, store (key => 1 + num) (extending the < num sequence by one).

      var newLen = 1;

      // Get portion of map whose keys are <= num
      var headMap = subseqLenMap.headMap(num + 1);
      if (!headMap.isEmpty()) {
        var key = headMap.lastKey();
        if (key == num)
          continue;
        newLen = headMap.get(key) + 1;
      }

      db("...storing:", num, "=>", newLen);
      subseqLenMap.put(num, newLen);

      // Delete any subsumed results.
      //
      // We want to delete all keys > num whose values are <= newLen.

      var subsumeStart = num + 1;
      var subsumeEnd = subsumeStart;
      for (var ent : subseqLenMap.tailMap(subsumeStart).entrySet()) {
        if (ent.getValue() > newLen)
          break;
        subsumeEnd = ent.getKey() + 1;
      }

      if (subsumeEnd > subsumeStart) {
        var subsumed = subseqLenMap.subMap(subsumeStart, subsumeEnd);
        db("...deleting subsumed:", subsumed);
        subsumed.clear();
      }
    }
    var max = 0;
    for (var x : subseqLenMap.values()) {
      max = max < x ? x : max;
    }
    return max;
  }

}
