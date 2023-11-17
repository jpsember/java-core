
package js.leetcode;

// 34. Find First and Last Position of Element in Sorted Array

// This can be done with two binary searches.

// ...I think I've beaten binary searches to death
//
import static js.base.Tools.*;

import js.base.Tools;

public class P41FirstMissingPositive {

  public static void main(String[] args) {
    new P41FirstMissingPositive().run();
  }

  private void run() {
    x(2, 1);
  }

  private void x(int... vals) {
    int[] nums = vals;

    pr(nums);
    var res = firstMissingPositive(nums);
    Tools.pr("first missing:", res);
  }

  int z;

  private void pr(Object... args) {
  }

  public int firstMissingPositive(int[] nums) {
    this.nums = nums;
    int n = nums.length;

    // To keep things simple, think in terms of indices being 1...n, not 0...n-1
    //
    for (int i = 1; i <= n; i++) {

      int slot = i;
      int x = readSlot(slot);
      if (slot != x) {
        writeSlot(slot, -99);
      }

      pr(VERT_SP, "slot:", slot, "array:", nums, "array[slot]:", x);
      while (true) {
        if (x <= 0 || x > n) {
          pr("...this is not a positive integer within the range");
          //          // For clarity, set it to something undefined
          //          writeSlot(slot,-99);
          break;
        }

        // Move x to its own slot, then repeat with the value that was in x's slot
        int y = readSlot(x);
        if (y == x) {
          pr(" the slot for x already contains x");
          break;
        }
        pr(" x is not in its slot; instead, it is occupied by y", y);
        writeSlot(x, x);
        x = y;

        //        checkState(z++ < 20);
      }

    }

    for (int x = 1; x <= n; x++) {
      if (readSlot(x) != x)
        return x;
    }
    return n + 1;
  }

  private int readSlot(int slot) {
    return nums[slot - 1];
  }

  private void writeSlot(int slot, int value) {
    nums[slot - 1] = value;
  }

  private int[] nums;

}
