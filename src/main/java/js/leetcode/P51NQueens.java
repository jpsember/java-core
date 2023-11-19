package js.leetcode;

import static js.base.Tools.*;
import static js.data.DataUtil.*;

import java.util.ArrayList;
import java.util.List;

// 51. N-Queens
//

public class P51NQueens {

  public static void main(String[] args) {
    new P51NQueens().run();
  }

  private void run() {
    int[] expected = { 0, 1, 0, 4, 12, 68, 92, -1, 21072, -1, -1 };
    int n = 4;
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
      exp = expected[n];
    if (exp != x.size()) {
      pr("****** Unexpected number of solutions:", x.size(), "expected:", exp);
    }
  }

  private static final int MAX_BOARD_SIZE = 9;

  private static final int DIAG_1 = 1 << 0;
  private static int DIAG_2 = 1 << 1;
  private static final int COL = 1 << 2;
  private static final int ROW = 1 << (2 + MAX_BOARD_SIZE);
  private static final int BITS_TOTAL = 2 + MAX_BOARD_SIZE * 2;

  //  private void init(int n) {
  //    boardSize = n;
  //    result = new ArrayList<>();
  //
  //    int bs = n * n;
  //    squareFlags = new short[bs];
  //    {
  //      int i = 0;
  //      for (int y = 0; y < n; y++) {
  //        for (int x = 0; x < n; x++) {
  //          int flag = (ROW << y) | (COL << x);
  //          if (x == y)
  //            flag |= DIAG_1;
  //          if (x == n - 1 - y)
  //            flag |= DIAG_2;
  //          squareFlags[i++] = (short) flag;
  //        }
  //      }
  //    }
  //  }
  //   
  public List<List<String>> solveNQueens(int n) {

    boolean log = true;

    int boardSize;
    List<List<String>> result;
    short[] squareFlags;

    StringBuilder sb = new StringBuilder();

    boardSize = n;
    result = new ArrayList<>();

    int bs = n * n;
    squareFlags = new short[bs];
    {
      int i = 0;
      for (int y = 0; y < n; y++) {
        for (int x = 0; x < n; x++) {
          int flag = (ROW << y) | (COL << x);
          if (x == y)
            flag |= DIAG_1;
          if (x == n - 1 - y)
            flag |= DIAG_2;
          squareFlags[i++] = (short) flag;
        }
      }
    }

    byte[] queenSlots = new byte[boardSize];
    int activeColumn = 0;
    int activeRow = 0;
    short[] startRowStates = new short[boardSize];

    while (true) {
      if (log) {
        pr(VERT_SP);
        sb.setLength(0);
        sb.append("---------------------------------\n");
        for (int y = 0; y <= activeRow; y++) {
          sb.append(y);
          sb.append(": ");
          for (int x = 0; x <= n; x++) {
            if (y == activeRow && x == activeColumn)
              sb.append('>');
            else
              sb.append(' ');
            char c = ' ';
            if (x < n) {
              if (y < activeRow && x == queenSlots[y])
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
      if (activeColumn == n) {
        if (log)
          pr("...current row exhausted");
        // Is this the first row?  If so, done.
        if (activeRow == 0) {
          if (log)
            pr("...row zero, done");
          break;
        }
        activeRow--;
        if (log)
          pr("...backtracking to row:", activeRow);
        // Advance to the next column
        activeColumn = queenSlots[activeRow] + 1;
        if (log)
          pr("...advanced to column", activeColumn);
        continue;
      }

      int rowState = startRowStates[activeRow];
      int squareIndex = activeRow * n + activeColumn;
      int squareFlg = squareFlags[squareIndex];

      if (log) {
        pr("rowstate :", bitString(BITS_TOTAL, rowState));
        pr("squareFlg:", bitString(BITS_TOTAL, squareFlg));
        pr("common   :", bitString(BITS_TOTAL, rowState & squareFlg));
      }

      // Is this an invalid move?
      if ((rowState & squareFlg) != 0) {
        if (log)
          pr("...invalid move; incrementing column");
        activeColumn++;
        continue;
      }
      if (log)
        pr("valid move, placing queen, moving to next row");

      queenSlots[activeRow] = (byte) activeColumn;
      activeRow++;
      activeColumn = 0;

      if (activeRow < n)
        startRowStates[activeRow] = (short) (rowState | squareFlg);
      else {
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
        activeRow--;
        activeColumn = queenSlots[activeRow] + 1;
      }

    }
    return result;
  }
}
