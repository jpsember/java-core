package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

/**
 * Works! Is very fast! Not quite as fast as some.
 * 
 * Now it is very fast. Doing a conventional sort of integers that are a
 * combination of (frequency | value).
 */
public class TopKFrequentElements extends LeetCode {

  public static void main(String[] args) {
    new TopKFrequentElements().run();
  }

  public void run() {
    x("[5,6,6,7,7,7,7,8,8,8,8,9,9,9,9,9,9,9,9,9]", 2);
    rand(123);
    xr(20, 20);
    x("[3,0,1,0]", 1);
    x("[1]", 1);
    x("[1,1,1,2,2,3]", 2);
    xr(100000, 50000);
  }

  private void xr(int n, int k) {
    int[] num = new int[n];
    for (int i = 0; i < n; i++)
      num[i] = rand().nextInt(2000) - 1000;
    x(num, k);
  }

  private void x(String numsStr, int k) {
    x(extractNums(numsStr), k);
  }

  private void x(int[] nums, int k) {
    db = nums.length < 30;
    db(toStr(nums), INDENT, "k=", k);
    var res = topKFrequent(nums, k);
    var expected = SLOWTopKFrequent(nums, k);
    Arrays.sort(res);
    Arrays.sort(expected);
    verify(res, expected);
  }

  private int[] SLOWTopKFrequent(int[] nums, int k) {
    final int MIN_NUM = -10000;
    final int MAX_NUM = 10000;
    final int NUM_ORIGIN = -MIN_NUM;

    var hlen = MAX_NUM + 1 - MIN_NUM;
    int hist[] = new int[hlen];
    for (var x : nums) {
      hist[x + NUM_ORIGIN]++;
    }

    // Modify array so upper n bits are the frequency, lower the actual value; omit elements with freq zero
    int destCount = 0;
    for (int i = 0; i < hlen; i++) {
      var f = hist[i];
      if (f == 0)
        continue;
      hist[destCount++] = (f << 15) | i;
    }
    pr("augmented hist:", Arrays.copyOfRange(hist, 0, destCount));

    Arrays.sort(hist, 0, destCount);
    pr("sorted        :", Arrays.copyOfRange(hist, 0, destCount));

    int kEff = Math.min(destCount, k);
    int[] out = new int[kEff];
    int j = 0;
    for (int i = destCount - kEff; i < destCount; i++) {
      out[j++] = (hist[i] & 0x7fff) - NUM_ORIGIN;
    }
    pr("recovered values:", out);
    return out;
  }

  public int[] topKFrequent(int[] nums, int k) {
    final int MIN_NUM = -10000;
    final int MAX_NUM = 10000;
    final int NUM_ORIGIN = -MIN_NUM;

    // Construct a histogram as an array of frequencies for each possible value
    var histogramLength = MAX_NUM + 1 - MIN_NUM;
    int histogram[] = new int[histogramLength];
    for (var x : nums)
      histogram[x + NUM_ORIGIN]++;

    // Modify the histogram so upper bits are the frequency, and the lower the actual value 
    int j = 0;
    {
      for (int i = 0; i < histogramLength; i++) {
        var f = histogram[i];
        if (f == 0)
          continue;
        histogram[j++] = (f << 15) | i;
      }
      // Sort this array
      Arrays.sort(histogram, 0, j);
    }

    // Extract the last k values, recovering the original numbers while doing so
    int kEff = Math.min(j, k);
    int[] result = new int[kEff];
    {
      var v = 0;
      for (var i = j - kEff; i < j; i++)
        result[v++] = (histogram[i] & 0x7fff) - NUM_ORIGIN;
    }
    return result;
  }

}
