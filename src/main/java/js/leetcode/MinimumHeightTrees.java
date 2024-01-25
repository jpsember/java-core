package js.leetcode;

import static js.base.Tools.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import js.file.Files;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {

    x(3, "[[0,1],[0,2]]", "0");

    x(5, "0 4 4 2 4 1 1 3", "1 4");

    x(4, "[[1,0],[1,2],[1,3]]", "1");

    x(1, "[]", "0");
    x(2, "[[0,1]]", "0 1");

    if (true)
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

      alg = new Tree();
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

  class Tree implements Alg {

    private int mK;

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

      // Choose an arbitrary node as the root of a tree
      var root = nodes[0];

      deleteEdgesToParentNodes(root);
      // Determine depth and height of each node relative to this tree.
      calcHeights(root, null);

      mK = root.height;
      calcParentPaths(root);

      if (db) {
        db(VERT_SP, "nodes after calc everything:");
        db("k:", mK);
        for (var nd : nodes) {
          pr(nd);
        }
        db(VERT_SP);
      }

      List<Integer> result = new ArrayList<>();

      // All nodes that have a height + parent length "equal" to half the root's height are added to the
      // output.  We have to compensate for the root's height being an odd number...

      var k = mK;

      int v1 = k / 2;
      int v2 = (root.height % 2 == 1) ? v1 + 1 : v1;

      if (db) {
        db("root:", root);
        db("k:", k);
        db("v1:", v1);
        db("v2:", v2);
      }

      for (var node : nodes) {
        pushIndent();
        if (db)
          db("child:", node);
        if ((node.height == v1 && node.parentLen == v2) || (node.height == v2 && node.parentLen == v1)) {
          if (db)
            db("...adding to result");
          result.add(node.name);
        }
        popIndent();
      }
      return result;
    }

    private List<Integer> work = new ArrayList<>(20);

    /**
     * Walk a subtree, delete edges leading to parent nodes
     */
    private void deleteEdgesToParentNodes(Node root) {

      var stack = new Stack<Node>();
      stack.add(root);
      stack.add(null);

      while (!stack.isEmpty()) {
        var parent = stack.pop();
        var node = stack.pop();
        // Delete any edges that go back to the parent

        work.clear();
        for (int i = node.children.size() - 1; i >= 0; i--) {
          var child = node.children.get(i);
          if (child == parent) {
            // pr("...removing edge to parent");
            work.add(i);
            //  node.children.remove(i);
          }
        }
        for (var index : work) {
          node.children.remove(index.intValue());
        }

        for (var child : node.children) {
          stack.add(child);
          stack.add(node);
        }
      }
      //  deleteEdgesToParentNodes(child, node);
    }

    /**
     * Walk a subtree, calculating height of each node
     */
    private void calcHeights(Node node, Node parent) {
      pr("walk2, node:", node.name);

      for (var child : node.children) {
        calcHeights(child, node);
        var childLongestPath = child.height;
        node.height = Math.max(node.height, 1 + childLongestPath);
      }
    }

    /**
     * Walk subtree, calculating longest path through parent node
     */
    private void calcParentPaths(Node node) {

      pr(VERT_SP, "calcParentPaths", node.name);

      // Determine the two highest height values of this node's children, and the node associated with the highest's
      Node maxChild1 = null;
      Node maxChild2 = null;

      for (var n : node.children) {
        if (maxChild1 == null || n.height > maxChild1.height)
          maxChild1 = n;
        else if (maxChild2 == null || n.height > maxChild2.height)
          maxChild2 = n;
        pr("...child:", n.name, "max1:", maxChild1, "max2:", maxChild2);
      }

      for (var n : node.children) {
        pr(VERT_SP, "calc parent path, ht1,2:", maxChild1, maxChild2);
        int longPath = 1 + node.parentLen;
        pr("...initial val is parent's longest path:", longPath);

        var c = 0;
        if (maxChild1 != n)
          c = 2 + maxChild1.height;
        else if (maxChild2 != null)
          c = 2 + maxChild2.height;

        if (c > longPath)
          longPath = c;

        n.parentLen = longPath;
        mK = Math.min(mK, n.height + n.parentLen);
      }

      for (var n : node.children) {
        calcParentPaths(n);
      }
    }

    private class Node {
      int name;
      List<Node> children = new ArrayList<>();
      // Length of longest path to leaf node
      int height;
      // Length of longest path through parent node
      int parentLen;

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
        s.append(" p:").append(d(parentLen));
        s.append(")->( ");
        for (var n : children) {
          s.append(n.name).append(' ');
        }
        s.append(") ");
        return s.toString();
      }

    }
  }

}
