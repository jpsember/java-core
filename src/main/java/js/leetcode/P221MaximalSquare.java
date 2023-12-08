
package js.leetcode;

import static js.base.Tools.*;

import js.base.BasePrinter;

public class P221MaximalSquare {

  public static void main(String[] args) {
    new P221MaximalSquare().run();
  }

  private void run() {

    x(5, 6, "01101 11010 01110 11110 11111 00000", 9);
    x(4, 5, "00011101111101110111", 9);
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
    // The 'size' of a square is its width (or length)
    final var bw = matrix[0].length;
    final var bh = matrix.length;
    int bestSizeSeen = 0;
    int maxPossible = bw < bh ? bw : bh;

    outer: for (int x0 = 0; x0 < bw + bh - 1; x0++) {
      var x = x0;
      var y = 0;
      if (x0 >= bw) {
        y = x0 - bw + 1;
        x = bw - 1;
      }
      while (x >= 0 && y < bh) {
        int best = 0;
        var c = matrix[y][x];
        if (c == '1') {
          best = 1;
          if (x > 0 && y > 0) {
            int nbr1 = matrix[y - 1][x];
            int nbr2 = matrix[y][x - 1];
            best = nbr1 < nbr2 ? nbr1 : nbr2;
            if (matrix[y - best][x - best] != 0)
              best++;
          }
          bestSizeSeen = best > bestSizeSeen ? best : bestSizeSeen;
          if (bestSizeSeen == maxPossible)
            break outer;
        }
        matrix[y][x] = (char) best;
        x--;
        y++;
      }
    }
    return bestSizeSeen * bestSizeSeen;
  }

}
