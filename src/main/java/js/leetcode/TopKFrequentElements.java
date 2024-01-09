package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.data.DataUtil;

/**
 * My approach is not satisfying and has a bunch of special cases.
 * 
 * Simplified the algorithm to do a double loop over all possible swaps (igoring
 * multiple copies)
 */
public class TopKFrequentElements extends LeetCode {

  public static void main(String[] args) {
    new TopKFrequentElements().run();
  }

  public void run() {
    x("[1,1,1,2,2,3]", 2);
    x("[1]", 1);
  }

  private void x(String numsStr, int k) {
    var nums = extractNums(numsStr);
    db = nums.length < 20;
    db(toStr(nums), INDENT, "k=", k);
    var res = topKFrequent(nums, k);
    var expected = SLOWTopKFrequent(nums, k);
    verify(res, expected);
  }

  private int[] SLOWTopKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (var x : nums) {
      var count = freq.getOrDefault(x, 0);
      freq.put(x, 1 + count);
    }
    List<Integer> vals = new ArrayList<>(freq.keySet());
    vals.sort((a, b) -> -compareFreq(a, b, freq));
    var y = DataUtil.intArray(vals);
    return Arrays.copyOfRange(y, 0, k);
  }

  private int compareFreq(int a, int b, Map<Integer, Integer> freq) {
    var diff = freq.getOrDefault(a, 0) - freq.getOrDefault(b, 0);
    if (diff == 0)
      diff = a - b;
    return diff;

  }

  public int[] topKFrequent(int[] nums, int k) {
    var res = new int[1];
    res[0] = 3;
    return res;
  }

}
