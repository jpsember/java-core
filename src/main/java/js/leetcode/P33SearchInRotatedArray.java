
package js.leetcode;

// 33. Search in Rotated Sorted Array

import static js.base.Tools.*;

public class P33SearchInRotatedArray {

  public static void main(String[] args) {
    new P33SearchInRotatedArray().run();
  }

  private void run() {
    x(20, 3, 16 * 3);

  }

  private void x(int size, int pivot, int target) {
    int[] nums = new int[size];
    for (int i = 0; i < size; i++) {
      nums[(i + pivot) % size] = i * 3;
    }
    pr(nums);
    int pos = search(nums, target);
    pr("pos:", pos);
  }

  public int search(int[] nums, int target) {
    int piv = findPivot(nums);
    pr("pivot:", piv);

    pr("...searching for", target, "within", 0, "...", piv);
    int slot = search(nums, 0, piv, target);
    pr("...slot:", slot);
    if (slot < 0) {
      pr("...searching for", target, "within", piv, "...", nums.length);
      slot = search(nums, piv, nums.length, target);
      pr("...slot:", slot);
    }
    if (slot < 0)
      return -1;
    int adjustedSlot = (slot - piv + nums.length) % nums.length;
    pr("found at slot", slot, "adjusted:", adjustedSlot);
    return adjustedSlot;
  }

  public int findPivot(int[] nums) {
    pr("find pivot");
    var first = nums[0];
    if (first < nums[nums.length - 1])
      return 0;

    int a = 0;
    int b = nums.length;

    while (true) {

      int slot = (a + b) >> 1;
      int x = nums[slot];

      pr("a:", a, "b:", b, "s:", slot, "x:", x);

      if (slot + 1 < nums.length && x > nums[slot + 1])
        return slot + 1;
      if (x > first) {
        a = slot + 1;
      } else {
        b = slot;
      }
    }
  }

  private int search(int[] nums, int i0, int i1, int target) {
    pr("search for:", target);
    int a = i0;
    int b = i1;
    int slot;

    while (true) {
      if (a >= b) {
        return -1;
      }
      slot = (a + b) >> 1;
      int x = nums[slot];
      pr("a:", a, "b:", b, "s:", slot, "x:", x);
      if (x == target) {
        return slot;
      }

      if (x < target)
        a = slot + 1;
      else
        b = slot;
    }
  }

}
