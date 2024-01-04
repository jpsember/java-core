package js.leetcode;

import static js.base.Tools.*;

/**
 * First attempt seemed to work, but failed on some inputs.
 * 
 * I wasn't accounting for the number of islands exceeding the size of a byte.
 * 
 * Now I realize I don't need to store the island number, rather just a zero.
 */
public class NumberOfIslands extends LeetCode {

  public static void main(String[] args) {
    new NumberOfIslands().run();
  }

  public void run() {
    x("[\"1\",\"1\",\"1\",\"1\",\"0\"],\n" + "  [\"1\",\"1\",\"0\",\"1\",\"0\"],\n"
        + "  [\"1\",\"1\",\"0\",\"0\",\"0\"],\n" + "  [\"0\",\"0\",\"0\",\"0\",\"0\"]", 5, 1);
    x("[\"1\",\"1\",\"0\",\"0\",\"0\"],\n" + "  [\"1\",\"1\",\"0\",\"0\",\"0\"],\n"
        + "  [\"0\",\"0\",\"1\",\"0\",\"0\"],\n" + "  [\"0\",\"0\",\"0\",\"1\",\"1\"]", 5, 3);
  }

  private void x(String s, int width, int expected) {
    var m = extractMatrix(s, width);
    var c = new char[m.length][width];
    for (int y = 0; y < m.length; y++) {
      var r = c[y];
      for (int x = 0; x < width; x++)
        r[x] = (char) ('0' + m[y][x]);
    }
    var res = numIslands(c);
    pr("got count:", res);
    verify(res, expected);
  }

  public int numIslands(char[][] grid) {
    int newWidth = grid[0].length;
    int newHeight = grid.length;

    var stack = new short[newWidth * newHeight * 4];
    int islandCount = 0;
    for (int y = 0; y < newHeight; y++) {
      for (int x = 0; x < newWidth; x++) {
        if (grid[y][x] == '1') {
          int sp = 0;
          stack[sp++] = (short) x;
          stack[sp++] = (short) y;
          islandCount++;
          while (sp != 0) {
            int sy = stack[sp - 1];
            int sx = stack[sp - 2];
            sp -= 2;
            if (grid[sy][sx] != '1')
              continue;
            grid[sy][sx] = '0';
            if (sx + 1 < newWidth) {
              stack[sp++] = (short) (sx + 1);
              stack[sp++] = (short) sy;
            }
            if (sx > 0) {
              stack[sp++] = (short) (sx - 1);
              stack[sp++] = (short) sy;
            }
            if (sy > 0) {
              stack[sp++] = (short) sx;
              stack[sp++] = (short) (sy - 1);
            }
            if (sy + 1 < newHeight) {
              stack[sp++] = (short) sx;
              stack[sp++] = (short) (sy + 1);
            }
          }
        }
      }
    }

    return islandCount;
  }
}
