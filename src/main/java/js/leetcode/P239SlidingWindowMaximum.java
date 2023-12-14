package js.leetcode;

import static js.base.Tools.*;

/**
 * <pre>
 * 
 *  Let Wn be the window of X[n-k+1],X[n-k+2],...,X[n], and Mn be the maximum value in this window.
 *  
 *  We define and maintain the subsequence Sn as follows:
 *  
 *  Base case:
 *  
 *  S1 = {X[1]}   
 *  
 *  Inductive case:
 *  
 *  For each additional Xi, while the last element of the window is <= Xi, remove it.
 *  
 *  Add Xi to the end of the window.
 *  
 *  Trim the window, by removing the first element, if its size > k.
 *  
 *  Note that the subsequence S is always sorted from highest to lowest, and
 *  the maximum element in the subsequence is the first element, which is also
 *  the maximum element in the window Wn.
 *  
 *  The key intuition is: if the elements being examined are in strictly increasing order, then
 *  only the most recent element needs to be kept in the window; otherwise, they can be attached
 *  to the end of the window, and the window is thus in decreasing order.
 *
 *  We use a linked list to store the window, to allow O(1) insertions and deletions.
 *  
 *  The running time is O(n), as is the memory usage.
 * </pre>
 */
public class P239SlidingWindowMaximum extends LeetCode {

  public static void main(String[] args) {
    new P239SlidingWindowMaximum().run();
  }

  public void run() {
    // x(3, "[1,3,-1,-3,5,3,6,7]");

    y(3, 20);
  }

  /* private */ void y(int k, int n) {
    int seed = 1965 + k * 3 + n * 17;
    rand(seed);
    int[] vals = new int[n];
    for (int i = 0; i < n; i++) {
      var v = rand().nextInt(900) + 100;
      if (n < 30)
        v = v / 10;
      vals[i] = v;
    }
    x(k, vals);
  }

  /* private */ void x2(Object... unused) {
  }

  private void x(int k, int[] nums) {
    var exp = slowMaxSlidingWindow(nums, k);
    var result = maxSlidingWindow(nums, k);
    if (result.length < 300)
      pr(nums, "k=", k, "result:", result);
    else
      pr(nums, "k=", k, "result: (", result.length, "numbers)");
    verify(result, exp);
  }

  private void x(int k, String a) {
    x(k, extractNums(a));
  }

  /* private */ int[] slowMaxSlidingWindow(int[] nums, int k) {
    int[] result = new int[nums.length - (k - 1)];
    for (int i = 0; i < result.length; i++) {

      int m = 0;
      for (int j = i; j < i + k; j++) {
        if (j == i || m < nums[j])
          m = nums[j];
      }
      result[i] = m;
    }
    return result;
  }

  public int[] maxSlidingWindow(int[] nums, int k) {
    windowStart = 0;
    windowEnd = 0;
    windowStorage = nums;
    int[] result = new int[nums.length - k + 1];

    for (int i = 0; i < nums.length; i++) {
      int val = nums[i];
      int newItem = buildItem(val, i);
      // While the tail window value is less than the new one, remove it
      while (windowSize() != 0 && itemVal(readWindowRight()) <= val)
        windowEnd--;
      appendToWindowRight(newItem);
      // If the left window value came from outside the window size, remove it
      if (itemSlot(readWindowLeft()) <= i - k)
        windowStart++;

      if (i + 1 >= k)  
        result[i + 1 - k] = itemVal(readWindowLeft());
    }
    return result;
  }

  // We will pack a (value, slot) pair into a 32-bit signed integer.
  //
  private static final int MIN_VAL = -10000;
  private static final int MAX_VAL = 10000 + 1;
  private static final int VALUE_SCALE = (MAX_VAL - MIN_VAL);

  private static int buildItem(int val, int slot) {
    return (val - MIN_VAL) + (slot * VALUE_SCALE) + 1;
  }

  private static int itemVal(int item) {
    return ((item - 1) % VALUE_SCALE) + MIN_VAL;
  }

  private static int itemSlot(int item) {
    return ((item - 1) / VALUE_SCALE);
  }

  private int readWindowLeft() {
    return windowStorage[windowStart];
  }

  private int readWindowRight() {
    return windowStorage[windowEnd - 1];
  }

  private void appendToWindowRight(int item) {
    windowStorage[windowEnd++] = item;
  }

  private int windowSize() {
    return windowEnd - windowStart;
  }

  private int windowStart, windowEnd;
  private int[] windowStorage;

}
