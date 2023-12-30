package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Very slow (bottom 5%).
 * 
 * Maybe a breadth-first search?
 */
public class LongestIncreasingPathInAMatrix extends LeetCode {

  public static void main(String[] args) {
    new LongestIncreasingPathInAMatrix().run();
  }

  public void run() {

    x("[[13,6,16,6,16,4],[9,13,5,13,7,11],[11,7,9,17,0,7],[7,8,5,14,11,8],[14,2,8,7,9,5],[1,15,3,11,11,6]]",
        6, -1);
    {
      x(200, 200, 1965);
    }
    x("[12,11,7],[2,3,14]]", 3, -1);
    x("[12,11,7,12,2],[2,3,4,1,13]]", 5, -1);
    x("[[1,6,12,1,3],[8,4,6,10,5],[12,11,7,12,2],[2,3,4,1,13],[14,6,0,14,13]]", 5, -1);
    x("[[0,1,2,3,4,5,6],[7,8,9,10,11,12,13],[14,15,16,17,18,19,20],[21,22,23,24,25,26,27],[28,29,30,31,32,33,34],[35,36,37,38,39,40,41],[42,43,44,45,46,47,48]]",
        7, -1);
    x("[[9,9,4],[6,6,8],[2,1,1]]", 3, 4);
    x("[[3,4,5],[3,2,6],[2,2,1]]", 3, 4);
  }

  private void x(int width, int height, int seed) {
    var matrix = new int[height][width];
    rand(seed);
    for (var row : matrix) {
      for (int x = 0; x < width; x++)
        row[x] = rand().nextInt(90) + 10;
    }
    db = false;
    pr(strTable(matrix));
    checkpoint("starting");
    int result = 0;
    for (int i = 0; i < 100; i++)
      result = longestIncreasingPath(matrix);
    checkpoint("stopping");
    pr("path length:", result);
  }

  private void x(String s, int width, int expected) {
    db = true;

    var matrix = extractMatrix(s, width);

    if (db)
      db("matrix:", INDENT, strTable(matrix));

    var result = longestIncreasingPath(matrix);
    pr("path length:", result);

    if (expected < 0)
      expected = new SLOWLongestIncreasingPath().longestIncreasingPath(matrix);

    verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int longestIncreasingPath(int[][] matrix) {
    return -1;
  }

}

// ------------------------------------------------------------------

class SLOWLongestIncreasingPath extends LeetCode {

  @Override
  public void run() {
    throw notSupported();
  }

  public int longestIncreasingPath(int[][] matrix) {
    var width = matrix[0].length;
    var height = matrix.length;
    var a = pad(matrix);
    var rowOffset = width + 1;
    var origin = rowOffset + 1;
    var cursorEnd = a.length - rowOffset;

    // The maximum frontier size is the number of cells in the matrix, in the case
    // where they are all the same value and thus have no incoming edges.
    final int qlen = width * height;
    int[] q1 = new int[qlen];
    int[] q2 = new int[qlen];
    int c1 = 0;
    int c2 = 0;

    int[] cursorMoves = { -1, 1, -rowOffset, rowOffset };

    var inEdgeCount = new byte[a.length];

    // Determine in edge counts for each cell, and populate the initial frontier
    // with those cells that have edge counts of zero
    {
      for (var cursor = origin; cursor < cursorEnd; cursor++) {
        var count = 0;
        var mc = a[cursor];
        if (mc < 0) // padding cell?
          continue;
        for (var cursorMove : cursorMoves) {
          var nb = a[cursor + cursorMove];
          // Don't include padding cells
          if (nb >= 0 && nb < mc)
            count++;
        }
        inEdgeCount[cursor] = (byte) count;
        if (count == 0)
          q1[c1++] = cursor;
      }
    }

    // Keep advancing a step in the BFS until the horizon is empty.
    // The longest path length is the number of iterations.

    int len = 0;
    while (c1 != 0) {
      len++;
      c2 = 0;

      while (c1-- != 0) {
        var ca = q1[c1];
        var v = a[ca];

        // Try moving to each adjacent cell

        for (var movement : cursorMoves) {
          // Determine index of adjacent cell
          var cb = ca + movement;
          // Is this a valid move (neighbor has higher value)?
          if (v >= a[cb])
            continue;
          // If we are the last possible visitor to this cell, add to frontier;
          // otherwise, act as if it's an invalid move
          if (--inEdgeCount[cb] != 0)
            continue;
          q2[c2++] = cb;
        }
      }
      var tmp = q2;
      q2 = q1;
      q1 = tmp;
      c1 = c2;
    }
    return len;
  }

  // Pad matrix with -1 so we don't need to do clipping, and convert to a flat array
  private int[] pad(int[][] matrix) {
    int w = matrix[0].length + 1;
    int h = matrix.length + 2;
    var res = new int[w * h];
    Arrays.fill(res, -1);
    int cursor = 1 + w;
    for (var row : matrix) {
      System.arraycopy(row, 0, res, cursor, w - 1);
      cursor += w;
    }
    return res;
  }

}
