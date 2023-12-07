
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.base.BasePrinter;

public class P221MaximalSquare {

  public static void main(String[] args) {
    new P221MaximalSquare().run();
  }

  private void run() {
    x(5, 4, "10100 10111 11111 10010", 4);
  }

  private void x(int width, int height, String cells, int expected) {
    cells = cells.replaceAll(" ", "");
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
    showBoard(m, 0, m[0].length, 0, m.length, "initial");

    var result = maximalSquare(m);
    pr(width, "x", height, ": result:", result);
    checkState(expected == result, "expected:", expected);
  }

  private void showBoard(char[][] sMatrix, int x0, int x1, int y0, int y1, Object... messages) {
    pr(VERT_SP, BasePrinter.toString(messages), "x,y", x0, y1, CR,
        "--------------------------------------------");
    for (var y = y0; y < y1; y++) {
      var sb = new StringBuilder();
      var row = sMatrix[y];
      for (var x = x0; x < x1; x++) {
        var c = row[x];
        if (x != x0)
          sb.append(' ');
        sb.append(c == '0' ? "." : "X");
      }
      pr(sb);
    }
    pr("--------------------------------------------");
  }

  public int maximalSquare(char[][] matrix) {
    final var bw = matrix[0].length;
    final var bh = matrix.length;
    int maxArea = 0;

    List<Rect> activeList = new ArrayList<>();

    var boardIntv = new IntervalList(bw);
    var rectIntv = new IntervalList(bw);

    // A list for building the next row's active list
    List<Rect> updatedRects = new ArrayList<>();

    for (var y = 0; y <= bh; y++) {
      //pr(VERT_SP, "sweep:", y, "of bh:", bh);

      // Construct an interval list representing the free columns in the row

      boardIntv.clear();
      // If we're processing the row past the original matrix, treat as if that row existed and was empty;
      // we do this by just leaving the interval list empty
      if (y < bh) {
        var matrixRow = matrix[y];
        for (var x = 0; x < bw; x++) {
          // If there is space here, add it to the interval
          if (matrixRow[x] != '0')
            boardIntv.add(x);
        }
      }

      for (var r : activeList) {
        // Find intervals that would contain the start and stop column for this rectangle
        var aInt = boardIntv.find(r.x);
        var bInt = boardIntv.find(r.xe - 1);
        boolean retain = true;
        if (aInt < 0 || bInt < 0 || aInt != bInt)
          retain = false;

        if (retain) {
          //todo("can we avoid carrying around the height, and instead assign it when removing from the list?");
          updatedRects.add(r);
          continue;
        }

        // The rectangle does not fit within a single gap
        // Spawn slices where the rectangle intersects these gaps

        int startScan = normalize(aInt);
        if (startScan < boardIntv.size()) {
          int stopScan = bInt;
          if (bInt < 0) {
            stopScan = normalize(bInt) - 1; // stop at the interval preceding the one that would be added
          }
          for (var j = startScan; j <= stopScan; j++) {
            var g0 = boardIntv.start(j);
            var g1 = boardIntv.stop(j);
            addNewRect(y, updatedRects, Math.max(r.x, g0), Math.min(r.xe, g1), r.y);
          }
        }
        maxArea = Math.max(maxArea, r.area(y));
      }

      var tmp = activeList;
      activeList = updatedRects;
      updatedRects = tmp;
      updatedRects.clear();

      // While there are gaps that are not occupied by rects in the active list, generate new rects to fill them.
      // Each of these rects has unit height, and should have a width equal to their containing row interval.

      // Build a second gap list from the active list. If we sort the active list by each rect's starting column,
      // this can be done efficiently.

      //todo("optimization: avoid sorting active list when there have not been additions?");
      //todo("why do we not seem to need to sort the active list?");

      {
        final int INTERVAL_NONE = -10000;
        int intervalStart = INTERVAL_NONE;
        int intervalEnd = INTERVAL_NONE;

        rectIntv.clear();
        for (var r : activeList) {
          var x = r.x;
          var xe = r.xe;

          // If this rect starts past the current interval, output an interval
          if (x > intervalEnd) {
            if (intervalEnd != INTERVAL_NONE) {
              rectIntv.addInterval(intervalStart, intervalEnd);
            }
            intervalStart = x;
            intervalEnd = xe;
          } else {
            intervalEnd = Math.max(intervalEnd, xe);
          }
        }
        if (intervalEnd != INTERVAL_NONE)
          rectIntv.addInterval(intervalStart, intervalEnd);
      }

      // Where the active rect intervals disagree with the board's, generate new (maximal) rectangles based on board list.
      // Each interval in the active list *should* be contained by some interval from the board list.

      {
        int rc = 0;

        // Loop until we've consumed all the board intervals.
        for (int bc = 0; bc < boardIntv.size(); bc++) {
          int bx = boardIntv.start(bc);
          int bxe = boardIntv.stop(bc);

          boolean spawn = true;

          // If a next rectangle interval exists, see if it matches the board's interval
          if (rc < rectIntv.size()) {
            var rx = rectIntv.start(rc);
            var rxe = rectIntv.stop(rc);

            // Is this within this board interval?
            if (rxe <= bxe) {
              // If not an exact match, spawn a new rect
              if (rx == bx && rxe == bxe)
                spawn = false;
              // Skip over all rect intervals within this board interval
              rc++;
              while (rc < rectIntv.size() && rectIntv.start(rc) < bxe)
                rc++;
            }
          }
          if (spawn)
            addNewRect(y, activeList, bx, bxe, y);
        }
      }
    }
    return maxArea;

  }

  private static int normalize(int intervalCode) {
    if (intervalCode < 0)
      return -intervalCode - 1;
    return intervalCode;
  }

  private void addNewRect(int sweepY, List<Rect> destination, int x, int xe, int y) {
    if (x >= xe)
      return;
    var r = new Rect(x, xe, y);
    destination.add(r);
  }

  private static class Rect {
    int x, y, xe;

    public int area(int ye) {
      int dim = Math.min(ye - y, xe - x);
      return dim * dim;
    }

    public Rect(int x, int xe, int y) {
      this.x = x;
      this.xe = xe;
      this.y = y;
    }

    @Override
    public String toString() {
      return "[w " + (xe - x) + " pos " + x + " " + y + "]";
    }
  }

  /**
   * A data structure that maintains a sorted list of integer intervals, in the
   * form of pairs:
   * 
   * [start0, end0, start1, end1 ...]
   * 
   * where each interval includes startx and endx-1 (i.e., endx is exclusive),
   * and endx < start(x+1)
   *
   */
  private static class IntervalList {

    IntervalList(int maxIntervals) {
      intervals = new int[(maxIntervals + 1) << 1];
    }

    public void clear() {
      mSize = 0;
    }

    public void add(int x) {
      addInterval(x, x + 1);
    }

    /**
     * Determine the index of the interval that contains x, or if x is not in an
     * interval, -1 minus the insertion position of a new interval that would
     * contain x
     */
    public int find(int x) {
      int min = 0;
      int max = mSize;
      while (min < max) {
        int peek = (min + max) / 2;
        var cursor = peek << 1;
        int start = intervals[cursor];
        int stop = intervals[cursor + 1];
        if (x >= start && x < stop)
          return peek;
        if (x < start)
          max = peek;
        else
          min = peek + 1;
      }
      return -1 - max;
    }

    public void addInterval(int x, int xe) {
      int c = mSize << 1;
      if (c == 0 || x > intervals[c - 1]) {
        intervals[c] = x;
        intervals[c + 1] = xe;
        mSize++;
      } else {
        intervals[c - 1] = xe;
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("Intervals{ ");
      var k = sb.length();
      for (int i = 0; i < mSize; i++) {
        var c = i << 1;
        int gStart = intervals[c];
        int gEnd = intervals[c + 1];
        sb.append(spaces(gStart * 5 - (sb.length() - k)));
        sb.append('[');
        sb.append(gStart);
        if (gEnd - 1 > gStart) {
          sb.append("..");
          sb.append(gEnd - 1);
        }
        sb.append(']');
      }
      sb.append('}');
      return sb.toString();
    }

    public int start(int i) {
      return intervals[i << 1];
    }

    public int stop(int i) {
      return intervals[(i << 1) + 1];
    }

    public int size() {
      return mSize;
    }

    private final int[] intervals;
    private int mSize;
  }

}
