package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

// 51. N-Queens
//
// Runtime: Beats 23.72% of users with Java
// Memory:  Beats 5.3%
//
// Added reflection heuristic:
// Runtime: Beats 18.42% of users with Java
// Memory:  Beats 9.28%of users with Java

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

  public List<List<String>> solveNQueens(int n) {

    List<List<String>> result = new ArrayList<>(352);

    // Coordinates of queen we are attempting to place
    int bx = 0;
    int by = 0;

    int nMid = (n + 1) / 2;

    // Columns of queens already placed
    int[] queenSlots = new int[n];

    StringBuilder sb = new StringBuilder();

    while (true) {

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
        boolean valid = true;
        for (int qy = 0; qy < by; qy++) {
          int qx = queenSlots[qy];
          int dx = Math.abs(qx - bx);
          int dy = Math.abs(qy - by);
          if (dx == 0 || dy == 0 || dx == dy) {
            valid = false;
            break;
          }
        }
        if (!valid) {
          bx++;
          continue;
        }
      }

      // Place queen, move to next row
      queenSlots[by] = (byte) bx;
      by++;
      bx = 0;

      // If all n queens have been placed, it is a solution
      if (by == n) {

        // Special case: if we've generated a solution where the queen is past the midpoint of the first row,
        // stop and use symmetry to generate other solutions.
        if (queenSlots[0] >= nMid) {
          break;
        }

        List<String> soln = new ArrayList<>(n);
        for (int y = 0; y < n; y++) {
          sb.setLength(0);
          for (int x = 0; x < n; x++) {
            if (x == queenSlots[y])
              sb.append('Q');
            else
              sb.append('.');
          }
          soln.add(sb.toString());
        }

        result.add(soln);

      }
    }

    // Use symmetry to generate other solutions
    if (!result.isEmpty()) {

      int j = result.size();

      var match = "";
      if ((n & 1) != 0) {
        var lastGenerated = result.get(j - 1);
        match = lastGenerated.get(0);
      }

      for (int i = 0; i < j; i++) {
        var orig = result.get(i);
        // Don't generate reflected copies of a solution whose
        // first row queen is in the central column, as we have already generated them
        if (orig.get(0).equals(match))
          break;
        List<String> soln = new ArrayList<>(n);
        for (var s : orig) {
          sb.setLength(0);
          for (int k = n - 1; k >= 0; k--)
            sb.append(s.charAt(k));
          soln.add(sb.toString());
        }
        result.add(soln);
      }
    }

    return result;
  }
}
