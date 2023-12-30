package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    if (false) {
      x(200, 200, 1965);
      return;
    }

    //    x("[12,11,7],[2,3,14]]", 3, -1);
    //
    //    x("[12,11,7,12,2],[2,3,4,1,13]]", 5, -1);
    //
    //    x("[[1,6,12,1,3],[8,4,6,10,5],[12,11,7,12,2],[2,3,4,1,13],[14,6,0,14,13]]", 5, -1);

    x("[[0,1,2,3,4,5,6],[7,8,9,10,11,12,13],[14,15,16,17,18,19,20],[21,22,23,24,25,26,27],[28,29,30,31,32,33,34],[35,36,37,38,39,40,41],[42,43,44,45,46,47,48]]",
        7, -1);

    // x("[[9,9,4],[6,6,8],[2,1,1]]", 3, 4);
    // x("[[3,4,5],[3,2,6],[2,2,1]]", 3, 4);
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

  private String pt(int cursor, int[][] matrix) {
    int w = matrix[0].length;
    return "(" + (cursor % (w + 1) - 1) + " " + (cursor / (w + 1) - 1) + ")";
  }

  public int longestIncreasingPath(int[][] matrix) {
    var width = matrix[0].length;
    var height = matrix.length;
    var a = pad(matrix);
    var rowOffset = width + 1;
    var origin = rowOffset + 1;

    final int qlen = width * height;
    int[] q1 = new int[qlen];
    int[] q2 = new int[qlen];
    int c1 = 0;
    int c2 = 0;

    // Don't push the same cell on the stack if it was already visited in this or an earlier iteration
    var visited = new int[a.length];
    // Arrays.fill(visited, Integer.MAX_VALUE);
    // Populate the initial horizon with those cells that have no in edges.
    {
      var cursor = origin;
      for (int y = 0; y < height; y++, cursor += rowOffset - width) {
        for (int x = 0; x < width; x++, cursor++) {
          var mc = a[cursor];
          if (!(x > 0 && a[cursor - 1] < mc) //
              && !(x < width - 1 && a[cursor + 1] < mc) //
              && !(y > 0 && a[cursor - rowOffset] < mc) //
              && !(y < height - 1 && a[cursor + rowOffset] < mc) //
          ) {
            q1[c1++] = cursor;
            pr("c1:", c1);
          }
        }
      }
    }

    // Keep advancing a step in the BFS until the horizon is empty.
    // The longest path length is the number of iterations.

    int maxQueueSize = 0;
    int len = 0;
    while (c1 != 0) {
      len++;
      pr(VERT_SP, "length:", len, "horizon:", darray(q1, c1));
      c2 = 0;
      if (maxQueueSize < c1) {
        maxQueueSize = c1;
        pr("maxQueueSize:", maxQueueSize);
      }
      while (c1-- != 0) {
        var ca = q1[c1];
        //  pr("popped cursor", ca, "pt:", pt(ca, matrix), "visited:", visited[ca]);
        if (visited[ca] >= len)
          continue;
        visited[ca] = len;
        //  pr("...setting visited:", ca, "pt:", pt(ca, matrix));
        var v = a[ca];
        var cb = ca - 1;
        if (v < a[cb]) {
          q2[c2++] = cb;
        }
        cb = ca + 1;
        if (v < a[cb]) {
          q2[c2++] = cb;
        }
        cb = ca - rowOffset;
        if (v < a[cb]) {
          q2[c2++] = cb;

        }
        cb = ca + rowOffset;
        if (v < a[cb]) {
          q2[c2++] = cb;

        }
      }
      var tmp = q2;
      q2 = q1;
      q1 = tmp;
      c1 = c2;
    }
    pr("maxQueueSize:", maxQueueSize);
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

// ------------------------------------------------------------------

class SLOWLongestIncreasingPath extends LeetCode {

  @Override
  public void run() {
    throw notSupported();
  }

  public int longestIncreasingPath(int[][] matrix) {

    matrixWidth = matrix[0].length;
    matrixHeight = matrix.length;
    visitFlags = new int[matrixHeight][matrixWidth];
    this.matrix = matrix;

    List<State> stack = new ArrayList<>();

    for (int y = matrixHeight - 1; y >= 0; y--) {
      for (int x = matrixWidth - 1; x >= 0; x--) {
        stack.add(new State(x, y, 1));
      }
    }
    // Sort so higher priority states are at the start of the queue,
    // to perform a BFS
    stack.sort((a, b) -> compareStates(a, b));

    int maxLen = 0;

    int sp = 0;
    while (stack.size() > sp) {
      var s = stack.get(sp++);
      maxLen = Math.max(maxLen, s.pathLength);
      var oldPathLength = visitFlags[s.y][s.x];
      if (oldPathLength >= s.pathLength) {
        continue;
      }
      visitFlags[s.y][s.x] = s.pathLength;

      // Expand search to neighbors with greater values 
      nbrs.clear();
      searchNeighbor(s, -1, 0);
      searchNeighbor(s, 1, 0);
      searchNeighbor(s, 0, -1);
      searchNeighbor(s, 0, 1);

      // Sort so lower-valued states are searched first
      if (nbrs.size() >= 2)
        nbrs.sort((a, b) -> compareStates(a, b));
      stack.addAll(nbrs);
    }
    return maxLen;
  }

  private void searchNeighbor(State s, int xm, int ym) {
    int nx = s.x + xm;
    if (nx < 0 || nx >= matrixWidth)
      return;

    int ny = s.y + ym;
    if (ny < 0 || ny >= matrixHeight)
      return;
    if (matrix[ny][nx] <= s.value()) {
      return;
    }
    var expandedPathLength = s.pathLength + 1;
    var newState = new State(nx, ny, expandedPathLength);
    nbrs.add(newState);
  }

  private class State {

    State(int x, int y, int pathLength) {
      this.x = x;
      this.y = y;
      this.pathLength = pathLength;
    }

    public int value() {
      return matrix[y][x];
    }

    @Override
    public String toString() {
      return "(" + x + " " + y + " len:" + pathLength + " val:" + value() + ")";
    }

    final int x;
    final int y;
    final int pathLength;
  }

  private int compareStates(State a, State b) {
    var result = a.value() - b.value();
    if (result == 0)
      result = a.y - b.y;
    if (result == 0)
      result = a.x - b.x;
    return result;
  }

  private List<State> nbrs = new ArrayList<>(4);
  private int[][] visitFlags;
  private int[][] matrix;
  private int matrixWidth;
  private int matrixHeight;

}
