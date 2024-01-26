package js.leetcode;

import static js.base.Tools.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.file.Files;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {

    x(7, "[[0,1],[1,2],[1,3],[2,4],[3,5],[4,6]]", "[1,2]");

    x(2, "[[0,1]]", "0 1");

    x(1, "[]", "0");

    x(3, "[[0,1],[0,2]]", "0");

    x(5, "[[0,1],[0,2],[0,3],[3,4]]", "0 3");

    x(5, "0 4 4 2 4 1 1 3", "1 4");

    x(4, "[[1,0],[1,2],[1,3]]", "1");

    if (true)
      x(1212, Files.readString(new File("1212.txt")));

  }

  private void x(int n, String ns) {
    x(n, ns, null);
  }

  private void x(int n, String ns, String exp) {
    var nums = extractNums(ns);
    var edges = buildEdges(nums);

    List<Integer> expected = null;
    if (exp != null) {
      expected = toList(extractNums(exp));
    } else {
      Alg alg = new Slow();
      db = false;
      expected = alg.findMinHeightTrees(n, edges);
    }

    Alg alg = new Tree2();

    db = nums.length < 20;
    checkpoint("Starting alg");
    var result = alg.findMinHeightTrees(n, edges);
    pr("result:", result);
    checkpoint("Done alg");
    {
      result.sort(null);
      expected.sort(null);
      pr("expected after sort:", expected);
      verify(result, expected);
      pr("result:", result);
    }
    pr(result);
  }

  private int[][] buildEdges(int[] nums) {
    var edges = new int[nums.length / 2][2];

    for (int i = 0; i < nums.length; i += 2) {
      int j = i / 2;
      edges[j][0] = nums[i];
      edges[j][1] = nums[i + 1];
    }
    return edges;
  }

  private interface Alg {

    List<Integer> findMinHeightTrees(int n, int[][] edges);
  }

  class Slow implements Alg {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      // Construct graph
      var nodes = new Node[n];
      for (int i = 0; i < n; i++)
        nodes[i] = new Node(i);

      for (var edge : edges) {
        var na = nodes[edge[0]];
        var nb = nodes[edge[1]];
        na.children.add(nb);
        nb.children.add(na);
      }

      // build list of leaf nodes

      Set<Node> leafNodes = new HashSet<>();
      for (var node : nodes) {
        if (node.children.size() <= 1) {
          leafNodes.add(node);
          node.visited = true;
        }
      }

      while (true) {

        Set<Node> nextNodes = new HashSet<>();

        pushIndent();
        for (var node : leafNodes) {
          db(node);
          if (node.children.isEmpty())
            continue;

          var dest = node.children.get(0);
          if (dest.visited)
            continue;
          node.children.remove(0);
          dest.children.remove(node);
          db("...adding modified:", dest);
          nextNodes.add(dest);
        }
        popIndent();
        db("next nodes:", nextNodes);

        db("filterout nodes with deg > 1");
        pushIndent();
        // Filter out next nodes that have degree > 1, and mark
        // remaining as visited
        Set<Node> filtered = new HashSet<>();
        for (var node : nextNodes) {
          if (node.children.size() <= 1) {
            db("...retaining node:", node);
            node.visited = true;
            filtered.add(node);
          }
        }
        nextNodes = filtered;
        popIndent();
        db("filter out non-leaf:", nextNodes);

        if (nextNodes.isEmpty())
          break;
        leafNodes = nextNodes;
      }

      List<Integer> result = new ArrayList<>();
      for (var node : leafNodes)
        result.add(node.name);

      return result;
    }

    private class Node {
      int name;
      boolean visited;
      List<Node> children = new ArrayList<>();

      Node(int val) {
        this.name = val;
      }

      @Override
      public String toString() {
        var s = sb();
        s.append("#").append(name);
        s.append(visited ? " V " : "   ");
        s.append("->( ");
        for (var n : children) {
          s.append(n.name).append(' ');
        }
        s.append(") ");
        return s.toString();
      }

    }
  }

  class Tree2 implements Alg {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      // Construct graph
      var nodes = new Node[n];
      for (int i = 0; i < n; i++)
        nodes[i] = new Node(i);

      for (var edge : edges) {
        var na = nodes[edge[0]];
        var nb = nodes[edge[1]];
        na.children.add(nb);
        nb.children.add(na);
      }

      // build list of leaf nodes

      List<Node> leafNodes = new ArrayList<>(nodes.length);
      for (var node : nodes) {
        if (node.children.size() <= 1)
          leafNodes.add(node);
      }

      while (true) {

        List<Node> nextNodes = new ArrayList<>(leafNodes.size());

        pushIndent();

        for (var node : leafNodes) {
          db(node);
          if (node.children.isEmpty())
            continue;

          var dest = node.children.get(0);
          // If dest node only has one edge, then we are down to only two nodes; skip this one
          if (dest.children.size() == 1)
            continue;

          node.children.remove(0);
          dest.children.remove(node);
          db("...adding modified:", dest);
          if (!dest.flag) {
            dest.flag = true;
            nextNodes.add(dest);
          }
        }
        popIndent();
        db("next nodes:", nextNodes);

        db("filterout nodes with deg > 1");
        pushIndent();
        // Filter out next nodes that have degree > 1, and mark
        // remaining as visited
        List<Node> filtered = new ArrayList<>(nextNodes.size());
        for (var node : nextNodes) {
          node.flag = false;
          if (node.children.size() <= 1) {
            db("...retaining node:", node);
            // node.vis = true;
            filtered.add(node);
          }
        }
        nextNodes = filtered;
        popIndent();
        db("filter out non-leaf:", nextNodes);

        if (nextNodes.isEmpty())
          break;
        leafNodes = nextNodes;
      }

      List<Integer> result = new ArrayList<>();
      for (var node : leafNodes)
        result.add(node.name);

      return result;
    }

    private class Node {
      int name;
      boolean flag; // Flag for whether node is already in a particular list
      List<Node> children = new ArrayList<>();

      Node(int val) {
        this.name = val;
      }

      @Override
      public String toString() {
        var s = sb();
        s.append("#").append(name);
        s.append("->( ");
        for (var n : children) {
          s.append(n.name).append(' ');
        }
        s.append(") ");
        return s.toString();
      }

    }
  }

}
