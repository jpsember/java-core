package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import js.base.BasePrinter;

public class P218TheSkylineProblem extends LeetCode {

  public static void main(String[] args) {
    new P218TheSkylineProblem().run();
  }

  public void run() {
    x("[[0,2147483647,2147483647]]", "[[0,2147483647],[2147483647,0]]");

    x("[[1,2,1],[1,2,2],[1,2,3],[2,3,1],[2,3,2],[2,3,3]]", "[[1,3],[3,0]]");

    x("3 5 7 4 9 12", "3 7 4 12 9 0");
    x("[[1,2,1],[1,2,2],[1,2,3],[2,3,1],[2,3,2],[2,3,3]]", "[[1,3],[3,0]]");
    x("[[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]] ",
        "  [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]");
    x("[[0,2,3],[2,5,3]]", "[[0,3],[5,0]]");
  }

  private void x2(Object... unused) {
  }

  private void x(String a, String b) {
    var an = extractNums(a);
    int[][] bu = new int[an.length / 3][3];
    int i = 0;

    for (var row : bu) {
      row[0] = an[i++];
      row[1] = an[i++];
      row[2] = an[i++];
    }

    var result = getSkyline(bu);
    var ss = BasePrinter.toString(result);
    pr(extractNums(ss));
    var bn = extractPts(b);
    verify(extractPts(ss), bn);
  }

  private static Object extractPts(String s) {
    var n = extractNums(s);
    checkArgument(n.length % 2 == 0);
    var result = new ArrayList<List<Integer>>();
    for (int i = 0; i < n.length; i += 2)
      result.add(ptAsList(n[i], n[i + 1]));
    return result;
  }

  private static final boolean db = true;

  private static void db(Object... messages) {
    if (db) {
      pr(messages);
    }
  }

  private void show(Set<Point> points, Object... messages) {
    if (db) {
      var sb = new StringBuilder();
      sb.append(BasePrinter.toString(messages));
      Point prevPt = null;
      for (var pt : points) {
        if (prevPt != null) {
          sb.append("...");
          sb.append(new Edge(prevPt.x, pt.x, prevPt.y));
          sb.append(' ');
        }
        prevPt = pt;
      }
      sb.append(" >> ");
      sb.append(points);
      pr(sb);
    }
  }

  public List<List<Integer>> getSkyline(int[][] buildings) {
    List<Edge> edges = new ArrayList<>();
    for (var b : buildings)
      edges.add(new Edge(b[0] + PT_OFFSET, b[1] + PT_OFFSET, b[2] + PT_OFFSET));
    edges.sort(EDGE_SORT_BY_HEIGHT);
    db("edges sorted by height:", edges);

    // The active edges are maintained in a sorted order, by left edge
    SortedSet<Point> activePts = new TreeSet<Point>(POINT_SORT);

    // Add 'ground' points at y=0 that extend beyond the left and right of
    // any of the edges we'll be adding.
    activePts.add(new Point(PT_OFFSET - 1, PT_OFFSET));
    activePts.add(new Point(Integer.MAX_VALUE, PT_OFFSET));

    // Outer loop: iterates over each new edge to be inserted into the active points set
    //
    var w0 = new Point(0, Integer.MIN_VALUE);
    // todo("should we use offsetting in y as well?");
    var w1 = new Point(0, Integer.MAX_VALUE);

    for (var newEdge : edges) {
      db(VERT_SP, "inserting:", newEdge);
      show(activePts, "prior to insert");

      w0.x = newEdge.x0();
      w1.x = newEdge.x1();

      var scan = activePts.tailSet(w0);
      var leftPt = scan.first();
      pr("tail set from:", w0, "ends with:", leftPt);

      // We want to delete any points strictly between w0 and w1, but 
      // the second point we are inserting has a y-coordinate that is either
      // that of the last point in this range, or if the range is empty,
      // that of the last point preceding this range
      //
      Point rightPt = null;
      if (POINT_SORT.compare(leftPt, w1) <= 0) {
        var removePts = activePts.subSet(leftPt, w1);
        pr("removing points:", removePts);
        if (!removePts.isEmpty())
          rightPt = removePts.last();
        removePts.clear();
      }

      // If there were no points in the delete range, we must scan back to the last point
      // before the insertion region to determine the second new point's y coordinate
      if (rightPt == null)
        rightPt = activePts.headSet(w0).last();

      activePts.add(newEdge.pt);
      activePts.add(new Point(newEdge.x1(), rightPt.y));
      show(activePts, "after insertion");
    }

    todo("merge horizontal collinear");
    show(activePts, "extracting");
    var result = new ArrayList<List<Integer>>();
    {
      int prevPtY = Integer.MIN_VALUE;
      //Point prevPt = null;
      for (var pt : activePts) {
        if (pt.x < PT_OFFSET || pt.x == Integer.MAX_VALUE)
          continue;
        var x = pt.x - PT_OFFSET;
        var y = pt.y - PT_OFFSET;
        pr("...pt:", x, y);
        if (y != prevPtY)
          result.add(ptAsList(x, y));
        prevPtY = y;
      }
    }
    return result;
  }

  // I offset the coordinates by a negative number to avoid 
  // special code for dealing with points whose coordinates are exactly at Integer.MAX_VALUE
  private static final int PT_OFFSET = -1; // -100;

  private static ArrayList<Integer> ptAsList(int x, int y) {
    var result = new ArrayList<Integer>(2);
    result.add(x);
    result.add(y);
    return result;
  }

  private static class Edge {
    Point pt;
    int width;

    public Edge(int left, int right, int height) {
      pt = new Point(left, height);
      width = right - left;
      //      pr("built edge from left:", left, "right:", right, "height:", height, this);
    }

    @Override
    public String toString() {
      //      String x1str;
      //      int x1 = pt.x + width;
      //      if (x1 == Integer.MAX_VALUE)
      //        x1str = "XX";
      //      else
      //        x1str = "" + x1;
      return "(" + xs(x0()) + " " + xs(x1()) + ")y:" + pt.y;
    }

    public int x1() {
      return pt.x + width;
    }

    public int x0() {
      return pt.x;
    }

    public int y() {
      return pt.y;
    }
  }

  private static Comparator<Point> POINT_SORT = new Comparator<Point>() {
    public int compare(Point p1, Point p2) {
      int result = Integer.compare(p1.x, p2.x);
      if (result == 0)
        result = Integer.compare(p1.y, p2.y);
      return result;
    }
  };

  private static Comparator<Edge> EDGE_SORT_BY_HEIGHT = new Comparator<Edge>() {
    public int compare(Edge o1, Edge o2) {
      int diff = Integer.compare(o1.y(), o2.y());
      if (diff == 0)
        Integer.compare(o1.x0(), o2.x0());
      if (diff == 0)
        diff = Integer.compare(o1.x1(), o2.x1());
      return diff;
    }
  };

  private static class Point {
    int x;
    int y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      //      if (x >= Integer.MAX_VALUE - 10) {
      //        int diff = Integer.MAX_VALUE - x;
      //        if (diff == 0)
      //          return "XX";
      //        return "XX-" + diff;
      //      }
      return "(" + xs(x) + " " + xs(y) + ")";
    }

  }

  private static String xs(int x) {
    if (x >= Integer.MAX_VALUE - 10) {
      int diff = Integer.MAX_VALUE - x;
      if (diff == 0)
        return "XX";
      return "XX-" + diff;
    }
    return "" + x;
  }

}
