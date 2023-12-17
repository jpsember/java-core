package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P52NQueensII extends LeetCode {

  public static void main(String[] args) {
    new P52NQueensII().run();
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
    Map<Long, Pattern> prevSizeMap = new HashMap<>();
    Map<Long, Pattern> nextSizeMap = new HashMap<>();

    // There is a single solution for n=0 queens in the n=0 map
    {
      var p = new Pattern(0, 0);
      p.variants = 1;
      p.boards.add(new Board());
      prevSizeMap.put(0L, p);
    }
    int currentVariantTotal = 0;
    for (int m = 1; m <= n; m++) {
      currentVariantTotal = 0;
      nextSizeMap.clear();
      db(VERT_SP, "size", m);

      // Look through all the entries for map from m-1
      for (var ent : prevSizeMap.entrySet()) {
        long used = ent.getKey();
        int queenCount = countQueens(used);
        var pattern = ent.getValue();
        int variants = pattern.variants;
        db(VERT_SP, "...entry, # queens:", queenCount, "variants:", variants);
        db(pattern);

        // Try to place one and two additional queens to get solutions for m-1 and m queens

        // If this is already an m-1 solution, store it
        if (queenCount == m - 1) {
          db("......already an m-1 solution, storing");
          checkState(!nextSizeMap.containsKey(used));
          nextSizeMap.put(used, pattern);
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

            db("...found spot for queen #", queenCountNew, "at:", x, y);

            var pat2 = addPattern(m, nextSizeMap, usedNew, pattern.variants);
            for (var b : pattern.boards) {
              var b2 = b.makeMove(x, y, pat2);
              pat2.boards.add(b2);
            }
            db("...pattern:", INDENT, pat2);

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

                  db("......found slot for queen #" + (queenCountNew + 1), "x:", x, "y:", y, "variants now:",
                      variants);

                  var pat3 = addPattern(m, nextSizeMap, usedNew2, pat2.variants);
                  for (var b : pat2.boards) {
                    var b2 = b.makeMove(x, y, pat3);
                    pat3.boards.add(b2);
                  }
                  db("...pattern:", INDENT, pat3);
                  currentVariantTotal += variants;
                  db(pat3);
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

  private static Pattern addPattern(int boardSize, Map<Long, Pattern> map, long key, int variants) {
    var p = map.get(key);
    if (p == null) {
      p = new Pattern(boardSize, key);
      pr("created new pattern for board size:", boardSize, "key:", key, INDENT, p);
      map.put(key, p);
    }
    p.variants += variants;
    return p;
  }

  private static int countQueens(long n) {
    // We only need to look at the first N bits
    return Integer.bitCount(((int) n) & ((1 << N) - 1));
  }

  /**
   * A pattern is a bit pattern that indicates which rows, columns, and
   * diagonals are 'used'. Multiple boards can share the same pattern.
   */
  private static class Pattern {
    Pattern(int boardSize, long key) {
      this.boardSize = boardSize;
      this.key = key;
    }

    long key;
    int variants;
    // The boards that share this pattern
    List<Board> boards = new ArrayList<>();
    int boardSize;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("Pattern");
      sb.append(" queens:");
      sb.append(countQueens(key));
      sb.append(" variants:");
      sb.append(variants);
      sb.append("# boards:");
      sb.append(boards.size());
      for (var b : boards) {
        sb.append("\n");

        int lastMoveY = -1;

        var cells = new boolean[boardSize][boardSize];
        var b2 = b;
        while (b2.prev != null) {
          if (b2.y >= boardSize || b2.x >= boardSize)
            badState("illegal move for board of this size!");
          if (lastMoveY < 0)
            lastMoveY = b2.y;
          cells[b2.y][b2.x] = true;
          b2 = b2.prev;
        }
        sb.append("/");
        for (int x = 0; x < boardSize; x++)
          sb.append("---");
        sb.append("\\\n");

        for (int y = boardSize - 1; y >= 0; y--) {
          sb.append('|');
          for (int x = 0; x < boardSize; x++) {
            sb.append(cells[y][x] ? (lastMoveY == y ? "(X)" : " X ") : " : ");
          }
          sb.append("|\n");
        }
        sb.append("\\");
        for (int x = 0; x < boardSize; x++)
          sb.append("---");
        sb.append("/\n");

      }
      return sb.toString();
    }

  }

  private static class Board {
    //    Board(Pattern pattern) {
    //      this.pattern = pattern;
    //    }

    int x, y; // Last piece added (if prev != null)
    Board prev; // previous board position
    //    Pattern pattern;

    Board makeMove(int x, int y, Pattern newPattern) {
      var b = new Board();
      b.x = x;
      b.y = y;
      b.prev = this;
      return b;
    }

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
