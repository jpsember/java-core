
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

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

    //  x(3, 2, "111010", 3);
    //halt();

    //    x(3, 2, "110111", 4);
    //
    //    x(3, 3, "111111111", 9);

    x(5, 4, "10100101111111110010", 6);
    halt();
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

  private void printGrid(String prompt) {
    printGrid(prompt, null);
  }

  private void printGrid(String prompt, Pt cell) {
    pr("Grid,", prompt, CR, "--------------------------------------------------");

    var sb = new StringBuilder();
    for (var y = 0; y < bHeight; y++) {
      for (var x = 0; x < bWidth; x++) {
        byte c = sCells[y * bWidth + x];
        sb.append(' ');
        if (cell != null && cell.x == x && cell.y == y) {
          if (cell.dir >= 0)
            sb.append("nesw".charAt(cell.dir));
          else
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
    sCells = cells;
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

    List<List<Pt>> polygons = new ArrayList<>();

    int polyColor = 2;
    //var start = new Pt(1, 1, 0);
    int cellInd = bWidth + 1; // (1,1)
    int scanStop = bWidth * (bHeight - 1);

    while (cellInd < scanStop) {
      var cLeft = cells[cellInd - 1];
      var cRight = cells[cellInd];
      if (cLeft == 0 && cRight == 1) {
        polygons.add(extractPoly(indexToCell(cellInd, NORTH), polyColor));
        polyColor++;
        printGrid("after poly extract");
      } else if (cLeft == 1 && cRight == 0) {
        polygons.add(extractPoly(indexToCell(cellInd - 1, SOUTH), polyColor));
        polyColor++;
        printGrid("after poly extract");
      }
      cellInd++;
    }

    List<List<Pt>> res = new ArrayList<>();
    for (var poly : polygons) {
      res.addAll(splitAtConcaveVert(poly));
    }

    var newPolygons = res;
    //    var newPolygons = splitAtConcaveVertices(polygons);

    pr("orig poly count :", polygons.size());
    pr("split poly count:", newPolygons.size());

    int maxArea = 0;
    for (var p : newPolygons) {
      pr(VERT_SP, "poly:", p);

      var f = p.get(0);
      int xmin = f.x;
      int xmax = xmin;
      int ymin = f.y;
      int ymax = ymin;
      for (var pt : p) {
        xmin = Math.min(xmin, pt.x);
        xmax = Math.max(xmax, pt.x);
        ymin = Math.min(ymin, pt.y);
        ymax = Math.max(ymax, pt.y);
      }
      var area = (xmax - xmin) * (ymax - ymin);
      pr("area:", area);
      maxArea = Math.max(maxArea, area);
    }

    // Divide area by the area of the scaling factor (squared)
    return maxArea / 4;
  }

  private static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

  private static Pt indexToCell(int index, int dir) {
    return new Pt(index % bWidth, index / bWidth, dir);
  }

  private static class Poly {
    List<Pt> points = new ArrayList<>();
  }

  private static class Pt {
    int x;
    int y;
    int dir;

    Pt(int x, int y) {
      this(x, y, -1);
    }

    Pt(int x, int y, int dir) {
      this.x = x;
      this.y = y;
      this.dir = dir;
    }

    public int cellIndex() {
      return y * bWidth + x;
    }

    Pt move() {
      return new Pt(x + xmoves[dir], y + ymoves[dir], dir);
    }

    Pt turnLeft() {
      return new Pt(x + xturnleft[dir], y + yturnleft[dir], (dir - 1) & 3);
    }

    Pt turnRight() {
      return new Pt(x, y, (dir + 1) & 3);
    }

    private static int[] xmoves = { 0, 1, 0, -1 };
    private static int[] ymoves = { -1, 0, 1, 0 };
    private static int[] xedgeoff = { 0, 0, 1, 1 };
    private static int[] yedgeoff = { 1, 0, 0, 1 };

    int lookForward() {
      return sCells[(y + ymoves[dir]) * bWidth + (x + xmoves[dir])];
    }

    int lookForwardLeft() {
      return sCells[(y + ymoves[dir] + ymoves[sturnLeft(dir)]) * bWidth
          + (x + xmoves[dir] + xmoves[sturnLeft(dir)])];
    }

    public boolean is(Pt p) {
      return x == p.x && y == p.y;
    }

    private void paint(int value) {
      sCells[cellIndex()] = (byte) value;
    }

    @Override
    public String toString() {
      if (dir < 0)
        return "(" + x + "," + y + ")";
      return "(" + x + "," + y + "," + "NESW".charAt(dir) + ")";
    }

    public Pt edgeStart() {
      return new Pt(x + xedgeoff[dir], y + yedgeoff[dir], -1);
    }

  }

  private static int sturnLeft(int dir) {
    return (dir - 1) & 3;
  }

  private List<Pt> extractPoly(Pt cell, int polyColor) {

    final boolean db = true;

    List<Pt> poly = new ArrayList<>();

    pr("extractPoly, cell:", cell, "color:", polyColor);

    var cv = cell.edgeStart();
    poly.add(cv);

    var z = 1000;
    Pt prevCell = null;
    Pt firstVert = null;
    while (true) {
      if (prevCell != null) {
        if (prevCell.x == cell.x && prevCell.y == cell.y && prevCell.dir == cell.dir)
          badState("no progress");
      }
      prevCell = cell;
      var es = cell.edgeStart();
      if (firstVert != null) {
        if (es.is(firstVert))
          break;
      } else
        firstVert = es;
      poly.add(es);
      printGrid("extract loop, prevVert:" + firstVert, cell);
      pr("added point:", es, "now:", poly);
      //      if (poly.size() > 23)
      //        halt();

      if (db)
        checkState(z-- > 0);

      if (db) {
        pr("...look forward:", cell);
      }

      if (cell.lookForward() == 0) {
        cell = cell.turnRight();
        if (db) {
          pr("...turned right, now", cell);
        }
      } else if (cell.lookForwardLeft() != 0) {

        // Paint this cell with the poly number 
        cell.paint(polyColor);

        cell = cell.turnLeft();
        pr("turned left, now", cell);
      } else {
        // Paint this cell with the poly number 
        cell.paint(polyColor);
        cell = cell.move();
        if (db) {
          pr("...moved, now", cell);
        }
      }
    }
    return mergeEdges(poly);
  }

  private List<Pt> mergeEdges(List<Pt> poly) {
    pr("merging edges:", poly);

    var result = new ArrayList<Pt>();
    var prevPt2 = poly.get(0);
    var prevPt1 = poly.get(1);
    int dir = determineDir(prevPt2, prevPt1);
    int i = 2;
    Pt firstAdded = null;
    while (true) {
      var pt = poly.get(i);
      i = (i + 1) % poly.size();

      int newDir = determineDir(prevPt1, pt);
      if (newDir != dir) {
        dir = newDir;
        if (firstAdded == null) {
          firstAdded = prevPt1;
        } else if (prevPt1 == firstAdded)
          break;
        result.add(prevPt1);
      }
      prevPt1 = pt;
    }
    pr("merged:", result);
    return result;
  }

  private int determineDir(Pt a, Pt b) {
    int dx = b.x - a.x;
    int dy = b.y - a.y;
    if (dx != 0 && dy != 0) {
      badArg("a:", a, "b:", b);
    }
    if (dx > 0)
      return EAST;
    else if (dx < 0)
      return WEST;
    if (dy > 0)
      return SOUTH;
    return NORTH;
  }

  private static <T> T peekLast(List<T> list, int dist) {
    return list.get(list.size() - 1 - dist);
  }

  private int inf;

  private List<List<Pt>> splitAtConcaveVert(List<Pt> poly) {
    pr("splitAtConcaveVertices");

    final boolean db = false;

    List<List<Pt>> result = new ArrayList<>();
    Pt splitVert = null;

    if (db)
      pr("poly:", poly);
    var prev2 = peekLast(poly, 1);
    var prev1 = peekLast(poly, 0);
    var prevDir = determineDir(prev2, prev1);
    for (var pt : poly) {
      var newDir = determineDir(prev1, pt);
      if (((newDir - prevDir) & 3) == 3) {
        if (db)
          pr("pt:", pt, "newDir:", newDir, "prev:", prevDir, "adding split at", prev1);
        splitVert = prev1;
        break;
      }
      prevDir = newDir;
      prev1 = pt;
    }
    checkState(++inf < 100);

    if (splitVert == null) {
      result.add(poly);
    } else {
      var res = new ArrayList<List<Pt>>();
      splitPoly(poly, splitVert.y, res, false);
      splitPoly(poly, splitVert.x, res, true);

      for (var subpoly : res) {
        result.addAll(splitAtConcaveVert(subpoly));
      }
    }
    return result;
    //
    //    //halt("after horz splits:",polygons);
    //
    //    if (!vertSplits.isEmpty()) {
    //      var result = new ArrayList<List<Pt>>();
    //      for (var x : vertSplits) {
    //        for (var poly : polygons) {
    //          splitPoly(poly, x, result, true);
    //        }
    //      }
    //      polygons = result;
    //    }
    //
    //    pr("performed splits:", VERT_SP, polygons);
    //    //halt("after all splits:",polygons);
    //
    //    return polygons;
  }

  //  private List<List<Pt>> splitAtConcaveVertices(List<List<Pt>> polygons) {
  //    pr("splitAtConcaveVertices");
  //
  //    final boolean db = false;
  //
  //    // Determine where polygons must be split
  //    Set<Integer> horzSplits = new HashSet<>();
  //    Set<Integer> vertSplits = new HashSet<>();
  //
  //    List<Pt> splitVerts = new ArrayList<>();
  //
  //    for (var poly : polygons) {
  //      if (db)
  //        pr("poly:", poly);
  //      var prev2 = peekLast(poly, 1);
  //      var prev1 = peekLast(poly, 0);
  //      var prevDir = determineDir(prev2, prev1);
  //      for (var pt : poly) {
  //        var newDir = determineDir(prev1, pt);
  //        if (((newDir - prevDir) & 3) == 3) {
  //          if (db)
  //            pr("pt:", pt, "newDir:", newDir, "prev:", prevDir, "adding split at", prev1);
  //          splitVerts.add(prev1);
  //          //          horzSplits.add(prev1.y);
  //          //          vertSplits.add(prev1.x);
  //        }
  //        prevDir = newDir;
  //        prev1 = pt;
  //      }
  //
  //    }
  //
  //    pr("split verts:", splitVerts);
  //    //    pr("horzSplits:", horzSplits);
  //    //    pr("vertSplits:", vertSplits);
  //
  //    var result = new ArrayList<List<Pt>>();
  //
  //    for (var sv : splitVerts) {
  //
  //    }
  //
  //    if (!horzSplits.isEmpty()) {
  //      var result = new ArrayList<List<Pt>>();
  //      for (var y : horzSplits) {
  //        for (var poly : polygons) {
  //          splitPoly(poly, y, result, false);
  //        }
  //      }
  //      polygons = result;
  //    }
  //
  //    //halt("after horz splits:",polygons);
  //
  //    if (!vertSplits.isEmpty()) {
  //      var result = new ArrayList<List<Pt>>();
  //      for (var x : vertSplits) {
  //        for (var poly : polygons) {
  //          splitPoly(poly, x, result, true);
  //        }
  //      }
  //      polygons = result;
  //    }
  //
  //    pr("performed splits:", VERT_SP, polygons);
  //    //halt("after all splits:",polygons);
  //
  //    return polygons;
  //  }

  private static final int LEFT = 1;
  private static final int NONE = 0;
  private static final int RIGHT = -1;

  private static int signum(int amt) {
    return amt < 0 ? RIGHT : ((amt > 0) ? LEFT : NONE);
  }

  private static String Side(int side) {
    return sides[side - RIGHT];
  }

  private static final String[] sides = { "RIGHT", "NONE ", "LEFT " };

  static int z;

  private static void splitPoly(List<Pt> poly, int splitCoord, List<List<Pt>> result, boolean verticalLine) {

    pr(VERT_SP, "split poly:", poly);
    pr("at ", verticalLine ? "vertical " : "horizontal ", "line", splitCoord);

    List<Pt> left = new ArrayList<>(poly.size());
    List<Pt> right = new ArrayList<>(poly.size());

    // Start at a vertex on an edge that is strictly to one side
    int currentSide = NONE;

    //    var lastVert = peekLast(poly, 0);
    //    var lastSide = signum(verticalLine ? lastVert.x - splitCoord : lastVert.y - splitCoord);
    int startVertex;
    for (startVertex = 0; startVertex < poly.size(); startVertex++) {
      var pt = poly.get(startVertex);
      var pt2 = poly.get((startVertex + 1) % poly.size());

      //    for (var pt : poly) {

      //prevSide = signum(verticalLine ? pt.x - splitCoord : pt.y - splitCoord);
      var newCurrentSide = signum(verticalLine ? pt.x - splitCoord : pt.y - splitCoord);
      if (newCurrentSide != NONE) {
        var nextSide = signum(verticalLine ? pt2.x - splitCoord : pt2.y - splitCoord);
        if (newCurrentSide == nextSide) {
          currentSide = newCurrentSide;
          break;
        }
      }
    }
    checkState(currentSide != NONE);
    pr("starting current side:", Side(currentSide));

    for (int q = 0; q <= poly.size(); q++) {
      int vi = (q + startVertex) % poly.size();
      var polyPt = poly.get(vi);

      pr(VERT_SP, "vi:", vi, "of length", poly.size(), "polyPt:", polyPt);

      int newSide = signum(verticalLine ? polyPt.x - splitCoord : polyPt.y - splitCoord);
      Pt crossPt = null;
      if (newSide != currentSide) {
        if (verticalLine) {
          crossPt = new Pt(splitCoord, polyPt.y);
          pr("vert line, cross point at split coord:", splitCoord);
        } else {
          crossPt = new Pt(polyPt.x, splitCoord);
          pr("horz line, cross point at split coord:", splitCoord);
        }
      }
      pr("...left :", left);
      pr("...right:", right, VERT_SP);
      pr("...vi:", vi, "pt:", polyPt, "current side:", Side(currentSide), "new side:", Side(newSide),
          "cross:", crossPt);

      switch (newSide) {
      case NONE:
        if (currentSide == LEFT)
          left.add(crossPt);
        else if (currentSide == RIGHT)
          right.add(crossPt);
        break;
      case LEFT:
        if (currentSide == NONE) {
          left.add(crossPt);
          left.add(polyPt);
        } else if (currentSide == LEFT) {
          left.add(polyPt);
        } else {
          right.add(crossPt);
          left.add(crossPt);
          left.add(polyPt);
        }
        break;
      default: /* RIGHT */
        if (currentSide == NONE) {
          right.add(crossPt);
          right.add(polyPt);
        } else if (currentSide == RIGHT) {
          right.add(polyPt);
        } else {
          left.add(crossPt);
          right.add(crossPt);
          right.add(polyPt);
        }
        break;
      }
      pr("after processing vert:");
      pr("...left :", left);
      pr("...right:", right, VERT_SP);

      currentSide = newSide;
    }
    pr("after clipping:");
    pr("...left :", left);
    pr("...right:", right, VERT_SP);

    removeDupLastPt(left);
    removeDupLastPt(right);

    pr("validating left:", left);
    validate(left);
    pr("validating right:", right);
    validate(right);

    pr("**** just split poly:", poly);

    //    if (true && ++z == 4)
    //      halt();

    if (!left.isEmpty())
      result.add(left);
    if (!right.isEmpty())
      result.add(right);
  }

  private static void validate(List<Pt> poly) {
    int s = poly.size();
    if (s == 0)
      return;
    if (s < 4) {
      badState("bad clipped polygon:", poly);
    }

  }

  private static void removeDupLastPt(List<Pt> list) {
    int s = list.size() - 1;
    if (s <= 0)
      return;
    if (list.get(0).is(list.get(s))) {
      // halt("unexpected:",list);
      list.remove(list.size() - 1);
    }
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
}
