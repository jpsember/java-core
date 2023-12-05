
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

    List<Poly> polygons = new ArrayList<>();

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
      } else if (cLeft == 1 && cRight == 0) {
        polygons.add(extractPoly(indexToCell(cellInd - 1, SOUTH), polyColor));
        polyColor++;
      }
      cellInd++;
    }

    List<Poly> res = new ArrayList<>();
    for (var poly : polygons) {
      res.addAll(splitAtConcaveVert(poly));
    }

    var newPolygons = res;

    pr("orig poly count :", polygons.size());
    pr("split poly count:", newPolygons.size());

    int maxArea = 0;
    for (var p : newPolygons) {
      pr(VERT_SP, "poly:", p);
      int area = p.area();
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

  /* private */ static class Poly {
    List<Pt> points = new ArrayList<>();

    boolean filter = true;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[ ");
      for (var p : points) {
        sb.append(p);
        sb.append(' ');
      }
      sb.append(']');
      return sb.toString();
    }

    public void add(Pt pt) {
      var i = points.size() - 1;
      if (i >= 0 && points.get(i).is(pt))
        return;

      if (filter) {
        if (points.size() > 0 && pt.is(points.get(points.size() - 1)))
          badArg("attempt to add duplicate point:", pt, "to:", INDENT, this);
      }
      points.add(pt);
    }

    public Pt getMod(int index) {
      if (index < 0)
        index += points.size();
      checkState(index >= 0);
      return points.get(index % points.size());
    }

    private int area = -1;
    private Pt boundsMin, boundsMax;

    Pt boundsMin() {
      area();
      return boundsMin;
    }

    Pt boundsMax() {
      area();
      return boundsMax;
    }

    public int area() {
      if (area < 0) {
        var f = points.get(0);
        int xmin = f.x;
        int xmax = xmin;
        int ymin = f.y;
        int ymax = ymin;
        for (var pt : points) {
          xmin = Math.min(xmin, pt.x);
          xmax = Math.max(xmax, pt.x);
          ymin = Math.min(ymin, pt.y);
          ymax = Math.max(ymax, pt.y);
        }
        area = (xmax - xmin) * (ymax - ymin);
        boundsMin = new Pt(xmin, ymin);
        boundsMax = new Pt(xmax, ymax);
      }
      return area;
    }

    public List<Pt> vertices() {
      return points;
    }

    public int size() {
      return points.size();
    }

    public void removeDupLastPt() {
      int s = size() - 1;
      if (s <= 0)
        return;
      if (points.get(0).is(points.get(s))) {
        points.remove(s);
      }
    }

    public void validate() {
      int s = size();
      if (s == 0)
        return;
      if (s < 4) {
        badState("bad clipped polygon:", INDENT, this);
      }
      for (int i = 0; i < s - 1; i++) {
        if (points.get(i).is(points.get(i + 1))) {
          badState("duplicated vertex at", i, ",", i + 1, ":", INDENT, this);
        }
      }
    }

    public boolean isEmpty() {
      return size() == 0;
    }

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

  private Poly extractPoly(Pt cell, int polyColor) {

    final boolean db = false;

    var poly = new Poly();
    poly.filter = false;
    if (db)
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
      if (db) {
        printGrid("extract loop, prevVert:" + firstVert, cell);
        pr("added point:", es, "now:", poly);
      }

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
        if (db)
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
    printGrid("extracted polygon", cell);
    var result = mergeEdges(poly);
    pr("merged edges:", INDENT, result);
    return result;
  }

  private Poly mergeEdges(Poly poly) {
    pr("merging edges:", poly);

    var result = new Poly();
    var prevPt2 = poly.getMod(0);
    var prevPt1 = poly.getMod(1);
    int dir = determineDir(prevPt2, prevPt1);
    int i = 2;
    Pt firstAdded = null;
    while (true) {
      var pt = poly.getMod(i);
      i++;

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

  private int inf;

  private List<Poly> splitAtConcaveVert(Poly poly) {
    final boolean db = false;

    List<Poly> result = new ArrayList<>();
    Pt splitVert = null;

    if (db)
      pr("poly:", poly);
    var prev2 = poly.getMod(-2);
    var prev1 = poly.getMod(-1);
    var prevDir = determineDir(prev2, prev1);
    for (var pt : poly.vertices()) {
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
      var res = new ArrayList<Poly>();
      splitPoly(poly, splitVert.y, res, false);
      splitPoly(poly, splitVert.x, res, true);

      pr("split poly at vert:", splitVert, INDENT, poly, CR, "result:", INDENT, res);

      for (var subpoly : res) {
        result.addAll(splitAtConcaveVert(subpoly));
      }
    }

    pr("splitAtConcaveVert:", INDENT, poly, CR, result);

    return result;
  }

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

  private static void splitPoly(Poly poly, int splitCoord, List<Poly> result, boolean verticalLine) {

    final boolean db = false;
    if (db) {
      pr(VERT_SP, "split poly:", poly);
      pr("at ", verticalLine ? "vertical " : "horizontal ", "line", splitCoord);
    }

    Poly left = new Poly();
    Poly right = new Poly();

    // Start at a vertex on an edge that is strictly to one side
    int currentSide = NONE;

    int startVertex;
    for (startVertex = 0; startVertex < poly.size(); startVertex++) {
      var pt = poly.getMod(startVertex);
      var pt2 = poly.getMod(startVertex + 1);

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
    if (db)
      pr("starting current side:", Side(currentSide));

    for (int q = 0; q <= poly.size(); q++) {
      int vi = (q + startVertex);
      var polyPt = poly.getMod(vi);

      if (db)
        pr(VERT_SP, "vi:", vi, "of length", poly.size(), "polyPt:", polyPt);

      int newSide = signum(verticalLine ? polyPt.x - splitCoord : polyPt.y - splitCoord);
      Pt crossPt = null;
      if (newSide != currentSide) {
        if (verticalLine) {
          crossPt = new Pt(splitCoord, polyPt.y);
          if (db)
            pr("vert line, cross point at split coord:", splitCoord);
        } else {
          crossPt = new Pt(polyPt.x, splitCoord);
          if (db)
            pr("horz line, cross point at split coord:", splitCoord);
        }
      }
      if (db) {
        pr("...left :", left);
        pr("...right:", right, VERT_SP);
        pr("...vi:", vi, "pt:", polyPt, "current side:", Side(currentSide), "new side:", Side(newSide),
            "cross:", crossPt);
      }

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
      if (db) {
        pr("after processing vert:");
        pr("...left :", left);
        pr("...right:", right, VERT_SP);
      }

      currentSide = newSide;
    }
    if (db) {
      pr("after clipping:");
      pr("...left :", left);
      pr("...right:", right, VERT_SP);
    }

    left.removeDupLastPt();
    right.removeDupLastPt();

    if (db)
      pr("validating left:", left);
    left.validate();
    if (db)
      pr("validating right:", right);
    right.validate();

    if (db)
      pr("**** just split poly:", poly);

    if (!left.isEmpty())
      result.add(left);
    if (!right.isEmpty())
      result.add(right);
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
}
