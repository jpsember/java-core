
package js.leetcode;

// 34. Find First and Last Position of Element in Sorted Array

// This can be done with two binary searches.

// ...I think I've beaten binary searches to death
//
import static js.base.Tools.*;

public class P34FindFirstAndLastPositionOfElementInSortedArray {

  public static void main(String[] args) {
    new P34FindFirstAndLastPositionOfElementInSortedArray().run();
  }

  private void run() {
    x(5, 7, 7, 8, 8, 10, 8);
  }

  private void x(int... vals) {
    int[] nums = new int[vals.length - 1];
    int target = vals[vals.length - 1];

    for (int i = 0; i < nums.length; i++) {
      nums[i] = vals[i];
    }
    pr(nums);
    var res = searchRange(nums, target);
    pr("target:", target, "nums:", nums, INDENT, "result:", res);
  }

  public int[] searchRange(int[] nums, int target) {
    int[] res = new int[2];
    int a = search(nums, target, true, 0, nums.length);
    res[0] = a;
    res[1] = a;
    if (a >= 0) {
      res[1] = search(nums, target, false, a, nums.length);
    }
    return res;
  }

  private int search(int[] nums, int target, boolean ltOrEqual, int a, int b) {
    
    int slot;

    int bestResult = -1;
    while (true) {
      if (a >= b) {
        break;
      }
      slot = (a + b) >> 1;
      int x = nums[slot];

      if (x == target) {
        bestResult = slot;
      }

      if (x < target || (!ltOrEqual && x == target))
        a = slot + 1;
      else
        b = slot;
    }
    return bestResult;
  }

}
