
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.base.BasePrinter;

public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  private void run() {

    x(5, 4, "10100101111111110010", 6);

    x(4, 3, "110111011111", 6);

    x(1, 1, "0", 0);
    for (int y = 1; y < 20; y++) {
      for (int x = 1; x < 5; x++) {
        y(x, y);
      }
    }

    y(2, 16);

    x(3, 2, "001111", 3);

    x(1, 1, "1", 1);

    x(3, 2, "111010", 3);

    x(3, 2, "110111", 4);

    x(3, 3, "111111111", 9);

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
    prepareGrid(matrix);
    final var bw = bWidth;
    final var bh = bHeight;
    final var sc = sCells;
    showBoard(0, bw, 0, bh, "initial");

    int maxArea = 0;
    List<Rect> activeList = new ArrayList<>(50);
    List<Rect> newActive = new ArrayList<>(50);
    byte[] workRow = new byte[bw];
    int cellIndex = 0;
    for (var y = 0; y < bh; y++, cellIndex += bw) {
      pr(VERT_SP, "sweep:", y);

      // Copy sweep row into work buffer
      for (var x = 0; x < bw; x++)
        workRow[x] = sc[cellIndex + x];

      for (var r : activeList) {
        boolean retain = true;
        var prevBlocker = r.x - 1;
        for (var x = r.x; x < r.xe; x++) {
          if (workRow[x] == 0) {
            // we've encountered an obstacle above this rect.
            retain = false;
            // use a vertical slice of this rect as a new rect
            addNewRect(y, newActive, prevBlocker + 1, x, r.y);
            prevBlocker = x;
          }
        }
        if (!retain) {
          // add rightmost vertical slice of blocked rect
          if (workRow[r.xe - 1] != 0)
            addNewRect(y, newActive, prevBlocker + 1, r.xe, r.y);
          var area = r.area();
          if (area > maxArea)
            maxArea = area;
        }
        if (retain) {
          r.ye++;
          newActive.add(r);
          // paint this rectangle into the work row so we don't spawn new ones
          for (var x = r.x; x < r.xe; x++) {
            workRow[x] = 2;
          }
        }
      }

      // The leftmost pixel is always zero
      for (var x = 1; x < bw; x++) {
        if (workRow[x] == 1) {
          // spawn a new rectangle of maximal width at this row
          var xe = x + 1;
          while (workRow[xe] != 0)
            xe++;
          while (workRow[x - 1] != 0)
            x--;
          addNewRect(y, newActive, x, xe, y);
          while (x < xe)
            workRow[x++] = 2;
        }
      }

      var tmp = activeList;
      activeList = newActive;
      newActive = tmp;
      newActive.clear();
    }
    return maxArea;
  }

  private void addNewRect(int sweepY, List<Rect> destination, int x, int xe, int y) {
    if (x >= xe)
      return;
    var r = new Rect(x, xe, y, 1 + sweepY);
    destination.add(r);
  }

  private void prepareGrid(char[][] matrix) {
    int gridWidth = matrix[0].length;
    int gridHeight = matrix.length;

    bWidth = gridWidth + 2;
    bHeight = gridHeight + 1;

    byte[] cells = new byte[bWidth * bHeight];
    sCells = cells;
    int ci = 1;
    for (var row : matrix) {
      for (var c : row) {
        if (c == '1')
          cells[ci] = 1;
        ci++;
      }
      ci += 2;
    }
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;

  private static class Rect {
    int x, y, xe, ye;

    public int area() {
      return (ye - y) * (xe - x);
    }

    public Rect(int x, int xe, int y, int ye) {
      this.x = x;
      this.xe = xe;
      this.y = y;
      this.ye = ye;
    }
  }

}
