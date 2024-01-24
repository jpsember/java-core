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
      // this.matrix = matrix;
      int h = matrix.length;
      int w = matrix[0].length;
      sum = new int[h][w];
      sum[0][0] = matrix[0][0];

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
            prev1 = sum[y][x - 1];
            if (y > 0)
              prev3 = sum[y - 1][x - 1];
          }
          if (y > 0)
            prev2 = sum[y - 1][x];
          sum[y][x] = prev1 + prev2 - prev3 + matrix[y][x];
          x++;
          y--;
        }
      }
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
      var x0 = col1;
      var y0 = row1;
      var x1 = col2;
      var y1 = row2;
      return sum(x1, y1) - sum(x1, y0 - 1) - sum(x0 - 1, y1) + sum(x0 - 1, y0 - 1);
    }

    private int sum(int x, int y) {
      var result = 0;
      if (x >= 0 && y >= 0)
        result = sum[y][x];
      return result;
    }

    private int[][] sum;
  }

}
