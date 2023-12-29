package js.leetcode;

import static js.base.Tools.*;

public class LongestIncreasingPathInAMatrix extends LeetCode {

  public static void main(String[] args) {
    new LongestIncreasingPathInAMatrix().run();
  }

  public void run() {
    x("[[9,9,4],[6,6,8],[2,1,1]]", 4);
  }

  private void x(String s, int expected) {
    var matrix = extractMatrix(s, 3);

    var result = longestIncreasingPath(matrix);
    pr("path length:", result);
    verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int longestIncreasingPath(int[][] matrix) {
    return -1;
  }
}
