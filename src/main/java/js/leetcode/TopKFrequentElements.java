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
    x("[5,6,6,7,7,7,7,8,8,8,8,9,9,9,9,9,9,9,9,9]", 2);

    x("[1,1,1,2,2,3]", 2);
    x("[1]", 1);
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

  public int[] topKFrequent(int[] nums, int k) {

    // Build a histogram 

    final int MIN_NUM = -10000;
    final int MAX_NUM = 10000;
    final int NUM_ORIGIN = -MIN_NUM;

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

    // Partition histogram around a pivot value.
    // Move *higher* frequency values to left of pivot, to simplify things.

    double pivotValue = ((d - k) * n) / (d * (float) d);
    db(VERT_SP, "partitioning; k:", k, "n:", n, "d:", d, "pivot:", pivotValue);

    int low = 0;
    int high = d - 1;
    while (low < high) {
      var fLow = hist[partition[low] + NUM_ORIGIN];
      db("hist[low ", low, "]=", fLow);
      if (fLow >= pivotValue) {
        db("...incr low");
        low++;
      } else {
        db("...swapping with high");
        var tmp = partition[low];
        partition[low] = partition[high];
        partition[high] = tmp;
        high--;
      }
    }
    db("partition now:", toStr(partition, 0, low), toStr(partition, low, d));

     
    if (low == k)  
      return Arrays.copyOfRange(partition,0,low) ;
    if(low > k) {
      
    var res = new int[1];
    res[0] = 3;
    return res;
  }

}
