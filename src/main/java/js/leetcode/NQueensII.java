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
    verify(c, n);
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

  public int totalNQueens(int n) {
    var squareFlags = new long[N][N];
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
        //pr("y:", y, "x:", x, "f:", bitStr(row[x] | (1L << 62)));
      }
    }

    Map<Long, Integer> countMapA = new HashMap<>();
    // There is a single solution for n=0 queens in the n=0 map
    countMapA.put(0L, 1);
    // Auxilliary map 
    Map<Long, Integer> countMapB = new HashMap<>();

    int sum = 0;
    for (int m = 1; m <= n; m++) {
      sum = 0;
      countMapB.clear();
      db(VERT_SP, "size", m);

      // Look through all the entries for map from m-1
      for (var ent : countMapA.entrySet()) {
        long used = ent.getKey();
        int queenCount = countQueens(used);
        int variants = ent.getValue();
        pr("map entry; # queens:", queenCount, "variants:", variants);
        // pr("rowFlags:", bitStr(used), "count:", variants, "queens:", queenCount);

        // Try to place one and two additional queens to store size-1 and size-0 solutions

        // If this is already an m-1 solution, store it
        if (queenCount == m - 1) {
          pr("...already an m-1 solution, storing");
          countMapB.put(used, variants);
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
          pr("x:", x, "y:", y, "f:", bitStr(sf));
          if ((sf & used) == 0) {
            pr("...found slot for queen #" + (queenCount + 1), "x:", x, "y:", y);
            countMapB.put(used | sf, variants);
            queenCount++;
            // See if we can place an additional queen for an m solution
            if (queenCount + 1 == m) {
              pr("...scanning for a queen #" + queenCount, "solution");
              for (int i2 = i + 1; i2 < scanCount; i2++) {
                if (i2 < m) {
                  y = m - 1;
                  x = i2;
                } else {
                  y = i2 - m;
                  x = m - 1;
                }
                pr("scanCount:", scanCount, "i2:", i2, "x:", x, "y:", y);
                sf = squareFlags[y][x];
                pr("x:", x, "y:", y, "f:", bitStr(sf));
                if ((sf & used) == 0) {
                  countMapB.put(used | sf, variants);
                  sum += variants;
                  pr("...found slot for queen #" + (queenCount + 1), "x:", x, "y:", y);
                  pr("...found size m solution, # variants:", variants);
                }
              }
            }
          }
        }
        //        {
        //          var x = size - 1;
        //          for (int y = 1; y < size; y++) {
        //            var sf = squareFlags[y][x];
        //            pr("x:", x, "y:", y, "f:", bitStr(sf));
        //            if ((sf & used) == 0) {
        //              newBoardKeys.add(sf | used);
        //              newBoardValues.add(count);
        //              sum += count;
        //            }
        //          }
        //        }
      }
      var tmp = countMapA;
      countMapA = countMapB;
      countMapB = tmp;
      //
      //      for (int i = 0; i < newBoardKeys.size(); i++)
      //        countMap.put(newBoardKeys.get(i), newBoardValues.get(i));
      db("total for n=", m, "is", sum);

    }
    return sum;
  }
  //
  //  private static class Solution {
  //    int boardSize;
  //    int numQueens;
  //  }

}
