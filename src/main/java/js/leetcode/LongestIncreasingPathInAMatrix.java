package js.leetcode;

import static js.base.Tools.*;

import java.util.Comparator;
import java.util.PriorityQueue;

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

  private class Pt implements Comparator {
    int x;
    int y;
    int value;
    
    Pt(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int flag() {
      return visitFlags[y][x];
    }

    @Override
    public int compare(Object o1, Object o2) {
      var p1 = (Pt) o1;
      var p2 = (Pt) o2;
      int diff = 
    }
  }

  public int longestIncreasingPath(int[][] matrix) {
    int w = matrix[0].length;
    int h = matrix.length;
    visitFlags = new int[h][w];
    this.matrix = matrix;

    var queue = new PriorityQueue<Pt>(w * h, new Pt(0,0));
    return -1;
  }

  private int[][] visitFlags;
  private int[][] matrix;

}
