package js.leetcode;

import static js.base.Tools.*;

public class RangeSumQuery2D extends LeetCode {

  public static void main(String[] args) {
    new RangeSumQuery2D().run();
  }

  public void run() {

    x("[[[[-4,-5]]]", 2, 0, 0, 0, 1);
    //[[[[-4,-5]]],[0,0,0,0],[0,0,0,1],[0,1,0,1]]

  }

  private void x(String ns, int width, int r1, int c1, int r2, int c2) {
    var nums = extractMatrix(ns, width);
    var alg1 = new NumMatrix(nums);
    var sum = alg1.sumRegion(r1, c1, r2, c2);
    pr(sum);
  }

  class NumMatrix {

    public NumMatrix(int[][] matrix) {

      // The 'sum' array will hold sum of cells(0...x-1, 0...y-1); so that first row and col are zero

      int h = matrix.length + 1;
      int w = matrix[0].length + 1;
      sum = new int[h][w];

      for (int y = 1; y < h; y++) {
        var ry = sum[y];
        var rym = sum[y - 1];
        var mym = matrix[y - 1];
        for (int x = 1; x < w; x++)
          ry[x] = mym[x - 1] + ry[x - 1] + rym[x] - rym[x - 1];
      }
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
      var r2 = sum[row2 + 1];
      var r1 = sum[row1];
      return r2[col2 + 1] - r1[col2 + 1] - r2[col1] + r1[col1];
    }

    private int[][] sum;
  }

}
