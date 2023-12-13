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
   // x("3 5 7 4 9 12", "3 7 4 12 9 0");
        x("[[1,2,1],[1,2,2],[1,2,3],[2,3,1],[2,3,2],[2,3,3]]", "[[1,3],[3,0]]");
    //    x("[[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]] ",
    //        "  [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]");
    //    x("[[0,2,3],[2,5,3]]", "[[0,3],[5,0]]");
    //    x("[[0,2147483647,2147483647]]", "[[0,2147483647],[2147483647,0]]");
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
      edges.add(new Edge(b[0] + PT_X_OFFSET, b[1] + PT_X_OFFSET, b[2]));
    edges.sort(EDGE_SORT_BY_HEIGHT);
    db("edges sorted by height:", edges);

    // The active edges are maintained in a sorted order, by left edge
    SortedSet<Point> activePts = new TreeSet<Point>(POINT_SORT);

    // Add 'ground' points at y=0 that extend beyond the left and right of
    // any of the edges we'll be adding.
    activePts.add(new Point(PT_X_OFFSET - 1, 0));
    activePts.add(new Point(Integer.MAX_VALUE, 0));

    // Outer loop: iterates over each new edge to be inserted into the active points set
    //
    var workPt = new Point(0, -1);
    todo("should we use offsetting in y as well?");
    var workPt2 = new Point(0, Integer.MAX_VALUE);

    for (var newEdge : edges) {
      db(VERT_SP, "inserting:", newEdge);
      show(activePts, "prior to insert");

      workPt.x = newEdge.pt.x;
      workPt2.x = newEdge.x1();

      //      
      //      var edgePt0 = newEdge.pt;
      //      var edgePt1 = workPt;
      //      workPt.x = newEdge.x1();
      //      workPt.y = edgePt0.y;

      var scan = activePts.tailSet(workPt);
      checkState(!scan.isEmpty());
      var leftPt = scan.first();
      pr("tail set from:", workPt, "ends with:", leftPt);

      //      if (
      //      scan = activePts.headSet(workPt2);
      //      var rightPt = scan.last();
      //      pr("tail set from:", workPt2, "ends with:", rightPt);

      var rightPt = leftPt;
      if (POINT_SORT.compare(leftPt, workPt2) <= 0) {
        var removePts = activePts.subSet(leftPt, workPt2);
        pr("removing points:", removePts);
        if (!removePts.isEmpty())
          rightPt = removePts.last();
        removePts.clear();
      }

      activePts.add(newEdge.pt);
      activePts.add(new Point(newEdge.x1(), rightPt.y));
      show(activePts, "after insertion");
    }

    todo("merge horizontal collinear");
    show(activePts, "extracting");
    var result = new ArrayList<List<Integer>>();
    for (var pt : activePts) {
      if (pt.x >= PT_X_OFFSET && pt.x < Integer.MAX_VALUE) {
        var disp = new Point(pt.x - PT_X_OFFSET, pt.y);
        pr("...adding point:", disp);
        result.add(ptAsList(pt.x - PT_X_OFFSET, pt.y));
      }
    }
    return result;
  }

  // I shift the active points' x coordinates left by this amount to avoid
  // special code for dealing with buildings whose right edges are at Integer.MAX_VALUE
  private static final int PT_X_OFFSET = 0; // -100;

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
