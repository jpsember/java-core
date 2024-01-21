package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {

    x(1, 8, 9, 7, 2);

    if (false)
      x("[54,88,26,48,9,32,0,66,4,82,66,7,28,23,73,99,44,27,16,29,52,66,30,97,41,33,60,62,45,40,19,35,97,69,19,60,2,48,81,66,45,86,51,51,47,97,5,59,54,98,79,90,70,70,62,16,96,95,97,77,21,35,10,90,14,31,21,29,40,81,33,64,91,44,81,85,77,9,67,58,77,88,69,76,89,33,36,87,85,77,94,96,37,61,23,75,15,29,21,88,91]",
          39731870);

    x("[8,2,6,8,9,8,1,4,1,5,3,0,7,7,0,4,2]");

    x(1, 2, 3, 4, 2, 1);
    x(2, 3, 4, 5, 7, 6, 2, 1);
    x(3, 5, 10, 11, 9, 5, 3);
    x(3, 5, 9, 4, 11, 13, 7, 10, 4, 2);
    x(4, 3, 4, 5, 6, 5);
    x(11, 5, 4);
    x("[11,5,4,8]");
    //
    //    int s = 126;
    //    for (int y = 3; y < 20; y++) {
    //      pr(VERT_SP, "y:", y);
    //      xSeed(y * s + 17, y, null);
    //    }
    //    x("[1,1,1,10,2,0,1,1,1]");
    //
    //    x("[10,2,0]");
    //    x("[1,3,7,7,1,7]");
    //
    //    x("[3,1,5,8]", 167);
    //    x("[2,5,10,20,10,5,2]");
    //    x("[1,5]", 10);
    //
    //    xSeed(1965, 300, 105676120);
    //
    //    x("[3,7,11,7,3,7,11,7,3]");
    //    x("[5,6,7,1,2,3,0,6,12,20]");
    //    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]", 3394);
  }

  private void xSeed(int seed, int count, Integer expected) {
    var nums = new int[count];
    rand(seed);
    for (int i = 0; i < count; i++) {
      nums[i] = rand().nextInt(101);
    }
    x(nums, expected);
  }

  private void x(int... nums) {
    x(nums, null);
  }

  private void x(String a) {
    x(a, null);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);
    x(nums, expected);
  }

  private void x(int[] nums, Integer expected) {
    db = nums.length < 12;

    Alg alg1 = new Recursion2();

    pr(toStr(nums));
    var res = alg1.maxCoins(nums);
    pr(INDENT, res);

    if (expected == null) {
      var alg2 = new Slow();
      db = true;
      expected = alg2.maxCoins(nums);
      pr(alg2);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxCoins(int[] nums);

  }

  /**
   * A new recursive method in which we partition based on the *last* balloon to
   * be popped, and supply the external left and right balloon values to each
   * subarray
   */
  private class Slow extends Alg {

    public int maxCoins(int[] nums) {
      mMemo.clear();
      return aux(nums, 0, nums.length, 1, 1);
    }

    private int aux(int[] nums, int start, int stop, int leftValue, int rightValue) {
      if (stop <= start)
        return 0;
      if (stop == start + 1)
        return leftValue * nums[start] * rightValue;

      // We store a key that embeds the left and right values (log 100 = 7 bits), and the
      // start and stop indices (log 300 = 9 bits)
      var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
      var output = mMemo.get(key);
      if (output != null)
        return output;

      // Consider each balloon as the *last* one to pop

      var bestResult = -1;
      for (int pivot = start; pivot < stop; pivot++) {
        var pivotValue = nums[pivot];
        var leftSum = aux(nums, start, pivot, leftValue, pivotValue);
        var rightSum = aux(nums, pivot + 1, stop, pivotValue, rightValue);
        var c = leftSum + (leftValue * pivotValue * rightValue) + rightSum;
        if (c > bestResult)
          bestResult = c;
      }

      mMemo.put(key, bestResult);
      return bestResult;
    }

    private Map<Integer, Integer> mMemo = new HashMap<>();
  }

  // ------------------------------------------------------------------

  /**
   * A new recursive method in which we partition based on the *last* balloon to
   * be popped, and supply the external left and right balloon values to each
   * subarray
   */
  private class Recursion2 extends Alg {

    public int maxCoins(int[] nums) {
      mMemo.clear();
      mCalls = 0;
      mMisses = 0;
      var result = aux(nums, 0, nums.length, 1, 1);
      pr("calls:", mCalls, "miss pct:", (mMisses * 100.0) / mCalls);
      return result;
    }

    private int aux(int[] nums, int start, int stop, int leftValue, int rightValue) {
      if (stop <= start)
        return 0;
      if (stop == start + 1)
        return leftValue * nums[start] * rightValue;

      // We store a key that embeds the left and right values (log 100 = 7 bits), and the
      // start and stop indices (log 300 = 9 bits)
      var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
      var output = mMemo.get(key);
      mCalls++;
      if (output != null)
        return output;
      mMisses++;

      if (db) {
        pushIndent();
        db("aux", leftValue, toStr(nums, start, stop), rightValue);
      }

      // Consider each balloon as the *last* one to pop

      // Is there a heuristic we can employ to speed things up?
      // Skip certain values?

      var bestResult = -1;

      // The values of the left and right sides are nostrictly increasing as the number of values
      // increases.

      for (int pivot = start; pivot < stop; pivot++) {

        // Heuristic: try not considering pivot values that are strictly between their neighbors
        var pivotValue = nums[pivot];
        if (pivot > start && pivot < stop - 1) {
          int diff1 = pivotValue - nums[pivot - 1];
          int diff2 = nums[pivot + 1] - pivotValue;
          if (diff1 * diff2 > 0) {
            pr("...skipping pivot value:", pivotValue);
            continue;
          }
        }
        var leftSum = aux(nums, start, pivot, leftValue, pivotValue);
        var rightSum = aux(nums, pivot + 1, stop, pivotValue, rightValue);
        var c = leftSum + (leftValue * pivotValue * rightValue) + rightSum;
        if (c > bestResult)
          bestResult = c;
      }

      if (db) {
        popIndent();
      }
      mMemo.put(key, bestResult);
      return bestResult;
    }

    private int mCalls, mMisses;
    private Map<Integer, Integer> mMemo = new HashMap<>();
  }
}
