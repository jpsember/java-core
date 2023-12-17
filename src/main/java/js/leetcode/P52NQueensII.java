package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class P52NQueensII extends LeetCode {

  public static void main(String[] args) {
    new P52NQueensII().run();
  }

  private static void ac() {
    for (int x1 = 0; x1 < N; x1++) {
      for (int y1 = 0; y1 < N; y1++) {
        var f1 = squareFlags[y1][x1];
        for (int x2 = 0; x2 < N; x2++) {
          for (int y2 = 0; y2 < N; y2++) {
            var f2 = squareFlags[y2][x2];
            int xd = x1 - x2;
            int yd = y1 - y2;
            if (x1 == x2 && y1 == y2)
              continue;
            boolean exp = (x1 == x2) || (y1 == y2) || (xd * xd == yd * yd);

            if (((f1 & f2) != 0L) != exp) {
              halt("x1,y1:", x1, y1, " x2,y2:", x2, y2, CR, bitStrFull(f1), CR, bitStrFull(f2));
            }
          }
        }
      }
    }
  }

  public void run() {
    x(5, 10);
    //    x(2, 0);
    //    x(4, 2);
  }

  private void x(int n, int exp) {
    var c = totalNQueens(n);
    pr("n:", n, "c:", c);
    verify(c, exp);
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
      for (var ent : prevSizeMap.entrySet()) {
        long used = ent.getKey();
        int queenCount = countQueens(used);
        int variants = ent.getValue();
        db(VERT_SP, "...entry, # queens:", queenCount, "variants:", variants);

        // Try to place one and two additional queens to get solutions for m-1 and m queens

        // If this is already an m-1 solution, store it
        if (queenCount == m - 1) {
          db("......already an m-1 solution, storing");
          addVariants(nextSizeMap, used, variants);
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
            var queenCountNew = queenCount + 1;
            var usedNew = used | sf;

            addVariants(nextSizeMap, usedNew, variants);
            db("...found spot for queen #", queenCountNew, "at:", x, y);
            if (queenCountNew == m) {
              currentVariantTotal += variants;
              db("......variants now", currentVariantTotal);
            }
            // See if we can place an additional queen for an m solution
            if (queenCountNew + 1 == m) {
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
                  var usedNew2 = usedNew | sf;
                  addVariants(nextSizeMap, usedNew2, variants);
                  currentVariantTotal += variants;
                  db("......found slot for queen #" + (queenCountNew + 1), "x:", x, "y:", y, "variants now:",
                      variants);
                }
              }
            }
          }
        }
      }

      //      {
      //        int check = 0;
      //        for (var ent : nextSizeMap.entrySet()) {
      //          //          ent.
      //          int queenCount = countQueens(ent.getKey());
      //          if (queenCount == m) {
      //            check += ent.getValue();
      //          }
      //        }
      //        checkState(check == currentVariantTotal, "expected:", currentVariantTotal, "but found in map:",
      //            check);
      //      }

      var tmp = prevSizeMap;
      prevSizeMap = nextSizeMap;
      nextSizeMap = tmp;
      db("total for n=", m, "is", currentVariantTotal);
    }
    return currentVariantTotal;
  }

  private static void addVariants(Map<Long, Integer> map, long key, int variants) {
    map.put(key, map.getOrDefault(key, 0) + variants);
  }

  private static int countQueens(long n) {
    // We only need to look at the first N bits
    return Integer.bitCount(((int) n) & ((1 << N) - 1));
  }

  private static final int N = 5; // 10;

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
}
