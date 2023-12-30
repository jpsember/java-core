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
    x("[[1,6,12,1,3],[8,4,6,10,5],[12,11,7,12,2],[2,3,4,1,13],[14,6,0,14,13]]", 5, -1);
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

    var result = longestIncreasingPath(matrix);
    pr("path length:", result);

    if (expected < 0)
      expected = new SLOWLongestIncreasingPath().longestIncreasingPath(matrix);

    verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int longestIncreasingPath(int[][] matrix) {
    matrix = pad(matrix);
    int w = matrix[0].length;
    int h = matrix.length;
    List<Integer> q = new ArrayList<>();
    for (int y = 0; y < h; y++) {
      var row = matrix[y];
      for (int x = 0; x < w; x++) {

      }
    }
    return -1;
  }

  // Pad matrix with -1 so we don't need to do clipping
  private int[][] pad(int[][] matrix) {
    pr("orig:", INDENT, strTable(matrix));
    int w = matrix[0].length + 2;
    int h = matrix.length + 2;
    var res = new int[h][w];
    for (int y = 0; y < h; y++) {
      var row = res[y];
      Arrays.fill(row, -1);
      if (y >= 1 && y < h - 1)
        System.arraycopy(matrix[y - 1], 0, row, 1, h - 2);
    }
    pr("padded:", INDENT, strTable(res));
    return res;
  }

  //  private int[][] visitFlags;
  //  private int[][] matrix;
  //  private int matrixWidth;
  //  private int matrixHeight;
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

    if (db)
      db("matrix:", INDENT, strTable(matrix));

    List<State> stack = new ArrayList<>();

    for (int y = matrixHeight - 1; y >= 0; y--) {
      for (int x = matrixWidth - 1; x >= 0; x--) {
        stack.add(new State(x, y, 1));
      }
    }
    // Sort so higher priority states are at the start of the queue,
    // to perform a BFS
    stack.sort((a, b) -> compareStates(a, b));

    // pr("stack contents:", stack);
    int maxLen = 0;

    int sp = 0;
    while (stack.size() > sp) {
      var s = stack.get(sp++);
      maxLen = Math.max(maxLen, s.pathLength);
      db(VERT_SP, "popped state:", s);

      var oldPathLength = visitFlags[s.y][s.x];
      if (oldPathLength >= s.pathLength) {
        db("...already visited by longer path:", oldPathLength);
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
    // db("...expand to:", nx, ny, "? value", matrix[ny][nx]);
    if (matrix[ny][nx] <= s.value()) {
      // db("......no, value isn't higher");
      return;
    }

    var expandedPathLength = s.pathLength + 1;
    //    if (visitFlags[ny][nx] >= expandedPathLength) {
    //      db(".......no, path length is not less than ours");
    //      return;
    //    }

    var newState = new State(nx, ny, expandedPathLength);
    db("......pushing", newState);
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
