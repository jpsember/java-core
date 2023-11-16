//26. Remove Duplicates from Sorted Array

package js.leetcode;

import static js.base.Tools.*;

public class P26RemoveDuplicatesSortedArray {

  public static void main(String[] args) {
    new P26RemoveDuplicatesSortedArray().run();
  }

  private void run() {
    int[] a = { 0, 0, 1, 1, 1, 2, 2, 3, 3, 4 };

    pr("input:", INDENT, a);
    var len = removeDuplicates(a);
    pr("result:", INDENT, a);
    pr("unique:", len);
  }

  public int removeDuplicates(int[] nums) {
    int unique = 0;
    int prevValue = 0;
    for (int x : nums) {
      if (unique == 0 || x != prevValue) {
        nums[unique++] = x;
        prevValue = x;
      }
    }
    return unique;
  }
}
