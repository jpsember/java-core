package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {

    x("3 1 5 8");
    x("51  23  10  41  45 *47      95      78");
    x(" 51  23  10  41  45  47  35  95                 *97                      78");
    //  x("[2,5,3]");

    //    if (true) {
    //    xSeed(1000, 100, null);
    //    return;
    //    }

    if (true) {
      int s = 128;
      for (int y = 3; y < 20; y++) {
        pr(VERT_SP, "y:", y);
        xSeed(y * s + 17, y, null);
      }
      return;
    }

    if (false)
      xSeed(123, 5, 722946);
    x("[3,1,5,8]");
    if (true)
      return;

    x("[8,2,6,8,9,8,1,4,1,5,3,0,7,7,0,4,2]", 3414);
    if (true)
      return;

    x(3, 5, 10, 11, 9, 5, 3);
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

    var alg1 = new DP();

    pr(str(nums));
    var res = alg1.maxCoins(nums);
    pr(alg1);

    if (expected == null) {
      var alg2 = new Basic();
      expected = alg2.maxCoins(nums);
      if (expected != res)
        pr(VERT_SP, "*** Unexpected results!  Expected:", INDENT, alg2);
    }

    verify(res, expected);
  }

  private String gameResults(int[] nums, int[] slots) {
    var sb = sb();

    var dash = repeatText("----", nums.length + 4) + "\n";
    sb.append(dash);
    var slotEmptyFlags = new BitSet(nums.length);
    var totalPopValue = 0;
    var numsColumn = 0;

    for (int turn = 0; turn < nums.length; turn++) {
      var cursor = sb.length();
      var currMove = slots[turn];
      sb.append("[ ");

      int leftValue = 1;
      int rightValue = 1;
      int leftIndex = Integer.MIN_VALUE;
      int rightIndex = Integer.MAX_VALUE;

      for (int i = 0; i < nums.length; i++) {
        if (slotEmptyFlags.get(i))
          continue;

        if (i < currMove && i > leftIndex) {
          leftIndex = i;
          leftValue = nums[i];
        } else if (i > currMove && i < rightIndex) {
          rightIndex = i;
          rightValue = nums[i];
        }

        tab(sb, cursor + 3 + i * 4);
        sb.append(i == currMove ? '*' : ' ');
        sb.append(nums[i]);
      }

      slotEmptyFlags.set(currMove);

      var c2 = cursor + 3 + nums.length * 4;
      tab(sb, c2);
      sb.append("] = ");
      numsColumn = sb.length() - cursor;
      var popValue = nums[currMove] * leftValue * rightValue;
      sb.append(fmt(popValue));
      totalPopValue += popValue;
      sb.append('\n');
    }
    sb.append(spaces(numsColumn));
    sb.append(fmt(totalPopValue));
    sb.append('\n');

    sb.append(dash);

    return sb.toString();
  }

  private String fmt(int value) {
    var s = "" + value;
    return spaces(6 - s.length()) + s;
  }

  private abstract class Alg {
    public abstract int maxCoins(int[] nums);
  }

  // ------------------------------------------------------------------

  private class Basic extends Alg {

    public int maxCoins(int[] nums) {
      mNums = nums;
      mMemo.clear();
      mSlots = null;
      var result = aux(0, nums.length, 1, 1);
      return result;
    }

    public int[] getSlots() {
      if (mSlots == null) {
        var x = mNums.length;
        var sl = new ArrayList<Integer>(x);
        auxFillSlots(sl, 0, x, 1, 1);
        mSlots = new int[x];
        for (int i = 0; i < x; i++)
          mSlots[i] = sl.get(x - 1 - i);
      }
      return mSlots;
    }

    private void auxFillSlots(List<Integer> dest, int start, int stop, int leftValue, int rightValue) {
      if (stop <= start)
        return;
      int slot = start;
      if (stop > start + 1) {
        var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
        var memoValue = mMemo.get(key);
        checkState(memoValue != null);
        slot = (int) (memoValue >> 32);
      }
      dest.add(slot);
      auxFillSlots(dest, start, slot, leftValue, mNums[slot]);
      auxFillSlots(dest, slot + 1, stop, mNums[slot], rightValue);
    }

    private int aux(int start, int stop, int leftValue, int rightValue) {
      if (stop <= start)
        return 0;
      var nums = mNums;
      if (stop == start + 1)
        return leftValue * nums[start] * rightValue;

      // We store a key that embeds the left and right values (log 100 = 7 bits), and the
      // start and stop indices (log 300 = 9 bits)
      var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
      long memoValue = mMemo.getOrDefault(key, -1L);
      if (memoValue >= 0)
        return (int) memoValue;

      // Consider each balloon as the *last* one to pop

      // Is there a heuristic we can employ to speed things up?
      // Skip certain values?

      var bestResult = 0;
      var bestSlot = -1;

      // The values of the left and right sides are nonstrictly increasing as the number of values
      // increases.

      for (int pivot = start; pivot < stop; pivot++) {
        var pivotValue = nums[pivot];
        // We never want a zero to be the *last* balloon popped in a set
        if (pivotValue == 0)
          continue;

        var leftSum = aux(start, pivot, leftValue, pivotValue);
        var rightSum = aux(pivot + 1, stop, pivotValue, rightValue);
        var c = leftSum + (leftValue * pivotValue * rightValue) + rightSum;
        if (c > bestResult) {
          bestResult = c;
          bestSlot = pivot;
        }
      }

      mMemo.put(key, bestResult | (((long) bestSlot) << 32));
      return bestResult;
    }

    @Override
    public String toString() {
      return "(Basic Algorithm:)\n" + gameResults(mNums, getSlots());
    }

    // The values are the score in the lower 32 bits, the last move in the upper 32 bits
    private Map<Integer, Long> mMemo = new HashMap<>();
    private int[] mNums;
    private int[] mSlots;
  }

  private class DP extends Alg {

    public int maxCoins(int[] nums) {

      int n = nums.length;
      {
        // Construct expanded num list by adding 1 to each side
        var tmp = new int[n + 2];
        tmp[0] = tmp[n + 1] = 1;
        System.arraycopy(nums, 0, tmp, 1, n);
        nums = tmp;
        n = nums.length;
      }

      // Construct dynamic grid
      // The first dimension will be the 'from' slot, and the
      // second will be the 'to' slot.
      //
      // We never need a 'from' value of nc-1, so don't allocate a row there.
      // And we never need a 'to' value of zero, but to keep things simple we won't change
      // anything there.
      //
      var g = new int[n - 1][n];

      // Outer loop iterates over the maximum gap between source and target balloons,
      // which is at most n, to jump from the leftmost '1' to the rightmost '1'.

      for (int gap = 2; gap < n; gap++) {
        // Loop over each possible source balloon
        for (int src = 0; src + gap < n; src++) {
          var trg = src + gap;
          var srcTrgProd = nums[src] * nums[trg];

          // Iterate over each possible step or 'middle' balloon
          var bestSum = 0;
          var cSrc = g[src]; // optimization to avoid g[src][...] while src doesn't change
          for (int mid = src + 1; mid < trg; mid++) {
            var sum = cSrc[mid] + srcTrgProd * nums[mid] + g[mid][trg];
            if (sum > bestSum)
              bestSum = sum;
          }
          cSrc[trg] = bestSum;
        }
        //db("Gap", gap, INDENT, strTable(g));
      }
      return g[0][n - 1];
    }

    public String toString() {
      return "DP";
    }
  }

}
