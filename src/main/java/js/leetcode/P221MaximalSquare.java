
package js.leetcode;

import static js.base.Tools.*;

import js.base.BasePrinter;

public class P221MaximalSquare {

  public static void main(String[] args) {
    new P221MaximalSquare().run();
  }

  private void run() {
    x(5, 4, "10100 10111 11111 10010", 4);
  }

  private void x(int width, int height, String cells, int expected) {
    cells = cells.replaceAll(" ", "");
    char[][] m = new char[height][];
    int i = 0;
    var sb = new StringBuilder();
    for (int y = 0; y < height; y++) {
      m[y] = new char[width];
      sb.setLength(0);
      for (int x = 0; x < width; x++) {
        m[y][x] = cells.charAt(i++);
      }
    }
    showBoard(m, 0, m[0].length, 0, m.length, "initial");

    var result = maximalSquare(m);
    pr(width, "x", height, ": result:", result);
    checkState(expected == result, "expected:", expected);
  }

  private void showBoard(char[][] sMatrix, int x0, int x1, int y0, int y1, Object... messages) {
    pr(VERT_SP, BasePrinter.toString(messages), "x,y", x0, y1, CR,
        "--------------------------------------------");
    for (var y = y0; y < y1; y++) {
      var sb = new StringBuilder();
      var row = sMatrix[y];
      for (var x = x0; x < x1; x++) {
        var c = row[x];
        if (x != x0)
          sb.append(' ');
        sb.append(c == '0' ? "." : "X");
      }
      pr(sb);
    }
    pr("--------------------------------------------");
  }

  public int maximalSquare(char[][] matrix) {
    final var bw = matrix[0].length;
    final var bh = matrix.length;
    
    int[] sizesTable = new int[bw * bh];

    int bestSizeSeen = 0;
    int xSource = 0;
    int ySource = 0;
    while (true) {
      if (xSource < bw - 1)
        xSource++;
      else if (ySource < bh - 1)
        ySource++;
      else
        break;

      int x = xSource;
      int y = ySource;
      while (true) {
        int best = 0;
        var c = matrix[y][x];
        if (c == '1') {
          best = 1;
          if (x > 0 && y > 0) {
            var j = sizesTable[(y - 1) * bw + (x - 1)];
            for (int i = 1; i <= j; i++) {
              if (matrix[y - i][x] != '1' || matrix[y][x - i] != '1')
                break;
              best++;
            }
          }
          sizesTable[y * bw + x] = best;
          bestSizeSeen = Math.max(bestSizeSeen, best);
        }
        x--;
        y++;
        if (x < 0 || y == bh)
          break;
      }
    }
    return bestSizeSeen * bestSizeSeen;
  }

}
