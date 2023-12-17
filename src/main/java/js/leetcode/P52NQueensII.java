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
 * 
 * I think I am spending a lot of time in hash table lookups.
 */
public class P52NQueensII extends LeetCode {

  public static void main(String[] args) {
    new P52NQueensII().run();
  }

  public void run() {
    x(9, 352);
    //    x(5, 10);
    //    x(2, 0);
    //    x(4, 2);
  }

  private void x(int n, int exp) {
    var c = totalNQueens(n);
    pr("n:", n, "c:", c);
    verify(c, exp);
  }

  public int totalNQueens(int n) {
    int n2 = n / 2;
    if (n % 2 == 0)
      return 2 * aux(n, 0, n2);
    else
      return aux(n, n2, n2 + 1) + 2 * aux(n, 0, n2);
  }

  private int aux(int n, int colStart, int colEnd) {
    if (colStart == colEnd)
      return 0;
    Map<Long, Integer> prevHeightMap = new HashMap<>();
    Map<Long, Integer> nextHeightMap = new HashMap<>();

    // There is a single solution for n=0 queens in the height=zero map
    prevHeightMap.put(0L, 1);
    int solutionCount = 0;
    var x0 = colStart;
    var x1 = colEnd;

    for (int height = 1; height <= n; height++) {
      solutionCount = 0;
      var row = squareFlags[height - 1];
      nextHeightMap.clear();
      for (var ent : prevHeightMap.entrySet()) {
        long rowUsageFlags = ent.getKey();
        var solutions = ent.getValue();
        for (int x = x0; x < x1; x++) {
          var candidateMoveFlags = row[x];
          if ((candidateMoveFlags & rowUsageFlags) == 0) {
            var newUsageFlags = rowUsageFlags | candidateMoveFlags;
            nextHeightMap.put(newUsageFlags, nextHeightMap.getOrDefault(newUsageFlags, 0) + solutions);
            solutionCount += solutions;
          }
        }
      }
      var tmp = prevHeightMap;
      prevHeightMap = nextHeightMap;
      nextHeightMap = tmp;

      x0 = 0;
      x1 = n;
    }

    return solutionCount;
  }

  // Prepare grid indicating how a queen placed on a square affects the row usage
  private static final int NMax = 9;
  private static final long[][] squareFlags = new long[NMax][NMax];

  static {
    for (int y = 0; y < NMax; y++) {
      for (int x = 0; x < NMax; x++) {
        final int OFF_COL = 0;
        final int OFF_DIAG1 = OFF_COL + NMax;
        final int OFF_DIAG2 = OFF_DIAG1 + NMax * 2 - 1;
        squareFlags[y][x] = (1L << (OFF_COL + x)) //
            | (1L << (OFF_DIAG1 + x + y)) //
            | (1L << (OFF_DIAG2 + x - y + NMax - 1));
      }
    }
  }

}
