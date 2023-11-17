
package js.leetcode;

// 34. Find First and Last Position of Element in Sorted Array
//
// We scan each integer, and if it's within range, we move it to its
// appropriate order within the array, and do the same with the element
// that was replaced (if necessary).  
//
// This runs in linear time since each element will be moved at least once.
//
import static js.base.Tools.*;

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
    pr("first missing:", res);
  }

  public int firstMissingPositive(int[] nums) {
    int n = nums.length;

    // To keep things simple, think in terms of indices being 1...n, not 0...n-1
    //
    for (int x : nums) {
      while (true) {
        // If not a positive integer from 1...n, ignore
        if (x <= 0 || x > n) {
          break;
        }
        // Move x to its own slot, then repeat with the value that was in x's slot
        int y = nums[x - 1];
        if (y == x) {
          break;
        }
        nums[x - 1] = x;
        x = y;
      }
    }

    for (int x = 1; x <= n; x++) {
      if (nums[x - 1] != x)
        return x;
    }
    return n + 1;
  }

}
