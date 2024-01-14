package js.leetcode;

import static js.base.Tools.*;

public class LongestCommonSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubsequence().run();
  }

  public void run() {
    loadTools();
    x("abcde", "ace",3);
  }

  private void x(String a, String b, int expected) {
    var res = longestCommonSubsequence(a, b);
    pr(a, CR, b, INDENT, res);
    verify(res, expected);
  }

  // ------------------------------------------------------------------

  public int longestCommonSubsequence(String text1, String text2) {
    return 22;
  }
}
