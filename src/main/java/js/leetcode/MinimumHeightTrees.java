package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {
    //x(3, "[[0,1],[0,2]]");
    // x(4, "[[1,0],[1,2],[1,3]]");
    // x(4, "1 3 3 2 2 0");
    // x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
    x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
  }

  private void x(int n, String ns) {
    var nums = extractNums(ns);
    var edges = new int[nums.length / 2][2];

    for (int i = 0; i < nums.length; i += 2) {
      int j = i / 2;
      edges[j][0] = nums[i];
      edges[j][1] = nums[i + 1];
    }
    var alg = new Solution();
    db = true;
    var result = alg.findMinHeightTrees(n, edges);
    pr(result);
  }

  class Solution {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {

      // Perform Floyd-Warshall algorithm.

      // Construct edge incidence matrix.   
      mAdjMat = new int[n][n];
      mPathMat = new int[n][n];
      for (var row : mAdjMat)
        Arrays.fill(row, Integer.MAX_VALUE);
      for (int i = 0; i < n; i++) {
        setEdge(i, i, 0);
      }
      if (db) {
        for (var row : mPathMat)
          Arrays.fill(row, Integer.MIN_VALUE);
      }

      for (var edge : edges) {
        var a = edge[0];
        var b = edge[1];
        setEdge(a, b, 1);
        setEdge(b, a, 1);
        setPath(a, b, b);
        setPath(b, a, a);
      }
      db("adjmat:", INDENT, strTable(mAdjMat));

      {
        for (int k = 0; k < n; k++) {
          for (int i = 0; i < n; i++) {
            var distIK = getEdge(i, k);
            if (distIK == Integer.MAX_VALUE)
              continue;
            for (int j = 0; j < n; j++) {
              var distKJ = getEdge(k, j);
              if (distKJ == Integer.MAX_VALUE)
                continue;
              var distIJ = getEdge(i, j);
              var newdist = distIK + distKJ;
              if (newdist < distIJ) {
                pr("dist from", i, "to", j, "improving from", distIJ, "to", newdist);
                setEdge(i, j, newdist);
                pr("path from", i, "to", j, "now goes through", k);
                setPath(i, j, k);
              }
            }
          }
        }
      }
      db("adjmat:", INDENT, strTable(mAdjMat));

      // Set k = graph radius (the maximum distance between any two vertices), and save all pairs
      // of vertices that are separated by this distance

      List<Integer> diam = new ArrayList<>();

      var k = 0;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          var k2 = mAdjMat[i][j];
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
      db("diameter, k:", k);

      // For each pair of diameter vertices (u,v), the node(s) halfway along the path between these two are 
      // added to the answer list.

      var result = new HashSet<Integer>();
      var steps = new int[k + 1];
      int ki0 = k / 2;
      for (int j = 0; j < diam.size(); j += 2) {
        var a = diam.get(j);
        var b = diam.get(j + 1);
        db("walking from", a, "to", b);

        var posn = a;
        int cursor = 0;
        steps[cursor++] = posn;

        while (posn != b) {
          int c = b;
          while (true) {
            var c2 = mPathMat[posn][c];
            if (c == c2)
              break;
            c = c2;
          }
          posn = c;
          steps[cursor++] = posn;
        }

        db("steps:", str(steps));
        result.add(steps[ki0]);
        if ((k & 1) != 0)
          result.add(steps[ki0 + 1]);
      }

      return new ArrayList<Integer>(result);
    }

    private void setEdge(int a, int b, int distance) {
      mAdjMat[a][b] = distance;
      //  mAdjMat[b][a] = distance;
    }

    private void setPath(int src, int dest, int nextVertex) {
      //db("set path", src, "=>", dest, "next vert:", nextVertex);
      mPathMat[src][dest] = nextVertex;
    }

    private int getEdge(int a, int b) {
      return mAdjMat[a][b];
    }

    private int[][] mAdjMat;
    private int[][] mPathMat;

  }
}
