package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NQueensII extends LeetCode {

  public static void main(String[] args) {
    new NQueensII().run();
  }

  public void run() {
    x(2,0);
//    x(4, 2);
  }

  private void x(int n, int exp) {
    var c = totalNQueens(n);
    pr("n:", n, "c:", c);
    verify(c, n);
  }

  public int totalNQueens(int n) {
    final int N = 10;
    var squareFlags = new long[N][N];
    for (int y = 0; y < N; y++) {
      pr(VERT_SP);
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
        pr("y:", y, "x:", x, "f:", bitStr(row[x] | (1L << 62)));
      }
    }
    Map<Long, Integer> countMap = new HashMap<>();
    countMap.put(0L, 1);
    Map<Long, Integer> countMap2 = new HashMap<>();
    
    List<Long> newBoardKeys = new ArrayList<>();
    List<Integer> newBoardValues = new ArrayList<>();

    int sum = 0;
    for (int size = 1; size <= n; size++) {
      newBoardKeys.clear();
      newBoardValues.clear();

      // Look through all the entries from the previous 
      sum = 0;
      for (var ent : countMap.entrySet()) {
        long used = ent.getKey();
        int count = ent.getValue();

        pr("rowFlags:", bitStr(used), "count:", count);
        {
          var y = size - 1;
          for (int x = 0; x < size; x++) {
            var sf = squareFlags[y][x];
            pr("x:", x, "y:", y, "f:", bitStr(sf));
            if ((sf & used) == 0) {
              newBoardKeys.add(sf | used);
              newBoardValues.add(count);
              sum += count;
            }
          }
        }
        {
          var x = size - 1;
          for (int y = 1; y < size; y++) {
            var sf = squareFlags[y][x];
            pr("x:", x, "y:", y, "f:", bitStr(sf));
            if ((sf & used) == 0) {
              newBoardKeys.add(sf | used);
              newBoardValues.add(count);
              sum += count;
            }
          }
        }
      }

      for (int i = 0; i < newBoardKeys.size(); i++)
        countMap.put(newBoardKeys.get(i), newBoardValues.get(i));
      db("total for n=", size, "is", sum);

    }
    return sum;
  }

}
