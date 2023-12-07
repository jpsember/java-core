
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import js.base.BasePrinter;

public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  private void run() {

    x(5, 4, "10100101111111110010", 6);

    x(3, 3, "111" //
        + "101" //
        + "111" //
        , 3);

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

  //  /* private */ void showGapList(GapList gapList, Object... messages) {
  //    pr(VERT_SP, "Gap list:", BasePrinter.toString(messages));
  //    var sb = new StringBuilder();
  //    int c = 0;
  //    var gaps = gapList.gaps;
  //    while (true) {
  //      int gStart = gaps[c];
  //      int gEnd = gaps[c + 1];
  //      c += 2;
  //      if (gStart == GAP_STOP)
  //        break;
  //      sb.append(spaces(gStart * 5 - sb.length()));
  //      sb.append('[');
  //      sb.append(gStart);
  //      if (gEnd - 1 > gStart) {
  //        sb.append("..");
  //        sb.append(gEnd - 1);
  //      }
  //      sb.append(']');
  //    }
  //    pr(sb);
  //  }
  // ------------------------------------------------------------------

  private static final int GAP_STOP = 10000;

  private static class IntervalList {
    IntervalList(int maxIntervals) {
      intervals = new int[(maxIntervals + 1) * 2];
      done();
    }

    public void clear() {
      mCurrentStart = -1;
      cursor = 0;
    }

    public void add(int x) {
      if (mCurrentStart < 0) {
        mCurrentStart = x;
        mCurrentStop = x;
      }
      checkState(x >= mCurrentStop);
      if (x == mCurrentStop) {
        mCurrentStop++;
      } else {
        addEntry(mCurrentStart, mCurrentStop);
        mCurrentStart = x;
        mCurrentStop = x + 1;
      }
    }

    private void addEntry(int start, int stop) {
      intervals[cursor] = start;
      intervals[cursor + 1] = stop;
      cursor += 2;
    }

    public void done() {
      if (mCurrentStart >= 0) {
        addEntry(mCurrentStart, mCurrentStop);
      }
      addEntry(GAP_STOP, GAP_STOP + 2);
    }

    /**
     * Determine the index of the interval that contains x, or if x is not in an
     * interval, -1 minus the insertion position of a new interval that would
     * contain x
     */
    public int findz(int x) {
      todo("binary search");
      checkState(x >= 0 && x < GAP_STOP);
      int index = 0;
      while (true) {
        var cursor = index << 1;
        int empStart = intervals[cursor];
        if (x < empStart)
          return -1 - index;
        int empStop = intervals[cursor + 1];
        if (x < empStop)
          return index;
        index++;
      }
    }

    public int value(int j) {
      return intervals[j];
    }

    public void addInterval(int x, int xe) {
      pr("interval list, add:", x, "..", xe);
      checkArgument(x >= 0 && xe > x);
      if (mCurrentStart < 0) {
        mCurrentStart = x;
        mCurrentStop = xe;
      } else {
        checkArgument(x >= mCurrentStop);
        if (x == mCurrentStop) {
          mCurrentStop = xe;
        } else {
          addEntry(mCurrentStart, mCurrentStop);
          mCurrentStart = x;
          mCurrentStop = xe;
        }
      }
      pr("...after add:", this);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("GapList< ");
      var k = sb.length();
      int c = 0;
      todo("simplify by getting rid of GAP_STOP");
      while (true) {
        int gStart = intervals[c];
        int gEnd = intervals[c + 1];
        c += 2;
        if (gStart == GAP_STOP)
          break;
        sb.append(spaces(gStart * 5 - (sb.length() - k)));
        sb.append('[');
        sb.append(gStart);
        if (gEnd - 1 > gStart) {
          sb.append("..");
          sb.append(gEnd - 1);
        }
        sb.append(']');
      }
      sb.append('>');
      return sb.toString();
    }

    private final int[] intervals;
    private int mCurrentStart;
    private int mCurrentStop;
    private int cursor;

    public int start(int i) {
      return intervals[i << 1];
    }

    public int stop(int i) {
      return intervals[(i << 1) + 1];
    }

    public int size() {
      return cursor >> 1;
    }
  }

  public int maximalRectangle(char[][] matrix) {
    prepareGrid(matrix);
    final var bw = bWidth;
    final var bh = bHeight;
    final var sc = sCells;
    showBoard(0, bw, 0, bh, "initial");

    int maxArea = 0;
    bActiveList.clear();

    var boardRowGaps = new IntervalList(bw);
    var activeRectGaps = new IntervalList(bw);

    var newRects = new ArrayList<Rect>();
    var removeRects = new ArrayList<Rect>();

    int cellIndex = 0;
    for (var y = 0; y < bh; y++, cellIndex += bw) {
      pr(VERT_SP, "sweep:", y);

      // Build compact list of empty pixels

      {
        var g = boardRowGaps;
        g.clear();

        //        int cursor = 0;
        //        var emptyCount = 0;
        //        var emptyStart = 0;
        for (var x = 0; x < bw; x++) {
          if (sc[cellIndex + x] != 0) {
            g.add(x);
            //            if (emptyCount == 0) {
            //              emptyStart = x;
            //              emptyCount = 0;
            //            }
            //            emptyCount++;
            //          } else {
            //            if (emptyCount != 0) {
            //              boardRowGaps[cursor] = emptyStart;
            //              boardRowGaps[cursor + 1] = emptyStart + emptyCount;
            //              cursor += 2;
            //            }
            //          }
          }
        }
        g.done();
        //        // Mark end of list with a gap that is way off screen
        //        boardRowGaps[cursor] = GAP_STOP;
        //        boardRowGaps[cursor + 1] = GAP_STOP;
      }

      newRects.clear();
      removeRects.clear();

      pr("activeList:", bActiveList);

      for (var r : bActiveList) {
        boolean retain = true;
        checkInf();

        pr("examining active rect", r);
        pr("boardRowGaps:", boardRowGaps);
        todo("we can maintain a pointer into the empty list since the rects are sorted by x");
        var aInt = boardRowGaps.findz(r.x);
        pr("...index of interval containing r.x", r.x, aInt);
        if (aInt < 0) {
          retain = false;
          aInt = -aInt - 1;
        }
        if (aInt == boardRowGaps.size()) {
          retain = false;
        } else {
          var bInt = boardRowGaps.findz(r.xe - 1);
          pr("...index of interval containing r.xe-1", r.xe - 1, bInt);
          if (bInt < 0) {
            retain = false;
            bInt = -bInt - 2; // Move back to previous gap
          }
          pr("normalized:", aInt, bInt);

          checkState(bInt >= aInt);
          if (aInt != bInt)
            retain = false;

          if (!retain) {
            // The rectangle does not fit within a single gap
            // Spawn slices where the rectangle intersects these gaps
            for (var j = aInt; j <= bInt; j++) {
              var g0 = boardRowGaps.start(j);
              var g1 = boardRowGaps.stop(j);
              var effx0 = Math.max(r.x, g0);
              var effx1 = Math.min(r.xe, g1);
              if (effx0 < effx1) {
                addNewRect(y, newRects, effx0, effx1, r.y, "vert slice");
              }
            }
          }
          //          var x = r.x;
          //          while (x < r.xe) {
          //            checkInf();
          //            // do we overlap the current gap?
          //            if (x >= boardRowGaps.value(j)) {
          //              var xe = Math.min(boardRowGaps.value(j + 1), r.xe);
          //              addNewRect(y, newRects, x, xe, r.y, "vert slice");
          //              j += 2;
          //              x = boardRowGaps.value(j);
          //              pr("...advanced x to:", x);
          //            } else {
          //              break;
          //            }
          //          }
          //          
          //          var prevBlocker = r.x - 1;
          //
          //          for (var x = r.x; x < r.xe; x++) {
          //            if (workRow[x] == 0) {
          //              // we've encountered an obstacle above this rect.
          //              retain = false;
          //              // use a vertical slice of this rect as a new rect
          //              addNewRect(y, newRects, prevBlocker + 1, x, r.y, "vert slice");
          //              prevBlocker = x;
          //            }
          //          }
          //
          //          if (!retain) {
          //            // add rightmost vertical slice of blocked rect
          //            if (workRow[r.xe - 1] != 0) {
          //              addNewRect(y, newRects, prevBlocker + 1, r.xe, r.y, "rightmost vert slice");
          //            }
          //            var area = r.area();
          //            if (area > maxArea)
          //              maxArea = area;
          //          }
        }
        if (!retain) {
          var area = r.area();
          if (area > maxArea)
            maxArea = area;
        }
        removeRects.add(r);
        pr("...removing rect:", r);
        //      if (retain) {
        //        r.ye++;
        //        newRects.add(r);
        //        // paint this rectangle into the work row so we don't spawn new ones
        //        for (var x = r.x; x < r.xe; x++) {
        //          workRow[x] = 2;
        //        }
        //      }
      }

      bActiveList.removeAll(removeRects);

      // While there are gaps that are not occupied by rects in the active list, generate new rects to fill them

      // Build a second gap list from the active list
      {
        var el = activeRectGaps;
        el.clear();
        //        var gapStart = -1;
        //        var gapStop = 0;
        for (var r : bActiveList) {
          el.addInterval(r.x, r.xe);
          //          if (gapStart < 0) {
          //            gapStart = r.x;
          //            gapStop = r.xe;
          //          }
          //          if (gapStop < r.x) {
          //            el[cursor] = gapStart;
          //            el[cursor + 1] = gapStop;
          //            cursor += 2;
          //
          //            gapStart = r.x;
          //            gapStop = r.xe;
          //          } else {
          //            if (gapStop < r.xe)
          //              gapStop = r.xe;
          //          }
        }
        el.done();
      }
      pr("built active list gaps from", bActiveList.size(), "rects:", activeRectGaps);

      // Where the active list's gap list is "less" than the board's, generate new (maximal) rectangles 

      pr("spawning new rects to fill gaps:");
      pr("board:", boardRowGaps);
      pr("rects:", activeRectGaps);
      {
        var activeGaps = activeRectGaps;
        var boardGaps = boardRowGaps;
        int c1 = 0;
        int c2 = 0;

        while (boardGaps.value(c1) < GAP_STOP) {
          if (activeGaps.value(c2) > boardGaps.value(c1)) {
            checkInf();

            int x = boardGaps.value(c1);
            int xe = Math.min(boardGaps.value(c1 + 1), activeGaps.value(c2 + 1));
            addNewRect(y, newRects, x, xe, y, "spawned new occupying unused pixel(s)", x);
            c2 += 2;
            continue;
          }
          c1 += 2;
          c2 += 2;
        }
      }

      bActiveList.addAll(newRects);
    }
    return maxArea;

  }

  int inf;

  void checkInf() {
    checkState(inf++ < 1000);
  }

  //  /**
  //   * Determine the index of the gap that contains x, or if x is not in a gap,
  //   * the first gap beyond x (or the pointer to the end of the list is there is
  //   * no next gap)
  //   */
  //  private int posWithinEmptyList(int x, int[] empList) {
  //    todo("binary search");
  //    int cursor = 0;
  //    while (true) {
  //      checkInf();
  //      int empStop = empList[cursor + 1];
  //      pr("empStop:", empStop, "x:", x, "cursor:", cursor);
  //      if (x < empStop)
  //        break;
  //      cursor += 2;
  //    }
  //    return cursor;
  //  }

  private void addNewRect(int sweepY, List<Rect> destination, int x, int xe, int y, Object... messages) {
    if (x >= xe)
      return;
    var r = new Rect(x, xe, y, 1 + sweepY);
    pr("...add new rect (", BasePrinter.toString(messages), ");", r);
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

  private static final Comparator<Rect> RECT_COMPARATOR = new Comparator<>() {
    @Override
    public int compare(Rect o1, Rect o2) {
      if (o1 == o2)
        return 0;
      int diff = o1.x - o2.x;
      if (diff == 0)
        diff = o1.xe - o2.xe;
      return diff;
    }
  };

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
  private static Set<Rect> bActiveList = new TreeSet<Rect>(RECT_COMPARATOR);

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

    @Override
    public String toString() {
      return "[" + (xe - x) + " x " + (ye - y) + " at " + x + " " + y + "]";
    }
  }

}
