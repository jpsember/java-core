
package js.leetcode;

// 33. Search in Rotated Sorted Array
//
// Do a binary search to find the pivot index, then search each subarray to the side of the pivot
//
import static js.base.Tools.*;

public class P33SearchInRotatedArray {

  public static void main(String[] args) {
    new P33SearchInRotatedArray().run();
  }

  private void run() {
    x(1, 0, 0);
    // x(20, 3, 16 * 3);

  }

  private void x(int size, int pivot, int target) {
    int[] nums = new int[size];
    for (int i = 0; i < size; i++) {
      nums[(i + pivot) % size] = i * 3 + 10;
    }
    pr(nums);
    int pos = search(nums, target);
    pr("pos:", pos);
  }

  public int search(int[] nums, int target) {
    int piv = findPivot(nums);

    // We can avoid doing two searches by comparing the value with the pivot element
    if (target <= nums[nums.length - 1])
      return search(nums, piv, nums.length, target);
    else
      return search(nums, 0, piv, target);
  }

  public int findPivot(int[] nums) {

    var first = nums[0];

    // If pivot is at zero, return right away
    if (nums.length == 1 || first < nums[nums.length - 1])
      return 0;

    int a = 0;
    int b = nums.length;

    while (true) {

      int slot = (a + b) >> 1;
      int x = nums[slot];

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
    int a = i0;
    int b = i1;
    int slot;

    while (true) {
      if (a >= b) {
        return -1;
      }
      slot = (a + b) >> 1;
      int x = nums[slot];
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
