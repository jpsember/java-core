package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

// 51. N-Queens
//
// Runtime: Beats 23.72% of users with Java
// Memory:  Beats 5.3%
//
// Slightly better performance in other algorithms using recursive calls and an explicit board array
//

public class P51NQueens {

  public static void main(String[] args) {
    new P51NQueens().run();
  }

  private void run() {
    int[] expected = { 1, 0, 4, 2, 68, 92, -1, 92, 352 };
    int n = 9;
    var x = solveNQueens(n);
    pr("n:", n, "solutions:", x.size());
    pr("--------------------------------");
    for (var y : x) {
      for (var z : y) {
        pr(z);
      }
      pr("--------------------------------");
    }
    pr("n:", n, "solutions:", x.size());
    int exp = -1;
    if (expected.length >= n)
      exp = expected[n - 1];
    if (exp != x.size()) {
      pr("****** Unexpected number of solutions:", x.size(), "expected:", exp);
    }
  }

  // Beats 62% runtime, 98.55% memory
  public List<List<String>> solveNQueens(int n) {

    List<List<String>> stringResults = new ArrayList<>(352);

    // Coordinates of queen we are attempting to place
    int bx = 0;
    int by = 0;

    // Columns of queens already placed
    int[] queenSlots = new int[n];
    String[] queenRowStrings = new String[n];
    for (int i = 0; i < n; i++) {
      queenRowStrings[i] = "........Q........".substring(8 - i, 8 - i + n);
    }

    outer: while (true) {

      // Are we past the last row, or is the current row exhausted?
      if (by == n || bx == n) {
        // Is this the first row?  If so, done.
        if (by == 0)
          break;
        by--;
        // Advance to the next column
        bx = queenSlots[by] + 1;
        continue;
      }

      // Is this an invalid move?
      {
        for (int qy = 0; qy < by; qy++) {
          int qx = queenSlots[qy];
          int dx = qx - bx;
          int dy = qy - by;
          if (dx == 0  || dx == dy || dx == -dy) {
            bx++;
            continue outer;
          }
        }
      }

      // Place queen, move to next row
      queenSlots[by] = bx;
      by++;
      bx = 0;

      // If all n queens have been placed, it is a solution
      if (by == n) {
        List<String> strings = new ArrayList<>(n);
        for (int k : queenSlots) { 
          strings.add(queenRowStrings[k]);
        }
        stringResults.add(strings);
      }
    }
    return stringResults;
  }

}
