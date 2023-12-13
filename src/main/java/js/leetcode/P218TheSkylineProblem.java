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
    x("[[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]] ",
        "  [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]");
    x("[[0,2,3],[2,5,3]]", "[[0,3],[5,0]]");
    x("[[0,2147483647,2147483647]]", "[[0,2147483647],[2147483647,0]]");
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

  private void show(Set<Edge> edges, Object... messages) {
    if (db) {
      var sb = new StringBuilder();
      sb.append(BasePrinter.toString(messages));
      Edge prevEdge = null;
      for (var edge : edges) {
        if (edge.prev != prevEdge)
          sb.append("*** link to prev is bad!");
        if (edge.prev != null && edge.prev.next != edge)
          sb.append("*** prev edge fwd link bad!");
        if (edge.prev != null && edge.prev.x1 != edge.x0)
          sb.append("*** prev edge doesn't meet current!");
        sb.append("...");
        sb.append(edge);
        sb.append(' ');
        prevEdge = edge;
      }
      if (prevEdge != null && prevEdge.next != null)
        sb.append("*** last edge has non null fwd link!");
      var s = sb.toString();
      pr(s);
      checkState(!s.contains("*** "));
    }
  }

  // I shift the edge x coordinates left by this amount to avoid
  // special code for dealing with edges that are at the limits of
  // the legal range (Integer.MAX_VALUE).
  private static final int EDGE_OFFSET = -1;

  private void addWork(List<Edge> target, Edge edge) {
    pr("...adding work edge:", edge);
    for (var e : target) {
      checkState(e.x1 <= edge.x0, "attempt to add bad edge to work:", edge);
    }
    target.add(edge);
  }

  public List<List<Integer>> getSkyline(int[][] buildings) {
    List<Edge> edges = new ArrayList<>();
    for (var b : buildings)
      edges.add(new Edge(b[0] + EDGE_OFFSET, b[1] + EDGE_OFFSET, b[2]));
    edges.sort(EDGE_SORT_BY_HEIGHT);
    db("edges sorted by height:", edges);

    SortedSet<Edge> activeEdges = new TreeSet<Edge>(EDGE_SORT_BY_LEFT);
    // Add a 'ground' edge
    activeEdges.add(new Edge(EDGE_OFFSET - 1, Integer.MAX_VALUE, 0));

    // A work array for storing new edges
    var edgeWork = new ArrayList<Edge>();

    // Outer loop: iterates over each new edge to be inserted into the active edges set
    //
    for (var newEdge : edges) {
      db(VERT_SP, "inserting:", newEdge);
      show(activeEdges, "prior to insert");

      // Iterate forward through edges that are affected by inserting the new one.
      var oldEdge = activeEdges.headSet(newEdge).last();

      // Since the active edges are nonoverlapping, no edge to the left of oldEdge
      // can be affected by the insertion.

      db("...oldEdge:", oldEdge);

      // These will be the edges immediately adjacent to the ones that were deleted by this insertion operation
      Edge joinToLeft = oldEdge.prev;
      Edge joinToRight = null;

      edgeWork.clear();
      boolean addedNew = false;

      // Inner loop: iterate over edges affected by the insertion of the new edge
      //
      while (oldEdge != null) {

        // If this edge is collinear with the new edge, and intersecting it, merge them.
        // Due to the sort order, we know that this old edge will *not* extend past the right of the insert edge.
        if (oldEdge.y == newEdge.y && oldEdge.x1 <= newEdge.x0) {
          db("********* merging collinear active edge:", oldEdge, newEdge);
          newEdge.x0 = Math.min(newEdge.x0, oldEdge.x0);
          newEdge.x1 = Math.max(newEdge.x1, oldEdge.x1);
          activeEdges.remove(oldEdge);
          db("moving oldEdge from:", oldEdge, "to next:", oldEdge.next);
          oldEdge = oldEdge.next;
          continue;
        }

        // If edge overlaps new to left, add modified version 
        if (oldEdge.x0 < newEdge.x0) {
          var modified = new Edge(oldEdge.x0, newEdge.x0, oldEdge.y);
          addWork(edgeWork, modified);
          activeEdges.remove(oldEdge);
        }
        // If edge overlaps new to right, or is strictly right, add new edge, and
        // (if necessary) a modified version
        if (oldEdge.x1 > newEdge.x1) {
          // If we haven't yet added the new edge to the insertion list, do so
          if (!addedNew) {
            addWork(edgeWork, newEdge);
            addedNew = true;
          }
          // If there is an overlap, add new "remainder" edge
          if (oldEdge.x0 < newEdge.x1) {
            activeEdges.remove(oldEdge);
            addWork(edgeWork, new Edge(newEdge.x1, oldEdge.x1, oldEdge.y));
          } else {
            joinToRight = oldEdge;
            // We've moved into the region where the active edges are no longer affected
            db("set joinToRight to:", joinToRight);
            break;
          }
        }

        db("Moving to next old edge:", oldEdge.next);
        oldEdge = oldEdge.next;
      }

      pr("edges in edgeWork:", edgeWork);
      for (var edge : edgeWork) {
        join(joinToLeft, edge);
        joinToLeft = edge;
        activeEdges.add(edge);
      }
      // join the last edge added to the joinToRight
      join(edgeWork.get(edgeWork.size() - 1), joinToRight);
      show(activeEdges, "after splicing in modified edges (in edgeWork):");

      show(activeEdges, "...after insert");
    }

    var res = new ArrayList<List<Integer>>();

    var edge = activeEdges.first();
    while (edge != null) {
      if (edge.x0 >= EDGE_OFFSET) {
        res.add(ptAsList(edge.x0 - EDGE_OFFSET, edge.y));
      }
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
      String x1str;
      if (x1 == Integer.MAX_VALUE)
        x1str = "XX";
      else
        x1str = "" + x1;
      return y + "{" + x0 + ".." + x1str + "}";
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

}
