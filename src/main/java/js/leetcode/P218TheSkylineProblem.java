package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import js.base.BasePrinter;

public class P218TheSkylineProblem extends LeetCode {

  public static void main(String[] args) {
    new P218TheSkylineProblem().run();
  }

  public void run() {
    x("[[0,2,3],[2,5,3]]", "[[0,3],[5,0]]");

    x("[[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]] ",
        "  [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]");
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

  private static void show(Collection<Edge> edges, Object... messages) {
    if (db) {
      pr(BasePrinter.toString(messages));
      Edge prevEdge = null;
      for (var edge : edges) {
        if (edge.prev != prevEdge)
          pr("*** link to prev is bad!");
        if (edge.prev != null && edge.prev.next != edge)
          pr("*** prev edge fwd link bad!");
        if (edge.prev != null && edge.prev.x1 != edge.x0)
          pr("*** prev edge doesn't meet current!");
        pr("...", edge);
        prevEdge = edge;
      }
      if (prevEdge != null && prevEdge.next != null)
        pr("*** last edge has non null fwd link!");
    }
  }

  private static class Edge {
    int x0, x1;
    int y;
    Edge prev, next;

    public Edge(int left, int right, int height) {
      x0 = left;
      x1 = right;
      y = height;
    }

    @Override
    public String toString() {
      return y + ":" + x0 + ".." + (x1 == Integer.MAX_VALUE ? "XX" : "" + x1);
    }

  }

  private void join(Edge a, Edge b) {
    if (a != null)
      a.next = b;
    if (b != null)
      b.prev = a;
  }

  private static Comparator<Edge> EDGE_SORT_BY_LEFT = new Comparator<Edge>() {
    public int compare(Edge o1, Edge o2) {
      int diff = Integer.compare(o1.x0, o2.x0);
      if (diff == 0)
        diff = Integer.compare(o1.x1, o2.x1);
      if (diff == 0)
        diff = Integer.compare(o1.y, o2.y);
      return diff;
    }
  };
  private static Comparator<Edge> EDGE_SORT_BY_HEIGHT = new Comparator<Edge>() {
    public int compare(Edge o1, Edge o2) {
      int diff = Integer.compare(o1.y, o2.y);
      if (diff == 0)
        Integer.compare(o1.x0, o2.x0);
      if (diff == 0)
        diff = Integer.compare(o1.x1, o2.x1);
      return diff;
    }
  };

  public List<List<Integer>> getSkyline(int[][] buildings) {
    List<Edge> edges = new ArrayList<>();
    for (var b : buildings)
      edges.add(new Edge(b[0], b[1], b[2]));
    edges.sort(EDGE_SORT_BY_HEIGHT);

    db("edges sorted by height:", INDENT, edges);

    SortedSet<Edge> activeEdges = new TreeSet<Edge>(EDGE_SORT_BY_LEFT);
    // Add a 'ground' edge
    activeEdges.add(new Edge(-1, Integer.MAX_VALUE, 0));

    for (var insertEdge : edges) {
      db(VERT_SP, "inserting:", insertEdge);
      show(activeEdges, "prior to insert");
      Edge activeEdge = null;
      var head = activeEdges.headSet(insertEdge);
      if (!head.isEmpty())
        activeEdge = head.last();
      else {
        var tail = activeEdges.tailSet(insertEdge);
        if (!tail.isEmpty())
          activeEdge = tail.first();
      }
      db("...activeEdge:", activeEdge);

      Edge joinToLeft = null;
      Edge joinToRight = null;

      if (activeEdge != null) {
        // Move backward, if possible, to rightmost edge strictly to left of this one

        while (true) {
          checkInf();
          if (activeEdge.x1 < insertEdge.x0) {
            joinToLeft = activeEdge;
            break;
          }
          if (activeEdge.prev == null)
            break;
          activeEdge = activeEdge.prev;
        }
        db("...moved backward to strictly left, activeEdge:", activeEdge);

        while (true) {
          db("...merge loop, insert:", insertEdge, "active:", activeEdge);
          checkState(activeEdge != null);

          if (activeEdge == null || activeEdge.x0 >= insertEdge.x1) {
            db("...existing is null or strictly to right");
            joinToRight = activeEdge;
            db(".......set join to right:", joinToRight);
            break;
          }

          if (activeEdge.x1 <= insertEdge.x0) {
            db("...existing is strictly to left");
            joinToLeft = activeEdge;
            db(".......set join to left:", joinToLeft);
          } else {
            int splitLeft = insertEdge.x0 - activeEdge.x0;
            int splitRight = activeEdge.x1 - insertEdge.x1;
            db("...split to left :", splitLeft);
            db("...split to right:", splitRight);
            if (splitLeft <= 0 && splitRight <= 0) {
              db("......new edge subsumes active edge completely");
              activeEdges.remove(activeEdge);
            } else {
              var oldActiveX1 = activeEdge.x1;
              if (splitLeft > 0) {
                db("......active edge overlaps and extends to left of new");
                activeEdge.x1 = insertEdge.x0;
                joinToLeft = activeEdge;
                db(".......set join to left:", joinToLeft);
              }
              if (splitRight > 0) {
                db("......active edge overlaps and extends to right of new");
                if (splitLeft <= 0) {
                  db("......active edge does not extend to left of new");
                  activeEdge.x0 = insertEdge.x1;
                  joinToRight = activeEdge;
                  db(".......set join to right:", joinToRight);
                } else {
                  db("......active edge extends to both left and right of new");
                  // The active edge is split on both left and right, so insert a new one for the right
                  var activeRight = new Edge(insertEdge.x1, oldActiveX1, activeEdge.y);
                  joinToRight = activeRight;
                  db(".......set join to right:", joinToRight);
                  join(activeRight, activeEdge.next);
                  activeEdges.add(activeRight);
                }
                break;
              }
            }
          }
          activeEdge = activeEdge.next;
        }

        pr("...joining left...insert:", joinToLeft, insertEdge);
        join(joinToLeft, insertEdge);
        pr("...joining insert...right:", insertEdge, joinToRight);
        join(insertEdge, joinToRight);
      }
      activeEdges.add(insertEdge);
    }

    var res = new ArrayList<List<Integer>>();

    var edge = activeEdges.first();
    while (edge != null) {
      if (edge.x0 < 0)
        continue;
      res.add(ptAsList(edge.x0, edge.y));
      edge = edge.next;
    }
    return res;
  }

  private static ArrayList<Integer> ptAsList(int x, int y) {
    var result = new ArrayList<Integer>(2);
    result.add(x);
    result.add(y);
    return result;
  }
}
