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

    var bn = extractNums(b);
    var result = getSkyline(bu);
    var ss = BasePrinter.toString(result);
    pr(extractNums(ss));
    verify(extractNums(ss), bn);
  }

  private static void show(Collection<Edge> edges, Object... messages) {
    pr(BasePrinter.toString(messages));
    Edge prevEdge = null;
    for (var edge : edges) {
      if (edge.prev != prevEdge) 
        pr("*** link to prev is bad!");
      if (edge.prev != null && edge.prev.next != edge)  
        pr("*** prev edge fwd link bad!");
      pr("...", edge);
     prevEdge = edge;
    }
    if (prevEdge != null && prevEdge.next != null)
      pr("*** last edge has non null fwd link!");
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
      return y + ":" + x0 + ".." + x1;
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
    SortedSet<Edge> activeEdges = new TreeSet<Edge>(EDGE_SORT_BY_LEFT);
    List<Edge> edges = new ArrayList<>();
    for (var b : buildings)
      edges.add(new Edge(b[0], b[1], b[2]));
    edges.sort(EDGE_SORT_BY_HEIGHT);

    pr("edges sorted by height:", INDENT, edges);

    for (var insertEdge : edges) {
      pr(VERT_SP, "inserting:", insertEdge);
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
      pr("...activeEdge:", activeEdge);
      
      if (activeEdge != null) {
        // Move backward, if possible, to rightmost edge strictly to left of this one

        while (true) {
          checkInf();
          if (activeEdge.x1 < insertEdge.x0)
            break;
          if (activeEdge.prev == null)
            break;
          activeEdge = activeEdge.prev;
        }
        pr("...moved backward to strictly left, activeEdge:", activeEdge);

        while (true) {
          pr("...merge loop, insert:",insertEdge,"active:",activeEdge);
          if (activeEdge == null)
            break;

          if (activeEdge.x1 < insertEdge.x0) {
            pr("...existing is strictly to left");
            join(activeEdge, insertEdge);
          } else if (activeEdge.x0 > insertEdge.x1) {
            pr("...existing is strictly to right");
            join(insertEdge, activeEdge);
            break;

          } else {

            int splitLeft = insertEdge.x0 - activeEdge.x0;
            int splitRight = activeEdge.x1 - insertEdge.x1;
            pr("...split to left :",splitLeft);
            pr("...split to right:",splitRight);
            if (splitLeft <= 0 && splitRight <= 0) {
              pr("......new edge subsumes active edge completely");
              activeEdges.remove(activeEdge);
            } else {

              if (splitLeft > 0) {
                pr("......active edge overlaps and extends to left of new");
                  activeEdge.x1 = insertEdge.x0;
                join(activeEdge, insertEdge);
              }
              if (splitRight > 0) {
                pr("......active edge overlaps and extends to right of new");
                 if (splitLeft <= 0) {
                   pr("......active edge does not extend to left of new");
                   activeEdge.x0 = insertEdge.x1;
                  join(insertEdge, activeEdge);

                } else {
                  pr("......active edge extends to both left and right of new");
                   // The active edge is split on both left and right, so insert a new one for the right
                  var activeRight = new Edge(insertEdge.x1, activeEdge.x1, activeEdge.y);
                  join(insertEdge, activeRight);
                  join(activeRight, activeEdge.next);
                  activeEdges.add(activeRight);
                }
              }
            }
          }
          activeEdge = activeEdge.next;
        }
      }
      activeEdges.add(insertEdge);
    }

    var res = new ArrayList<List<Integer>>();

    var edge = activeEdges.first();
    while (true) {
      var x = ptAsList(edge.x0, edge.y);
      res.add(x);
      if (edge.next == null) {
        res.add(ptAsList(edge.x1, 0));
        break;
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
}
