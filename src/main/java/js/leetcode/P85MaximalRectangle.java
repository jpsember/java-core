
package js.leetcode;

import static js.base.Tools.*;

public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  private void run() {
    x(5, 4, "10100101111111110010", 6);
  }

  private void x(int width, int height, String cells, int expected) {

    char[][] m = new char[height][];
    int i = 0;
    var sb = new StringBuilder();
    for (int y = 0; y < height; y++) {
      m[y] = new char[width];
      sb.setLength(0);
      for (int x = 0; x < width; x++) {
        m[y][x] = cells.charAt(i++);
      }
    }
    var result = maximalRectangle(m);
    pr(width, "x", height, ": result:", result);
    checkState(expected == result, "expected:", expected);
  }

  public int maximalRectangle(char[][] matrix) {
    return -1;
  }
}
