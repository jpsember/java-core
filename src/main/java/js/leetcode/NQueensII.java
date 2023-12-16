package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class NQueensII extends LeetCode {

  public static void main(String[] args) {
    new NQueensII().run();
  }

  public void run() {
    x(2, 0);
    x(4, 2);
  }

  private void x(int n, int exp) {
    var c = totalNQueens(n);
    pr("n:", n, "c:", c);
    verify(c, exp);
  }

  private static final int N = 10;

  private static int countQueens(long n) {
    int count = 0;
    for (int i = 0; i < N; i++) {
      if ((n & (1 << i)) != 0)
        count++;
    }
    return count;
  }

  private static final long[][] squareFlags = new long[N][N];

  static {
    for (int y = 0; y < N; y++) {
      var row = squareFlags[y];
      for (int x = 0; x < N; x++) {
        final int OFF_COL = 0;
        final int OFF_ROW = OFF_COL + N;
        final int OFF_DIAG1 = OFF_ROW + N;
        final int OFF_DIAG2 = OFF_DIAG1 + N * 2 - 1;
        row[x] = (1L << (OFF_COL + x)) //
            | (1L << (OFF_ROW + y)) //
            | (1L << (OFF_DIAG1 + x + y)) //
            | (1L << (OFF_DIAG2 + x - y + N - 1));
      }
    }
  }

  public int totalNQueens(int n) {
    Map<Long, Integer> prevSizeMap = new HashMap<>();
    Map<Long, Integer> nextSizeMap = new HashMap<>();

    // There is a single solution for n=0 queens in the n=0 map
    prevSizeMap.put(0L, 1);

    int currentVariantTotal = 0;
    for (int m = 1; m <= n; m++) {
      currentVariantTotal = 0;
      nextSizeMap.clear();
      db(VERT_SP, "size", m);

      // Look through all the entries for map from m-1
      pr("prev map size:", prevSizeMap.size());
      for (var ent : prevSizeMap.entrySet()) {
        long used = ent.getKey();
        int queenCount = countQueens(used);
        int variants = ent.getValue();
        db("map entry; # queens:", queenCount, "variants:", variants);

        // Try to place one and two additional queens to get solutions for m-1 and m queens

        // If this is already an m-1 solution, store it
        if (queenCount == m - 1) {
          db("...already an m-1 solution, storing");
          nextSizeMap.put(used, variants);
        }
        int scanCount = m * 2 - 1;
        for (int i = 0; i < scanCount; i++) {
          int x, y;
          if (i < m) {
            y = m - 1;
            x = i;
          } else {
            y = i - m;
            x = m - 1;
          }

          var sf = squareFlags[y][x];
          if ((sf & used) == 0) {
            var usedNew = used | sf;
            var newQueenCount = queenCount + 1;

            nextSizeMap.put(used | sf, variants);
            pr("found spot for queen #", newQueenCount, "at:", x, y);
            if (newQueenCount == m) {
              currentVariantTotal += variants;
              pr("...variants now", currentVariantTotal);
            }
            // See if we can place an additional queen for an m solution
            if (newQueenCount + 1 == m) {

              for (int i2 = i + 1; i2 < scanCount; i2++) {
                if (i2 < m) {
                  y = m - 1;
                  x = i2;
                } else {
                  y = i2 - m;
                  x = m - 1;
                }
                sf = squareFlags[y][x];
                if ((sf & usedNew) == 0) {
                  nextSizeMap.put(usedNew | sf, variants);
                  currentVariantTotal += variants;
                  db("...found slot for queen #" + (newQueenCount + 1), "x:", x, "y:", y, "variants now:",
                      variants);
                }
              }
            }
          }
        }
      }
      var tmp = prevSizeMap;
      prevSizeMap = nextSizeMap;
      nextSizeMap = tmp;
      db("total for n=", m, "is", currentVariantTotal);
    }
    return currentVariantTotal;
  }
}
