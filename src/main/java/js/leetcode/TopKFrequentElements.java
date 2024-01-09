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
    x("[3,0,1,0]", 1);
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
    int uniqueCount = 0;
    for (var x : nums) {
      int i = x + NUM_ORIGIN;
      var freq = ++hist[i];
      if (freq == 1) {
        partition[uniqueCount++] = x;
      }
    }

    var n = nums.length;
    return aux(k, n, hist, partition, 0, uniqueCount);
  }

  /**
   * 
   * @param k
   *          number of most frequent values to extract
   * @param n
   *          sum of frequencies of values in partition subarray
   * @param hist
   *          frequency histogram; frequency of value (n-NUM_ORIGIN)
   * @param partition
   *          array of unique values appearing in nums
   * @param pStart
   *          start of partition subarray
   * @param pEnd
   *          end of partition subarray
   * @return
   */
  private int[] aux(int k, int n, int[] hist, int[] partition, int pStart, int pEnd) {
    if (db) {
      int[] ff = new int[pEnd - pStart];
      for (int i = pStart; i < pEnd; i++)
        ff[i - pStart] = hist[partition[i] + NUM_ORIGIN];
      db(VERT_SP, "partition, k:", k, "n:", n, "array:", toStr(partition, pStart, pEnd), "freq:", toStr(ff));
    }
    int[] res;

    int pLen = pEnd - pStart;

    // Base case:
    if (pLen == 1) {
      res = new int[1];
      res[0] = partition[pStart];
    } else {

      // Partition histogram around a pivot value.
      // Move *higher* frequency values to left of pivot, to simplify things.
      var pivotValue = ((pLen - k) * n) / (pLen * (float) pLen);
      db("...pivot:", pivotValue);

      int leftCursor = pStart;
      int rightCursor = pEnd;
      int lowSum = 0;

      while (leftCursor < rightCursor) {
        var fLow = hist[partition[leftCursor] + NUM_ORIGIN];
        db("hist[low ", leftCursor, "]=", fLow);
        if (fLow >= pivotValue) {
          db("...advancing left");
          leftCursor++;
          lowSum += fLow;
        } else {
          db("...decrementing, then swapping right with left");
          rightCursor--;
          swapValues(partition, leftCursor, rightCursor);
        }
      }
      db("partition now:", toStr(partition, pStart, leftCursor), toStr(partition, leftCursor, pStart + pEnd),
          "low pop:", lowSum, "low:", leftCursor, "vs k:", k);

      checkInf(20);

      if (leftCursor == k) {
        // The left side contains exactly k elements, so we're done; return that subarray
        res = Arrays.copyOfRange(partition, pStart, leftCursor);
      } else if (leftCursor > k) {
        // The left side contains more then k elements, so recursively extract k from it
        res = aux(k, lowSum, hist, partition, pStart, leftCursor);
      } else {
        // The left side contains fewer than k elements.  Return those items PLUS the
        // results of a recursive call on the right side
        res = new int[k];

        int k2 = k - leftCursor;
        var remainder = aux(k2, n - lowSum, hist, partition, leftCursor, pEnd);
        System.arraycopy(remainder, 0, res, 0, k2);
        System.arraycopy(partition, pStart, res, k2, leftCursor);
      }
    }
    db("...returning:", toStr(res));
    return res;
  }

  private void swapValues(int[] a, int i, int j) {
    int tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
  }

}
