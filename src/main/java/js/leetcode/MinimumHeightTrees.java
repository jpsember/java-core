package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {
    // x(4, "[[1,0],[1,2],[1,3]]");
    // x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
    // x(3, "[[0,1],[0,2]]");
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
      for (var row : mAdjMat)
        Arrays.fill(row, Integer.MAX_VALUE);

      for (var edge : edges)
        setEdge(edge[0], edge[1], 1);
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
              if (newdist < distIJ)
                setEdge(i, j, newdist);
            }
          }
        }
      }
      db("adjmat:", INDENT, strTable(mAdjMat));

      // Set k = graph radius (the maximum distance between any two vertices)
      var k = 0;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          k = Math.max(k, mAdjMat[i][j]);
        }
      }
      db("diameter, k:", k);

      // While k > 1, delete all vertices that are at a distance k from any other 
      // (still existing) vertex, and subtract 2 from k.  We are 'peeling an onion' of
      // diameter pairs.

      var dead = new boolean[n];

      while (k > 1) {
        db("deleting vertices separated by k=", k);
        for (int i = 0; i < n; i++) {
          if (dead[i])
            continue;
          for (int j = i + 1; j < n; j++) {
            if (dead[j])
              continue;
            var dist = mAdjMat[i][j];
            if (dist == k) {
              dead[i] = true;
              dead[j] = true;
              db("...deleting vertices:", i, j);
            }
          }
        }
        k -= 2;
      }

      List<Integer> result = new ArrayList<>();
      for (int i = 0; i < n; i++)
        if (!dead[i])
          result.add(i);

      return result;
    }

    private void setEdge(int a, int b, int distance) {
      if (a < b)
        mAdjMat[a][b] = distance;
      else
        mAdjMat[b][a] = distance;
    }

    private int getEdge(int a, int b) {
      if (a < b)
        return mAdjMat[a][b];
      else
        return mAdjMat[b][a];
    }

    private int[][] mAdjMat;

  }
}
