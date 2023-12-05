
package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

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

  // ------------------------------------------------------------------

  public int maximalRectangle(char[][] matrix) {
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

    // Scan for concave vertices

    return subscan(0, bWidth, 0, bHeight, 0);
  }

  int depth = 0;

  private int subscan(int x0, int x1, int y0, int y1, int maxArea) {
    depth += 2;
    var x = subscan0(x0, x1, y0, y1, maxArea);
    depth -= 2;
    return x;
  }

  private int subscan0(int x0, int x1, int y0, int y1, int maxArea) {
    pr(spaces(depth), "subscan x", x0, "...", x1, " y", y0, "...", y1, "max area:", maxArea);
    if (x0 >= x1 || y0 >= y1)
      return maxArea;

    var cells = sCells;
    var bRow = bWidth;
    int cellInd = bRow * y0;
    int minx = x1;
    int miny = y1;
    int maxx = x0 - 1;
    int maxy = y0 - 1;

    for (int y = y0; y < y1; y++, cellInd += bRow) {
      for (int x = x0; x < x1; x++) {
        int c = cellInd + x;
        var cCurr = cells[c];
        if (cCurr != 1) {
          continue;
        }
        if (minx > x)
          minx = x;
        if (maxx < x)
          maxx = x;
        if (miny > y)
          miny = y;
        if (maxy < y)
          maxy = y;
        if (x > x0) {
          var nbr = c - 1;
          if (cells[nbr] == 0) {
            if (y > y0 && cells[nbr - bRow] == 1) {
              pr(spaces(depth), "found conc at", x, y, x - 1, y - 1);
              maxArea = subscan(x0, x1, y0, y, maxArea);
              maxArea = subscan(x0, x1, y, y1, maxArea);
              maxArea = subscan(x0, x, y0, y1, maxArea);
              maxArea = subscan(x, x1, y0, y1, maxArea);
              return maxArea;
            } else if (y + 1 < y1 && cells[nbr + bRow] == 1) {
              pr(spaces(depth), "found conc at", x, y, x - 1, y + 1);
              maxArea = subscan(x0, x1, y0, y + 1, maxArea);
              maxArea = subscan(x0, x1, y + 1, y1, maxArea);
              maxArea = subscan(x0, x, y0, y1, maxArea);
              maxArea = subscan(x, x1, y0, y1, maxArea);
              return maxArea;
            }
          }
        }
        if (x + 1 < x1) {
          var nbr = c + 1;
          if (cells[nbr] == 0) {
            if (y > y0 && cells[nbr - bRow] == 1) {
              pr(spaces(depth), "found conc at", x, y, x + 1, y - 1);
              maxArea = subscan(x0, x1, y0, y, maxArea);
              maxArea = subscan(x0, x1, y, y1, maxArea);
              maxArea = subscan(x0, x + 1, y0, y1, maxArea);
              maxArea = subscan(x + 1, x1, y0, y1, maxArea);
              return maxArea;
            } else if (y + 1 < y1 && cells[nbr + bRow] == 1) {
              pr(spaces(depth), "found conc at", x, y, x + 1, y + 1);
              maxArea = subscan(x0, x1, y0, y + 1, maxArea);
              maxArea = subscan(x0, x1, y + 1, y1, maxArea);
              maxArea = subscan(x0, x + 1, y0, y1, maxArea);
              maxArea = subscan(x + 1, x1, y0, y1, maxArea);
              return maxArea;
            }
          }
        }
      }
    }
    pr(spaces(depth), "minx:", minx, "maxx:", maxx, "miny:", miny, "maxy:", maxy);
    // We didn't find any concave vertices; the 1's form a rectangle
    if (minx <= maxx) {
      var area = (maxx + 1 - minx) * (maxy + 1 - miny);
      pr(spaces(depth), "this rect area is", area);
      if (maxArea < area) {
        maxArea = area;
      }
    }
    return maxArea;
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
}
