
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

    x(1, 1, "0", 0);
    for (int y = 1; y < 20; y++) {
      for (int x = 1; x < 5; x++) {
        y(x, y);
      }
    }

    y(2, 16);

    x(4, 3, "110111011111", 6);

    x(3, 2, "001111", 3);

    x(1, 1, "1", 1);

    x(3, 2, "111010", 3);

    x(3, 2, "110111", 4);

    x(3, 3, "111111111", 9);

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

  private void y(int w, int h) {
    x(w, h, dup("1", w * h), w * h);
  }

  private static String dup(String s, int count) {
    var sb = new StringBuilder();
    for (int i = 0; i < count; i++)
      sb.append(s);
    return sb.toString();
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

  /* private */ void showBoard(int x0, int x1, int y0, int y1, Object... messages) {
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
    sScannedSubrectsSet.clear();
    int gridWidth = matrix[0].length;
    int gridHeight = matrix.length;
    bWidth = gridWidth;
    bHeight = gridHeight;
    byte[] cells = new byte[bWidth * bHeight];
    sCells = cells;
    int ci = 0;
    for (var row : matrix) {
      for (var c : row) {
        if (c == '1')
          cells[ci] = 1;
        ci++;
      }
    }
    return subscan(0, bWidth, 0, bHeight, 0);
  }

  private static int[] work = new int[4];

  private static int[] inset(int x0, int x1, int y0, int y1) {
    var cl = sCells;
    var ptr = bWidth * y0 + x0;
    var wd = x1 - x0;
    outer1: while (y0 < y1) {
      for (int i = 0; i < wd; i++)
        if (cl[i + ptr] != 0)
          break outer1;
      y0++;
      ptr += bWidth;
    }

    ptr = bWidth * (y1 - 1) + x0;
    outer2: while (y0 < y1) {
      for (int i = 0; i < wd; i++)
        if (cl[i + ptr] != 0)
          break outer2;
      y1--;
      ptr -= bWidth;
    }

    ptr = bWidth * y0 + x0;
    var ht = y1 - y0;
    outer3: while (x0 < x1) {
      var ptr2 = ptr;
      for (int i = 0; i < ht; i++) {
        if (cl[ptr2] != 0)
          break outer3;
        ptr2 += bWidth;
      }
      x0++;
      ptr++;
    }

    ptr = bWidth * y0 + x1 - 1;
    ht = y1 - y0;
    outer4: while (x0 < x1) {
      var ptr2 = ptr;
      for (int i = 0; i < ht; i++) {
        if (cl[ptr2] != 0)
          break outer4;
        ptr2 += bWidth;
      }
      x1--;
      ptr--;
    }
    var w = work;
    w[0] = x0;
    w[1] = x1;
    w[2] = y0;
    w[3] = y1;
    return w;
  }

  private int subscan(int x0, int x1, int y0, int y1, int maxArea) {
    var area = (x1 - x0) * (y1 - y0);
    if (area <= maxArea)
      return maxArea;

    showBoard(x0, x1, y0, y1, "subscan", x0, y0, "area:", maxArea);

    // If we've already scanned (or are currently scanning) this particular subrect,
    // exit
    var key = x0 | (x1 << 8) | (y0 << 16) | (y1 << 24);
    if (!sScannedSubrectsSet.add(key))
      return maxArea;

    // Inset as much as possible
    var w = inset(x0, x1, y0, y1);
    x0 = w[0];
    x1 = w[1];
    y0 = w[2];
    y1 = w[3];
    area = (x1 - x0) * (y1 - y0);

    showBoard(x0, x1, y0, y1, "after inset", x0, y0, "area:", maxArea);

    var xmid = (x1 + x0) >> 1;
    var ymid = (y1 + y0) >> 1;

    var cells = sCells;
    int bestx = 0;
    int besty = 0;
    var minDist = 100000;
    boolean empty = true;
    boolean full = true;
    var rowOffset = bWidth - (x1 - x0);
    int cellInd = bWidth * y0 + x0;
    for (int y = y0; y < y1; y++, cellInd += rowOffset) {
      var yDistComponent = (y - ymid) * (y - ymid);
      for (int x = x0; x < x1; x++) {
        var cCurr = cells[cellInd++];
        if (cCurr == 1) {
          empty = false;
          continue;
        }
        full = false;
        var dist = (x - xmid) * (x - xmid) + yDistComponent;
        if (dist < minDist) {
          minDist = dist;
          bestx = x;
          besty = y;
        }
      }
    }
    if (full)
      return Math.max(area, maxArea);
    if (empty)
      return maxArea;
    maxArea = subscan(x0, bestx, y0, y1, maxArea);
    maxArea = subscan(bestx + 1, x1, y0, y1, maxArea);
    maxArea = subscan(x0, x1, besty + 1, y1, maxArea);
    maxArea = subscan(x0, x1, y0, besty, maxArea);
    return maxArea;
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
  private static Set<Integer> sScannedSubrectsSet = new HashSet<>();
}
