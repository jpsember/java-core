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
    int origWidth = grid[0].length;
    int origHeight = grid.length;

    // Construct a padded grid of bytes, with a one byte padded region of water.
    // We omit vertical padding on the right, as the padding on the left will serve.
    // 0 for water, 1 for (unlabelled) island, 2 for labelled island.
    int newWidth = origWidth + 1;
    int newHeight = origHeight + 2;
    int numCells = newWidth * newHeight;
    byte[] cells = new byte[numCells];
    int origin = newWidth + 1;
    int i = origin;
    for (var row : grid) {
      for (var c : row) {
        if (c != '0')
          cells[i] = 1;
        i++;
      }
      i += newWidth - origWidth;
    }

    int[] stack = new int[numCells];
    int islandCount = 0;
    for (i = origin; i < numCells; i++) {
      if (cells[i] == 1) {
        int sp = 0;
        stack[sp++] = i;
        islandCount++;
        while (sp != 0) {
          int j = stack[--sp];
          if (cells[j] != 1)
            continue;
          cells[j] = 0;
          stack[sp] = j + 1;
          stack[sp + 1] = j - 1;
          stack[sp + 2] = j + newWidth;
          stack[sp + 3] = j - newWidth;
          sp += 4;
        }
      }
    }
    return islandCount;
  }
}
