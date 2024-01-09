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
    x("[1]", 1);
    x("[5,6,6,7,7,7,7,8,8,8,8,9,9,9,9,9,9,9,9,9]", 2);
    x("[1,1,1,2,2,3]", 2);
  }

  private void x(String numsStr, int k) {
    var nums = extractNums(numsStr);
    db = nums.length < 30;
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

  private static final int MIN_NUM = -10000;
  private static final int MAX_NUM = 10000;
  private static final int NUM_ORIGIN = -MIN_NUM;

  public int[] topKFrequent(int[] nums, int k) {

    // Build a histogram 

    int[] hist = new int[MAX_NUM + 1 - MIN_NUM];
    int[] partition = new int[hist.length];
    int d = 0;
    for (var x : nums) {
      int i = x + NUM_ORIGIN;
      var freq = ++hist[i];
      if (freq == 1) {
        partition[d++] = x;
      }
    }

    var n = nums.length;
    return aux(hist, partition, 0, n, d, k);
  }

  private int[] aux(int[] hist, int[] partition, int pStart, int n, int d, int k) {

    // Base case:
    if (d == 1) {

      return Arrays.copyOfRange(partition, pStart, pStart + d);
    }

    // Partition histogram around a pivot value.
    // Move *higher* frequency values to left of pivot, to simplify things.
    double pivotValue = ((d - k) * n) / (d * (float) d);
    db(VERT_SP, "partitioning; k:", k, "n:", n, "d:", d, "pivot:", pivotValue);

    int low = pStart;
    int high = pStart + d - 1;
    int lowSum = 0;

    while (low < high) {

      var fLow = hist[partition[low] + NUM_ORIGIN];
      db("hist[low ", low, "]=", fLow);
      if (fLow >= pivotValue) {
        db("...incr low");
        low++;
        lowSum += fLow;
      } else {
        db("...swapping with high");
        var tmp = partition[low];
        partition[low] = partition[high];
        partition[high] = tmp;
        high--;
      }
    }
    db("partition now:", toStr(partition, pStart, low), toStr(partition, low, pStart + d), "low pop:", lowSum,
        "low:", low, "vs k:", k);

    checkInf(20);
    if (low == k)
      return Arrays.copyOfRange(partition, pStart, low);
    else if (low > k)
      return aux(hist, partition, pStart, lowSum, low - pStart, k);
    else {
      // We want to include the portion in the low side,
      // and recursively include some items from the high side
      var res = new int[k];

      int k2 = k - low;
      var remainder = aux(hist, partition, low, n - lowSum, d - (low - pStart), k2);
      System.arraycopy(remainder, 0, res, 0, k2);
      System.arraycopy(partition, pStart, res, k2, low);
      return res;
    }

  }

}
