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
    int n = 8;
    var x = solveNQueens(n);
    pr("--------------------------------");
    for (var y : x) {
      for (var z : y) {
        pr(z);
      }
      pr("--------------------------------");
    }
  }

  private static final int MAX_BOARD_SIZE = 9;

  private static final int DIAG_1 = 1 << 0;
  private static int DIAG_2 = 1 << 1;
  private static final int COL = 1 << 2;
  private static final int ROW = 1 << (2 + MAX_BOARD_SIZE);
  private static final int BITS_TOTAL = 2 + MAX_BOARD_SIZE * 2;

  private static class State {
    short[] startRowStates;
    byte[] queenSlots;
    int activeColumn;
    int activeRow;

    State(int boardSize) {
      queenSlots = new byte[boardSize];
      startRowStates = new short[boardSize];
    }
  }

  private int boardSize;
  private List<List<String>> result;
  private short[] squareFlags;

  // private State[] stack;
  StringBuilder sb = new StringBuilder();

  private void init(int n) {
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
  }

  private void generateResult(State state) {
    List<String> soln = new ArrayList<>();
    for (int y = 0; y < boardSize; y++) {
      sb.setLength(0);
      for (int x = 0; x < boardSize; x++) {
        if (x == state.queenSlots[y])
          sb.append('Q');
        else
          sb.append('.');
      }
      soln.add(sb.toString());
    }
    result.add(soln);
  }

  public List<List<String>> solveNQueens(int n) {

    init(n);

    State s = new State(n);

    while (true) {
      {
        pr(VERT_SP);
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------\n");
        for (int y = 0; y <= s.activeRow; y++) {
          sb.append(y);
          sb.append(": ");
          for (int x = 0; x <= n; x++) {
            if (y == s.activeRow && x == s.activeColumn)
              sb.append('>');
            else
              sb.append(' ');
            char c = ' ';
            if (x < n) {
              if (y < s.activeRow && x == s.queenSlots[y])
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
      if (s.activeColumn == n) {
        pr("...current row exhausted");
        // Is this the first row?  If so, done.
        if (s.activeRow == 0) {
          pr("...row zero, done");
          break;
        }
        s.activeRow--;
        pr("...backtracking to row:", s.activeRow);
        // Advance to the next column
        s.activeColumn = s.queenSlots[s.activeRow] + 1;
        pr("...advanced to column", s.activeColumn);
        continue;
      }

      int rowState = s.startRowStates[s.activeRow];
      int squareIndex = s.activeRow * n + s.activeColumn;
      int squareFlg = squareFlags[squareIndex];

      pr("rowstate:", bitString(rowState, BITS_TOTAL), "squareFlg:", bitString(rowState, BITS_TOTAL));
      // Is this an invalid move?
      if ((rowState & squareFlg) != 0) {
        pr("...invalid move; incrementing column");
        s.activeColumn++;
        continue;
      }
      pr("valid move, placing queen, moving to next row");

      s.queenSlots[s.activeRow] = (byte) s.activeColumn;
      s.activeRow++;
      s.activeColumn = 0;

      if (s.activeRow < n)
        s.startRowStates[s.activeRow] = (short) (rowState | squareFlg);
      else {
        pr("...generating result");
        // If we finished all rows, it's a result
        generateResult(s);

        // backtrack and increment column 
        pr("backtracking, incrementing column");
        s.activeRow--;
        s.activeColumn = s.queenSlots[s.activeRow] + 1;
      }

    }
    return result;
  }
}
