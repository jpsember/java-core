
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * I need to figure out how to offset the polygon boundary from the center of the pixel.
 *
 */
public class P85MaximalRectangle {

  public static void main(String[] args) {
    new P85MaximalRectangle().run();
  }

  private void run() {

    x(3,2,"111010",3);
    halt();
    
    x(3,2,"110111",4);
    halt();
    
    x(3,3,"111111111",9);
    halt();
    
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

    final boolean db = false;

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
    var start = new Pt(1, 1);
    int cellInd = start.cellIndex();
    int scanStop = bWidth * (bHeight - 1);

    while (cellInd < scanStop) {
      var cLeft = cells[cellInd - 1];
      var cRight = cells[cellInd];
      if (cLeft == 0 && cRight == 1) {
        // This is a North edge
        polygons.add(extractPoly(cellInd, NORTH, polyColor));
        polyColor++;
        printGrid("after poly extract");
      } else if (cLeft == 1 && cRight == 0) {
        if (db)
          printGrid("found south edge", PtWithIndex(cellInd));
        // This is a South edge
        polygons.add(extractPoly(cellInd - 1, SOUTH, polyColor));
        polyColor++;
        printGrid("after poly extract");
      }
      cellInd++;
    }

    var newPolygons = splitAtConcaveVertices(polygons);

    pr("orig poly count :", polygons.size());
    pr("split poly count:", newPolygons.size());

    int maxArea = 0;
    for (var p : newPolygons) {
      pr(VERT_SP,"poly:",p);
      
      var f = p.get(0);
      int xmin = f.x;
      int xmax = xmin;
      int ymin = f.y
          ;int ymax = ymin;
      for (var pt:p) {
        xmin = Math.min(xmin,pt.x);
        xmax= Math.max(xmax,pt.x);
        ymin = Math.min(ymin,pt.y);
        ymax= Math.max(ymax,pt.y);
      }
      var area = (xmax-xmin )*(ymax-ymin );
      pr("area:",area);
      maxArea = Math.max(maxArea,area);
    }

    // Divide area by the area of the scaling factor (squared)
    return maxArea / 4;
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

    public Pt offset(int dir) {
      return new Pt(x + xoff[dir], y + yoff[dir]);
    }
    
    Pt move(int dir) {
      return new Pt(x + xmoves[dir], y + ymoves[dir]);
    }

    private static int[] xmoves = { 0, 1, 0, -1 };
    private static int[] ymoves = { -1, 0, 1, 0 };
    private static int[] xoff = { -1,-1,1,1 };
    private static int[] yoff = { 1,-1,-1,1};

    int lookForward(int dir) {
      return sCells[(y + ymoves[dir]) * bWidth + (x + xmoves[dir])];
    }

    int lookForwardLeft(int dir) {

      return sCells[(y + ymoves[dir] + ymoves[turnLeft(dir)]) * bWidth
          + (x + xmoves[dir] + xmoves[turnLeft(dir)])];
    }

    public boolean is(Pt p) {
      return x == p.x && y == p.y;
    }

    private void paint(int value) {
      sCells[cellIndex()] = (byte) value;
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

    final boolean db = false;

    List<Pt> poly = new ArrayList<>();

    var c = PtWithIndex(cellIndex);
    var cStart = c;

    pr("extractPoly, cellIndex:", cellIndex, "pt:", c, "color:", polyColor);

    var z = 1000;
    while (true) {
      if (db)
        checkState(z-- > 0);

      // Paint this cell with the poly number 
      c.paint(polyColor);
      if (db) {
        pr("...look forward, dir", dir, ":", c.lookForward(dir));
        printGrid("about to look forward", c);
      }

      if (c.lookForward(dir) == 0) {
        dir = turnRight(dir);
        if (db) {
          pr("...turned right");
          printGrid("after turned right", c);
        }
      } else if (c.lookForwardLeft(dir) != 0) {
        if (db) {
          pr("lookForwardLeft was:", c.lookForwardLeft(dir));
          printGrid("after look fwd", c);
        }
        // We need to move forward before turning left.

        poly.add(c.offset(dir));
        c = c.move(dir);

        dir = turnLeft(dir);
        if (db)
          pr("...turned left");
      } else {
        
        
        poly.add(c.offset(dir));
        if (db)
          pr("...added:", c);
        c = c.move(dir);
        if (db) {
          pr("...moved to:", c);
          printGrid("after move", c);
        }
        if (c.is(cStart))
          break;
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
    //    pr("initial dir,", prevPt2, ">>", prevPt1, ":", dir);
    int i = 2;
    Pt firstAdded = null;
    while (true) {
      var pt = poly.get(i);
      i = (i + 1) % poly.size();

      int newDir = determineDir(prevPt1, pt);
      //      pr("new pt, dir:", prevPt1, ">>", pt, ":", newDir);

      if (newDir != dir) {
        //        pr("dir changed from", dir, "to", newDir, "; firstAdded:", firstAdded, "prevPt:", prevPt1);
        dir = newDir;
        if (firstAdded == null) {
          firstAdded = prevPt1;
        } else if (prevPt1 == firstAdded)
          break;
        //        checkState(result.size() < poly.size());
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

  private List<List<Pt>> splitAtConcaveVertices(List<List<Pt>> polygons) {

    // Determine where polygons must be split
    Set<Integer> horzSplits = new HashSet<>();
    Set<Integer> vertSplits = new HashSet<>();

    for (var poly : polygons) {
      pr("poly:", poly);
      var prev2 = peekLast(poly, 1);
      var prev1 = peekLast(poly, 0);
      pr("prev1:", prev1);
      var prevDir = determineDir(prev2, prev1);
      for (var pt : poly) {
        var newDir = determineDir(prev1, pt);
        if (((newDir - prevDir) & 3) == 3) {
          horzSplits.add(pt.y);
          vertSplits.add(pt.x);
        }
        prevDir = newDir;
        prev1 = pt;
      }
    }

    if (!horzSplits.isEmpty()) {
    var result = new ArrayList<List<Pt>>();
    for (var y : horzSplits) {
      for (var poly : polygons) {
        splitPoly(poly, y, result, false);
      }
    }
    
    polygons = result;
    }
    
    if (!vertSplits.isEmpty()) {
      
      var  result = new ArrayList<List<Pt>>();
    for (var x : vertSplits) {
      for (var poly : polygons) {
        splitPoly(poly, x, result, true);
      }
    }
    polygons = result;
    }
    
    return polygons;
  }

  //  private static void splitPolyHorz(List<Pt> poly, int y, List<List<Pt>> result) {
  //
  //    List<Pt> left = new ArrayList<>(poly.size());
  //    List<Pt> right = new ArrayList<>(poly.size());
  //
  //    Pt prevPt = null;
  //
  //    int prevNonZeroSign = 0;
  //
  //    for (var pt : poly) {
  //      int newSign = pt.y - y;
  //      if (newSign != 0) {
  //        if (prevPt != null && prevNonZeroSign != newSign) {
  //          var newPt = new Pt(pt.x, y);
  //          left.add(newPt);
  //          right.add(newPt);
  //        }
  //        prevNonZeroSign = newSign;
  //      }
  //      prevPt = pt;
  //      if (newSign > 0) {
  //        right.add(pt);
  //      } else if (newSign < 0) {
  //        left.add(pt);
  //      }
  //    }
  //
  //    //    pr("split poly horz at y:", y);
  //    //    pr("orig:", poly);
  //    //    pr("left:", left);
  //    //    pr("right:", right);
  //    //    halt();
  //
  //  }

  private static void splitPoly(List<Pt> poly, int splitCoord, List<List<Pt>> result, boolean vert) {

    pr("split poly:", poly);
    pr(" at coord:", splitCoord, "vert:", vert);

    List<Pt> left = new ArrayList<>(poly.size());
    List<Pt> right = new ArrayList<>(poly.size());

    // Start at a vertex strictly to one side
    int startVertex = 0;
    int prevNonZeroSign = 0;
    for (var pt : poly) {
      if (vert)
        prevNonZeroSign = pt.x - splitCoord;
      else
        prevNonZeroSign = pt.y - splitCoord;
      if (prevNonZeroSign != 0)
        break;
      startVertex++;
    }
    checkState(prevNonZeroSign != 0);

    boolean leftValid = false;
    boolean rightValid = false;
    Pt prevPt = null;
    for (int i = 0; i <= poly.size(); i++) {
      var pt = poly.get((i + startVertex) % poly.size());

      int newSign;
      if (vert)
        newSign = pt.x - splitCoord;
      else
        newSign = pt.y - splitCoord;
      if (newSign != 0) {
        if (prevPt != null && prevNonZeroSign != newSign) {
          Pt newPt;
          if (vert)
            newPt = new Pt(splitCoord, pt.y);
          else
            newPt = new Pt(pt.x, splitCoord);
          left.add(newPt);
          right.add(newPt);
        }
        prevNonZeroSign = newSign;
      }
      prevPt = pt;
      if (newSign >= 0) {
        pr("...newSign",newSign,"adding point to right:",pt);
        right.add(pt);
        if (newSign > 0)
          rightValid = true;
      }  
      if (newSign <= 0) {
        pr("...newSign",newSign,"adding point to left:",pt);
         left.add(pt);
        if (newSign < 0)
          leftValid = true;
      }
    }
pr("valid, left:",leftValid, "right:",rightValid);

    if (!leftValid)
      left.clear();
    if (!rightValid)
      right.clear();
    removeDupLastPt(left);
    removeDupLastPt(right);

    pr("validating left:", left);
    validate(left);
    pr("validating right:", right);
    validate(right);

    if (!left.isEmpty())
      result.add(left);
    if (!right.isEmpty())
      result.add(right);

    //    pr("split poly horz at y:", y);
    //    pr("orig:", poly);
    //    pr("left:", left);
    //    pr("right:", right);
    //    halt();

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
    if (list.get(0).is(list.get(s)))
      list.remove(s);
  }

  private static byte[] sCells;
  private static int bWidth;
  private static int bHeight;
}
