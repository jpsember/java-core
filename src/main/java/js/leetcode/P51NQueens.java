package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

// 51. N-Queens
//

public class P51NQueens {

  public static void main(String[] args) {
    new P51NQueens().run();
  }

  private void run() {
    int[] expected = { 1, 0, 4, 2, 68, 92, -1, 92, -1 };
    int n = 8;
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
    if (expected.length > n)
      exp = expected[n - 1];
    if (exp != x.size()) {
      pr("****** Unexpected number of solutions:", x.size(), "expected:", exp);
    }
  }

  public List<List<String>> solveNQueens(int n) {

    boolean log = true;

    int boardSize;
    List<List<String>> result;

    StringBuilder sb = new StringBuilder();

    boardSize = n;
    result = new ArrayList<>();

    // Active coordinates
    int bx = 0;
    int by = 0;

    // Queens placed in rows above by
    int[] queenSlots = new int[boardSize];

    while (true) {
      if (log) {
        pr(VERT_SP);
        sb.setLength(0);
        sb.append("---------------------------------\n");
        for (int y = 0; y <= by; y++) {
          sb.append(y);
          sb.append(": ");
          for (int x = 0; x <= n; x++) {
            if (y == by && x == bx)
              sb.append('>');
            else
              sb.append(' ');
            char c = ' ';
            if (x < n) {
              if (y < by && x == queenSlots[y])
                c = 'Q';
              else
                c = '.';
            }
            sb.append(c);
          }
          sb.append('\n');
        }
        sb.append("---------------------------------\n");
        pr(sb);
      }

      // Is the current row exhausted?
      if (bx == n) {
        if (log)
          pr("...current row exhausted");
        // Is this the first row?  If so, done.
        if (by == 0) {
          if (log)
            pr("...row zero, done");
          break;
        }
        by--;
        if (log)
          pr("...backtracking to row:", by);
        // Advance to the next column
        bx = queenSlots[by] + 1;
        if (log)
          pr("...advanced to column", bx);
        continue;
      }

      // Is this an invalid move?
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
        if (log)
          pr("...invalid move; incrementing column");
        bx++;
        continue;
      }
      if (log)
        pr("valid move, placing queen, moving to next row");

      queenSlots[by] = (byte) bx;
      by++;
      bx = 0;

      if (by == n) {
        if (log)
          pr("...generating result");

        // If we finished all rows, it's a result
        List<String> soln = new ArrayList<>();
        for (int y = 0; y < boardSize; y++) {
          sb.setLength(0);
          for (int x = 0; x < boardSize; x++) {
            if (x == queenSlots[y])
              sb.append('Q');
            else
              sb.append('.');
          }
          soln.add(sb.toString());
        }
        result.add(soln);

        // backtrack and increment column 
        if (log)
          pr("backtracking, incrementing column");
        by--;
        bx = queenSlots[by] + 1;
      }
    }
    return result;
  }
}
