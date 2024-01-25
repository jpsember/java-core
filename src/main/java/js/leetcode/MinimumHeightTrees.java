package js.leetcode;

import static js.base.Tools.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import js.data.DataUtil;
import js.file.Files;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {

    x(5, "0 4 4 2 4 1 1 3", "1 4");

    x(4, "[[1,0],[1,2],[1,3]]", "1");

    x(3, "[[0,1],[0,2]]", "0");

    x(1, "[]", "0");
    x(2, "[[0,1]]", "0 1");

    if (false)
      x(1212, Files.readString(new File("1212.txt")));
  }

  private void x(int n, String ns) {
    x(n, ns, null);
  }

  private void x(int n, String ns, String exp) {
    var nums = extractNums(ns);
    var edges = new int[nums.length / 2][2];
    List<Integer> expected = null;
    if (exp != null) {
      expected = toList(extractNums(exp));
    } else {
      Alg alg;

      alg = new FloydWarshall();
      db = false;
      expected = alg.findMinHeightTrees(n, edges);
    }

    for (int i = 0; i < nums.length; i += 2) {
      int j = i / 2;
      edges[j][0] = nums[i];
      edges[j][1] = nums[i + 1];
    }
    Alg alg;
    alg = new Tree();

    db = nums.length < 20;
    checkpoint("Starting alg");
    var result = alg.findMinHeightTrees(n, edges);
    checkpoint("Done alg");
    {
      result.sort(null);
      expected.sort(null);
      verify(result, expected);
      pr("result:", result);
    }
    pr(result);
  }

  private interface Alg {

    List<Integer> findMinHeightTrees(int n, int[][] edges);
  }

  class FloydWarshall implements Alg {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      // Perform Floyd-Warshall algorithm.

      // Construct edge incidence matrix, and path matrix.  
      var dist = new int[n][n]; // Holds the shortest known number of edges between i and j
      var path = new int[n][n]; // Holds intermediate destination on path from i to j (j if single step)

      for (var row : dist)
        Arrays.fill(row, Integer.MAX_VALUE);
      for (int i = 0; i < n; i++)
        dist[i][i] = 0;

      if (db) {
        for (var row : path)
          Arrays.fill(row, Integer.MIN_VALUE);
      }

      for (var edge : edges) {
        var a = edge[0];
        var b = edge[1];
        dist[a][b] = 1;
        dist[b][a] = 1;
        path[a][b] = b;
        path[b][a] = a;
      }
      if (db)
        db("adjmat:", INDENT, strTable(dist));

      pr("start floyd");
      {
        for (int k = 0; k < n; k++) {
          for (int i = 0; i < n; i++) {
            var distIK = dist[i][k];
            if (distIK == Integer.MAX_VALUE)
              continue;
            for (int j = 0; j < n; j++) {
              var distKJ = dist[k][j];
              if (distKJ == Integer.MAX_VALUE)
                continue;
              var distIJ = dist[i][j];
              var newdist = distIK + distKJ;
              if (newdist < distIJ) {
                dist[i][j] = newdist;
                path[i][j] = k;
              }
            }
          }
        }
      }
      pr("end floyd");
      if (db)
        db("adjmat:", INDENT, strTable(dist));

      // Set k = graph radius (the maximum distance between any two vertices), and save all pairs
      // of vertices that are separated by this distance

      List<Integer> diam = new ArrayList<>();

      var k = 0;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          var k2 = dist[i][j];
          if (k2 > k) {
            diam.clear();
            k = k2;
          }
          if (k == k2) {
            diam.add(i);
            diam.add(j);
          }
        }
      }
      if (db)
        db("diameter, k:", k);

      // For each pair of diameter vertices (u,v), the node(s) halfway along the path between these two are 
      // added to the answer list.

      var result = new HashSet<Integer>();
      if (k == 0)
        result.add(0);

      int ki0 = k / 2;
      int ki1 = ki0 + (k & 1);

      // pr("k:", k, "ki0:", ki0, "ki1:", ki1);

      for (int j = 0; j < diam.size(); j += 2) {
        pr("diam", j, "of", diam.size());
        var a = diam.get(j);
        var b = diam.get(j + 1);

        if (db)
          db("extract path from", a, "to", b);

        var posn = a;
        int cursor = 1; // already at start
        if (ki0 == 0) {
          result.add(posn);
        }

        while (posn != b) {

          // Advance posn to next *single step* towards b
          int dest = b;
          while (true) {
            // Get next step along path
            var firstStepToDest = path[posn][dest];
            if (dest == firstStepToDest) // If this is a single step, stop
              break;
            dest = firstStepToDest;
          }
          posn = dest;

          // If this position is halfway, add to result
          if (cursor >= ki0)
            result.add(posn);
          // If remaining steps are past halfway, stop
          if (cursor == ki1)
            break;

          cursor++;
        }
      }

      return new ArrayList<Integer>(result);
    }

  }

  class Tree implements Alg {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      // Construct graph
      var nodes = new Node[n];
      for (int i = 0; i < n; i++)
        nodes[i] = new Node(i);

      for (var edge : edges) {
        var na = nodes[edge[0]];
        var nb = nodes[edge[1]];
        na.edges.add(new Edge(nb));
        nb.edges.add(new Edge(na));
      }

      // Choose an arbitrary node as the root of a tree
      var root = nodes[0];

      // Determine depth and height of each node relative to this tree.
      walk2(root, null);

      walk3(root, 0);

      for (var nd : nodes) {
        pr(nd);
      }

      List<Integer> result = new ArrayList<>();

      //      // All nodes that have a height and depth "equal" to half the root's height are added to the
      //      // output.  We have to compensate for the root's height being an odd number...
      //      int v1 = root.height / 2;
      //      int v2 = (root.height % 2 == 1) ? v1 + 1 : v1;
      //
      //      if (db)
      //        db("root:", root);
      //       for (var node : nodes) {
      //        pushIndent();
      //        if (db)
      //          db("child:", node);
      //        if ((node.depth == v1 && node.height == v2) || (node.depth == v2 && node.height == v1)) {
      //          if (db)
      //            db("...adding to result");
      //          result.add(node.val);
      //        }
      //        popIndent();
      //      }
      return result;
    }

    /**
     * Walk a subtree, deleting edges that lead to parents, and calculating
     * height of each node
     */
    private void walk2(Node node, Node parent) {
      pr("walk2, node:",node.name);

      // Delete any edges that go back to the parent
      for (int i = node.edges.size() - 1; i >= 0; i--) {
        var e = node.edges.get(i);
        if (e.dest == parent) {
          pr("...removing edge to parent");
          node.edges.remove(i);
        }
      }

      int longestPath = 0;
      for (var e : node.edges) {
        pr("walk2, edge:", e.dest.name);
        walk2(e.dest, node);
        var childLongestPath = e.dest.height;
        longestPath = Math.max(longestPath, 1+childLongestPath);
      }

      node.height = longestPath;
      pr("...setting height:", node.height, "for:", node);

      //      for (var sibling : node.siblings) {
      //
      //        walk(sibling, depth + 1);
      //        node.height = Math.max(node.height, 1 + sibling.height);
      //      }
    }

    /**
     * Walk subtree, calculating longest path through parent node
     */
    private void walk3(Node node, int longestPathThroughParent) {
      // node.depth = depth;

      int longHt1 = 0;
      int longHt2 = 0;
      Node longNode1 = null;

      for (int i = 0; i < node.edges.size(); i++) {
        var e = node.edges.get(i);
        var n = e.dest;
        if (n.height > longHt1) {
          longHt2 = longHt1;
          longNode1 = n;
          longHt1 = n.height;
        } else if (n.height > longHt2) {
          longHt2 = n.height;
        }
      }

      for (var e : node.edges) {
        var n = e.dest;

        // The length of the longest path through this child's parent is the greater of
        // the path through its parent, or 2 + the longest height of some other child
        int longPath = 1 + longestPathThroughParent;
        if (longHt1 > longPath) {
          if (longNode1 != n) {
            int c = 2 + longHt1;
            if (c > longPath)
              longPath = c;
          } else {
            if (longHt2 > longPath) {
              int c = 2 + longHt2;
              if (c > longPath)
                longPath = c;
            }
          }
        }
        n.longestPathThroughParent = longPath;
      }

    }

    private class Edge {
      Node dest;
      // int longestPath;

      Edge(Node dest) {
        this.dest = dest;
      }
    }

    private class Node {
      int name;
      List<Edge> edges = new ArrayList<>();
      // Length of longest path to leaf node
      int height;
      // Length of longest path through parent node
      int longestPathThroughParent;

      Node(int val) {
        this.name = val;
      }

      private String d(int value) {
        var s = "" + value;
        return spaces(3 - s.length()) + s + " ";
      }

      @Override
      public String toString() {
        var s = sb();
        s.append("#").append(name);
        s.append("(h:").append(d(height));
        s.append(" p:").append(d(longestPathThroughParent));
        s.append(")->( ");
        for (var edge : edges) {
          var n = edge.dest;
          s.append(n.name).append(' ');
        }
        s.append(") ");
        return s.toString();
      }

    }
  }

}
