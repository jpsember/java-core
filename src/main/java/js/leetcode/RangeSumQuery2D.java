package js.leetcode;

import static js.base.Tools.*;

public class RangeSumQuery2D extends LeetCode {

  public static void main(String[] args) {
    new RangeSumQuery2D().run();
  }

  public void run() {

    x("[[[[3, 0, 1, 4, 2], [5, 6, 3, 2, 1], [1, 2, 0, 1, 5], [4, 1, 0, 1, 7], [1, 0, 3, 0, 5]]]", 5, 2, 1, 4,
        3);

  }

  private void x(String ns, int width, int r1, int c1, int r2, int c2) {

    var nums = extractMatrix(ns, width);
    var alg1 = new NumMatrix(nums);
    var sum = alg1.sumRegion(r1, c1, r2, c2);
    pr(sum);

  }

  class NumMatrix {

    public NumMatrix(int[][] matrix) {
      int h = matrix.length;
      int w = matrix[0].length;
      var s2 = new int[h][w];
      s2[0][0] = matrix[0][0];

      sum = new int[h + 1][w + 1];
      for (int scan = 1;; scan++) {
        int y = scan;
        int x = 0;
        if (y >= h) {
          x = y - h;
          y = h - 1;
          if (x == w)
            break;
        }

        while (x < w && y >= 0) {
          var prev1 = 0;
          var prev2 = 0;
          var prev3 = 0;
          if (x > 0) {
            prev1 = s2[y][x - 1];
            if (y > 0)
              prev3 = s2[y - 1][x - 1];
          }
          if (y > 0)
            prev2 = s2[y - 1][x];
          s2[y][x] = prev1 + prev2 - prev3 + matrix[y][x];
          sum[y + 1][x + 1] = s2[y][x];
          x++;
          y--;
        }
      }
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
      return sum[row2 + 1][col2 + 1] - sum[row1][col2 + 1] - sum[row2 + 1][col1] + sum[row1][col1];
    }

    private int[][] sum;
  }

}
