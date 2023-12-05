
package js.leetcode;

import static js.base.Tools.*;

import java.util.HashSet;
import java.util.Set;

import js.base.BasePrinter;

/**
 * I need to figure out how to offset the polygon boundary from the center of
 * the pixel.
 *
 */
public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  public static int[] xturnleft = { -1, 1, 1, -1 };

  public static int[] yturnleft = { -1, -1, 1, 1 };

  private void run() {

    x(4, 3, "110111011111", 6);

    x(3, 2, "001111", 3);

    x(1, 1, "1", 1);

    x(3, 2, "111010", 3);
    //halt();

    x(3, 2, "110111", 4);
    //  halt();

    x(3, 3, "111111111", 9);
    // halt();

    x(5, 4, "10100101111111110010", 6);
    x(3, 3, "111" //
        + "101" //
        + "111" //
        , 3);

    x(5, 5, "10100" //
        + "10111" //
        + "11101" //
        + "11111" //
        + "10010", 6);

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

  private void showBoard(int x0, int x1, int y0, int y1, Object... messages) {
    //var m = map();
    pr(VERT_SP, BasePrinter.toString(messages), "x,y", x0, y1, CR,
        "--------------------------------------------");
    for (var y = y0; y < y1; y++) {
      var sb = new StringBuilder();
      for (var x = x0; x < x1; x++) {
        var c = sCells[y * bWidth + x];
        if (x != x0)
          sb.append(' ');
        sb.append(c == 0 ? "." : "X");
      }
      pr(sb);
    }
    pr("--------------------------------------------");
  }

  // ------------------------------------------------------------------

  public int maximalRectangle(char[][] matrix) {
    unique.clear();
    int gridWidth = matrix[0].length;
    int gridHeight = matrix.length;
    bWidth = gridWidth + 2;
    bHeight = gridHeight + 2;
    byte[] cells = new byte[bWidth * bHeight];
    sCells = cells;
    int cc = bWidth + 1;
    for (var row : matrix) {
      for (var c : row) {
        if (c == '1') {
          cells[cc] = 1;
        }
        cc++;
      }
      cc += 2;
    }

    var s = map();
    for (var y = 0; y < bHeight; y++) {
      var sb = new StringBuilder();
      for (var x = 0; x < bWidth; x++) {
        sb.append((cells[x + y * bWidth] == 0 ? " ::" : " XX"));
      }
      s.putNumbered(sb.toString());
    }
    pr("grid:", INDENT, s);

    showBoard(0, bWidth, 0, bHeight, "grid");
    // Scan for concave vertices

    return subscan(0, bWidth, 0, bHeight, 0);
  }

  int depth = 0;

  private Set<Integer> unique = new HashSet<>();

  private int subscan(int x0, int x1, int y0, int y1, int maxArea) {
    var area = (x1 - x0) * (y1 - y0);
    if (area <= maxArea) {
     // pr("...skipping subscan with suboptimal area", area);
      return maxArea;
    }

    var key = x0 | (x1 << 8) | (y0 << 16) | (y1 << 24);
    if (!unique.add(key))
      return maxArea;

   // showBoard(x0, x1, y0, y1, "subscan, max area", maxArea);

    var xmid = (x1 + x0) >> 1;
    var ymid = (y1 + y0) >> 1;

    var cells = sCells;
    var bRow = bWidth;
    int cellInd = bRow * y0;

    int bestx = 0;
    int besty = 0;

    var minDist = area;

    boolean empty = true;
    boolean full = true;

    for (int y = y0; y < y1; y++, cellInd += bRow) {
      for (int x = x0; x < x1; x++) {
        int c = cellInd + x;
        var cCurr = cells[c];
        if (cCurr == 1) {
          empty = false;
          continue;
        }
        full = false;
        var dist = (x - xmid) * (x - xmid) + (y - ymid) * (y - ymid);
        if (dist < minDist) {
          minDist = dist;
          bestx = x;
          besty = y;
        //  pr("minDist now", minDist, "at", x, y);
        }
      }
    }
    if (full) {
     // pr("....full; area:", area);
      return Math.max(area, maxArea);
    }

    if (empty)
      return maxArea;

   // pr("bestx:", bestx, "besty:", besty, "x0:", x0, "y0:", y0);

    maxArea = subscan(x0, bestx, //
        y0, y1, maxArea);
    maxArea = subscan(bestx + 1, x1, //
        y0, y1, maxArea);

    maxArea = subscan(x0, x1, //
        besty + 1, y1, maxArea);
    maxArea = subscan(x0, x1, //
        y0, besty, maxArea);

    return maxArea;
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
}
