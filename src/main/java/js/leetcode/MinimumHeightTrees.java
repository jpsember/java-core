package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {

    //    x(4, "[[1,0],[1,2],[1,3]]");
    //    x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
    x(3, "[[0,1],[0,2]]");
  }

  private void x(int n, String ns) {
    var nums = extractNums(ns);
    var edges = new int[nums.length / 2][2];

    for (int i = 0; i < nums.length; i += 2) {
      int j = i / 2;
      edges[j][0] = nums[i];
      edges[j][1] = nums[i + 1];
    }
    var result = new Solution().findMinHeightTrees(n, edges);
    pr(result);
  }

  class Solution {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      db = true;
      // Construct graph
      var nodes = new Node[n];
      for (int i = 0; i < n; i++)
        nodes[i] = new Node(i);

      for (var edge : edges) {
        var na = nodes[edge[0]];
        var nb = nodes[edge[1]];
        na.siblings.add(nb);
        nb.siblings.add(na);
      }

      // Choose an arbitrary node as the root of a tree
      var root = nodes[0];

      // Determine depth and height of each node relative to this tree
      walk(root, 0);

      // All nodes that have a height and depth "equal" to half the root's height are added to the
      // output.  We have to compensate for the root's height being an odd number...
      int v1 = root.height / 2;
      int v2 = (root.height % 2 == 1) ? v1 + 1 : v1;

      db("root:", root);
      List<Integer> result = new ArrayList<>();
      for (var node : nodes) {
        pushIndent();
        db("child:", node);
        if ((node.depth == v1 && node.height == v2) || (node.depth == v2 && node.height == v1)) {
          db("...adding to result");
          result.add(node.val);
        }
        popIndent();
      }
      return result;
    }

    private void walk(Node node, int depth) {
      node.depth = depth;
      for (var sibling : node.siblings) {
        // If we've already visited this node, do nothing
        if (sibling.depth != null)
          continue;
        walk(sibling, depth + 1);
        node.height = Math.max(node.height, 1 + sibling.height);
      }
    }

    private class Node {
      int val;
      List<Node> siblings = new ArrayList<>();
      Integer depth;
      int height;

      Node(int val) {
        this.val = val;
      }

      @Override
      public String toString() {
        var s = sb();
        s.append("#").append(val);
        s.append("->( ");
        for (var c : siblings) {
          s.append(c.val).append(' ');
        }
        s.append(") ");
        if (depth != null)
          s.append("depth:").append(depth).append(" height:").append(height);
        return s.toString();
      }

    }
  }
}
