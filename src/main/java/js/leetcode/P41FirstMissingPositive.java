
package js.leetcode;

// 34. Find First and Last Position of Element in Sorted Array

// This can be done with two binary searches.

// ...I think I've beaten binary searches to death
//
import static js.base.Tools.*;

public class P41FirstMissingPositive {

  public static void main(String[] args) {
    new P41FirstMissingPositive().run();
  }

  private void run() {
    x(3, 4, -1, 1);
  }

  private void x(int... vals) {
    int[] nums = vals;

    pr(nums);
    var res = firstMissingPositive(nums);
    pr("first missing:", res);
  }

  int z;

  public int firstMissingPositive(int[] nums) {
    this.nums = nums;
    int n = nums.length;

    // To keep things simple, think in terms of indices being 1...n, not 0...n-1
    //
    for (int i = 1; i <= n; i++) {

      int slot = i;
      int x = readSlot(slot);

      pr(VERT_SP, "i:", i, "array:", nums);
      pr("slot:", slot, "x:", x);

      while (true) {
        pr("x:", x);
        if (x <= 0 || x > n) {
          pr("...this is not a positive integer within the range");
          break;
        }
        int xSlot = x;
        if (x == slot)
          break;

        // Move x to its own slot, then repeat with the value that was in x's slot
        int y = readSlot(xSlot);
        pr(" x is not in its slot; instead, it is occupied by y", y);
        writeSlot(xSlot, x);
        writeSlot(slot, 0);

        x = y;

        checkState(z++ < 20);
      }

    }

    for (int x = 1; x <= n; x++) {
      if (readSlot(x) != x)
        return x;
    }
    return -1;
  }

  private int readSlot(int slot) {
    return nums[slot - 1];
  }

  private void writeSlot(int slot, int value) {
    nums[slot - 1] = value;
  }

  private int[] nums;

}
