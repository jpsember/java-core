package js.leetcode;

import static js.base.Tools.*;

// 50. Rotate Image
//
// This was fairly straightforward, not very interesting.

public class P50RotateImage {

  public static void main(String[] args) {
    new P50RotateImage().run();
  }

  private void run() {
    x(3);
  }

  private void show(int[][] m) {
    pr(VERT_SP);
    for (int[] a : m) {
      pr(a);
    }
  }

  private void x(int size) {
    int m[][];
    m = new int[size][];
    for (int i = 0; i < size; i++)
      m[i] = new int[size];
    int j = 10;
    for (int i = 0; i < size * size; i++) {
      m[i / size][i % size] = j++;
    }
    show(m);

    rotate(m);
    show(m);
  }

  public void rotate(int[][] m) {
    int s = m.length;
    int sn = s - 1;

    for (int r = 0; r <= s / 2; r++) {
      int re = sn - r;

      for (int t = r; t < re; t++) {
        int x0 = t;
        int y0 = r;

        int x1 = re;
        int y1 = x0;

        int x2 = sn - t;
        int y2 = x1;

        int x3 = y0;
        int y3 = x2;

        int save = m[y3][x3];
        m[y3][x3] = m[y2][x2];
        m[y2][x2] = m[y1][x1];
        m[y1][x1] = m[y0][x0];
        m[y0][x0] = save;
      }
    }
  }
}
