package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import js.data.DataUtil;

public class MinimumHeightTrees extends LeetCode {

  public static void main(String[] args) {
    new MinimumHeightTrees().run();
  }

  public void run() {
    x(1, "[]", "0");
    x(2, "[[0,1]]", "0 1");
    //x(3, "[[0,1],[0,2]]");
    // x(4, "[[1,0],[1,2],[1,3]]");
    // x(4, "1 3 3 2 2 0");
    // x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
    // x(6, "[[3,0],[3,1],[3,2],[3,4],[5,4]]");
  }

  private void x(int n, String ns) {
    x(n, ns, null);
  }

  private void x(int n, String ns, String exp) {
    var nums = extractNums(ns);
    var edges = new int[nums.length / 2][2];
    int[] expected = null;
    if (exp != null)
      expected = extractNums(ns);

    for (int i = 0; i < nums.length; i += 2) {
      int j = i / 2;
      edges[j][0] = nums[i];
      edges[j][1] = nums[i + 1];
    }
    var alg = new Solution();
    db = true;
    var result = alg.findMinHeightTrees(n, edges);
    if (expected != null) {
      result.sort(null);
      var res = DataUtil.intArray(result);
      Arrays.sort(res);
      Arrays.sort(expected);
      verify(res, expected);
    }
    pr(result);
  }

  class Solution {

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
      db("adjmat:", INDENT, strTable(dist));

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
      db("diameter, k:", k);

      // For each pair of diameter vertices (u,v), the node(s) halfway along the path between these two are 
      // added to the answer list.

      var result = new HashSet<Integer>();
      int ki0 = k / 2;
      int ki1 = ki0 + (k & 1);

      for (int j = 0; j < diam.size(); j += 2) {
        var a = diam.get(j);
        var b = diam.get(j + 1);

        var posn = a;
        int cursor = 1; // already at start
        if (cursor >= ki0)
          result.add(posn);

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
}
