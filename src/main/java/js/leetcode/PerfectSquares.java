package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Beats 15% runtime
 */
public class PerfectSquares extends LeetCode {

  public static void main(String[] args) {
    new PerfectSquares().run();
  }

  public void run() {
    //    if (true) {
    //      x(166, 3);
    //      return;
    //    }

    //    x(12, 3);
    //    x(13, 2);

    checkpoint("starting");
    for (int n = 1; n <= 10000; n = (int) (n * 1.01 + 1)) {
      x(n, -1);
    }
    checkpoint("stopping");
  }

  private void x(int n, int expected) {
    db = false;
    var result = numSquares(n);
    if (expected >= 0)
      verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int numSquares(int n) {

    int squares[] = new int[100];
    for (int i = 0; i < 100; i++)
      squares[i] = (i + 1) * (i + 1);

    int table[] = new int[n + 1];
    int denom[] = new int[n + 1];

    table[0] = 0;
    for (int p = 1; p <= n; p++) {
      var minNumCoins = Integer.MAX_VALUE;
      int minCoinDenom = -1;
      for (var coinDenom : squares) {
        // Stop scanning coins if coin is worth more than sum we are making change for
        if (p < coinDenom)
          break;
        var remainder = p - coinDenom;
        var countForRemainder = table[remainder];
        if (1 + countForRemainder < minNumCoins) {
          minNumCoins = 1 + countForRemainder;
          minCoinDenom = coinDenom;
        }
      }
      table[p] = minNumCoins;
      denom[p] = minCoinDenom;
    }
    return table[n];
  }

}
