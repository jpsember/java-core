package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
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
    x("[[1,6,12,1,3],[8,4,6,10,5],[12,11,7,12,2],[2,3,4,1,13],[14,6,0,14,13]]", 5, 6);
    x("[[9,9,4],[6,6,8],[2,1,1]]", 3, 4);
    x("[[3,4,5],[3,2,6],[2,2,1]]", 3, 4);
  }

  private void x(String s, int width, int expected) {
    var matrix = extractMatrix(s, width);

    var result = longestIncreasingPath(matrix);
    pr("path length:", result);
    verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int longestIncreasingPath(int[][] matrix) {
    db = true;

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
    // Sort so higher priority states are at the END of the stack, so
    // we perform a depth-first search
    stack.sort((a, b) -> compareStates(b, a));

    int maxLen = 0;

    while (!stack.isEmpty()) {
      var topOfStack = stack.size() - 1;
      var s = stack.remove(topOfStack);

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

      // Sort so lower-valued states are searched first.
      // This definitely helps!
      if (nbrs.size() >= 2)
        nbrs.sort((a, b) -> compareStates(b, a));
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
