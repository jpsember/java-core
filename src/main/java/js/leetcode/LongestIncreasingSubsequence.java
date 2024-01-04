package js.leetcode;

import static js.base.Tools.*;

/**
 * I think I can work backwards from the last element, deciding whether the LIS
 * for the current index includes the current element or not.
 */
public class LongestIncreasingSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestIncreasingSubsequence().run();
  }

  public void run() {
    x("[10,9,2,5,3,7,101,18]", 4);
  }

  private void x(String s, int expected) {
    var nums = extractNums(s);
    var res = lengthOfLIS(nums);
    pr("Result:", res);
    verify(res, expected);
  }

  public int lengthOfLIS(int[] nums) {
    return 99;
  }
}
