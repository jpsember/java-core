package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Ok, I couldn't figure out why it wasn't working; added a bunch of diagnostic
 * code.
 * 
 * I think the 'allowing m or m-1' in the current map is faulty logic.
 * 
 * New approach: start with a board that is a full row, but unit height, then
 * add rows.
 */
public class P52NQueensII extends LeetCode {

  public static void main(String[] args) {
    new P52NQueensII().run();
  }

  public void run() {
    x(5, 10);
    x(2, 0);
    x(4, 2);
  }

  private void x(int n, int exp) {
    var c = totalNQueens(n);
    pr("n:", n, "c:", c);
    verify(c, exp);
  }

  public int totalNQueens(int n) {
    // Prepare grid indicating how a queen placed on a square affects the row usage
    long[][] squareFlags = new long[n][n];
    for (int y = 0; y < n; y++) {
      var row = squareFlags[y];
      for (int x = 0; x < n; x++) {
        final int OFF_COL = 0;
        final int OFF_DIAG1 = OFF_COL + n;
        final int OFF_DIAG2 = OFF_DIAG1 + n * 2 - 1;
        row[x] = (1L << (OFF_COL + x)) //
            | (1L << (OFF_DIAG1 + x + y)) //
            | (1L << (OFF_DIAG2 + x - y + n - 1));
      }
    }
    Map<Long, Integer> prevHeightMap = new HashMap<>();
    Map<Long, Integer> nextHeightMap = new HashMap<>();

    // There is a single solution for n=0 queens in the height=zero map
    prevHeightMap.put(0L, 1);
    int solutionCount = 0;
    for (int height = 1; height <= n; height++) {
      solutionCount = 0;
      nextHeightMap.clear();
      for (var ent : prevHeightMap.entrySet()) {
        long rowUsageFlags = ent.getKey();
        int queenCount = Integer.bitCount(((int) rowUsageFlags) & ((1 << n) - 1));
        var solutions = ent.getValue();
        for (int x = 0; x < n; x++) {
          var candidateMoveFlags = squareFlags[height - 1][x];
          if ((candidateMoveFlags & rowUsageFlags) == 0) {
            var newUsageFlags = rowUsageFlags | candidateMoveFlags;
            nextHeightMap.put(newUsageFlags, nextHeightMap.getOrDefault(newUsageFlags, 0) + solutions);
            if (queenCount + 1 == height)
              solutionCount += solutions;
          }
        }
      }
      var tmp = prevHeightMap;
      prevHeightMap = nextHeightMap;
      nextHeightMap = tmp;
    }
    return solutionCount;
  }

}
