
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.json.JSList;

public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  private void run() {

    x(3, 3, "111" //
        + "101" //
        + "111" //
        , 3);

    x(5, 5, "10100" //
        + "10111" //
        + "11101" //
        + "11111" //
        + "10010", 6);

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

  private void printGrid(String prompt) {
    printGrid(prompt, null);
  }

  private void printGrid(String prompt, Pt cell) {
    pr("Grid,", prompt, CR, "--------------------------------------------------");

    var sb = new StringBuilder();
    for (var y = 0; y < bHeight; y++) {
      for (var x = 0; x < bWidth; x++) {
        byte c = cells[y * bWidth + x];
        if (cell != null && cell.x == x && cell.y == y) {
          sb.append('>');
        } else
          sb.append(' ');
        sb.append((char) (c == 0 ? '.' : '0' + c));
      }
      sb.append('\n');
    }
    pr(sb);

    pr("--------------------------------------------------");

  }

  public int maximalRectangle(char[][] matrix) {

    // Scale up our grid so each matrix cell occupies 4 grid cells.
    // This is so holes are handled properly.
    int gridWidth = matrix[0].length;
    int gridHeight = matrix.length;
    bWidth = gridWidth * 2 + 2;
    bHeight = gridHeight * 2 + 2;
    byte[] cells = new byte[bWidth * bHeight];
    this.cells = cells;
    int cc = bWidth + 1;
    for (var row : matrix) {
      var c0 = cc;
      for (var c : row) {
        if (c == '1') {
          cells[cc] = 1;
          cells[cc + 1] = 1;
        }
        cc += 2;
      }
      // duplicate the 'small' cell row into the next
      for (int x = bWidth - 1; x >= 0; x--)
        cells[c0 + bWidth + x] = cells[c0 + x];

      cc += bWidth + 2;
    }

    printGrid("constructed");

    // Construct polygons

    int polyColor = 2;
    var start = new Pt(1, 1);
    int cellInd = start.cellIndex();
    //bWidth + 1;
    int scanStop = bWidth * (bHeight - 1);

    //    var z = scanStop+3;
    while (cellInd < scanStop) {
      //      checkState(z-- > 0);
      pr("cell:", cellInd, "<", scanStop);

      //for (while (cellInd < int i = 0; i < cells.length; i++) {
      var cLeft = cells[cellInd - 1];
      var cRight = cells[cellInd];
      pr(" ", cLeft, cRight);
      if (cLeft == 0 && cRight == 1) {
        // This is a North edge
        extractPoly(cellInd, NORTH, polyColor);
        polyColor++;
        printGrid("after poly extract");

      } else if (cLeft == 1 && cRight == 0) {
        printGrid("found south edge", PtWithIndex(cellInd));
        // This is a South edge
        extractPoly(cellInd - 1, SOUTH, polyColor);
        polyColor++;
        printGrid("after poly extract");
      }
      cellInd++;
      //      x++;
      //      if (x == bWidth) {
      //        x = 0;
      //        y++;
      //        if (y == bHeight - 1)
      //          break;
      //      }
    }

    printGrid("after poly extract");
    return -1;
  }

  private Pt coords(int cellIndex) {
    return new Pt(cellIndex % bWidth, cellIndex % bHeight);
  }

  private static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

  private static Pt PtWithIndex(int index) {
    return new Pt(index % bWidth, index / bHeight);
  }

  private static class Pt {
    int x;
    int y;

    Pt(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int cellIndex() {
      return y * bWidth + x;
    }

    Pt move(int dir) {
      return new Pt(x + xmoves[dir], y + ymoves[dir]);
    }

    private static int[] xmoves = { 0, 1, 0, -1 };
    private static int[] ymoves = { -1, 0, 1, 0 };

    int lookForward(int dir) {
      return cells[(y + ymoves[dir]) * bWidth + (x + xmoves[dir])];
    }

    int lookForwardLeft(int dir) {

      return cells[(y + ymoves[dir] + ymoves[turnLeft(dir)]) * bWidth
          + (x + xmoves[dir] + xmoves[turnLeft(dir)])];
    }

    public boolean is(Pt p) {
      return x == p.x && y == p.y;
    }

    private void paint(int value) {
      cells[cellIndex()] = (byte) value;
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }

  }

  private static int turnLeft(int dir) {
    return (dir - 1) & 3;
  }

  private static int turnRight(int dir) {
    return (dir + 1) & 3;
  }

  private List<Pt> extractPoly(int cellIndex, int dir, int polyColor) {

    List<Pt> poly = new ArrayList<>();

    var c = PtWithIndex(cellIndex);
    var cStart = c;

    pr("extractPoly, cellIndex:", cellIndex, "pt:", c, "color:", polyColor);

    var z = 1000;
    while (true) {
      checkState(z-- > 0);

      // Paint this cell with the poly number 
      c.paint(polyColor);
      pr("...look forward, dir", dir, ":", c.lookForward(dir));
      printGrid("about to look forward", c);

      if (c.lookForward(dir) == 0) {
        dir = turnRight(dir);
        pr("...turned right");
        printGrid("after turned right", c);
      } else if (c.lookForwardLeft(dir) != 0) {
        pr("lookForwardLeft was:", c.lookForwardLeft(dir));
        printGrid("after look fwd", c);
        // We need to move forward before turning left.
        {
          poly.add(c);
          c = c.move(dir);
        }
        dir = turnLeft(dir);
        pr("...turned left");
      } else {
        poly.add(c);
        pr("...added:", c);
        c = c.move(dir);
        pr("...moved to:", c);
        printGrid("after move", c);
        if (c.is(cStart))
          break;
      }

    }
    return poly;
  }

  private static byte[] cells;
  private static int bWidth;
  private static int bHeight;
}
