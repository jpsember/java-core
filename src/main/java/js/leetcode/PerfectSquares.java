package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Beats 5% runtime
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

    x(12, 3);
    x(13, 2);

    checkpoint("starting");
    for (int n = 1; n <= 10000; n = (int) (n * 1.01 + 1)) {
      x(n, -1);
    }
    checkpoint("stopping");
  }

  private void x(int n, int expected) {
    memo.clear();

    db = false;
    var result = numSquares(n);
    db("n:", n, result);
    if (expected >= 0)
      verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int numSquares(int n) {
    var result = aux(n, 100);
    return result;
  }

  private int aux(int n, int maxRoot) {
    if (n == 0)
      return 0;
    int key = (n << 8) | maxRoot;
    int result = memo.getOrDefault(key, -1);
    if (result < 0) {
      db("aux, n:", n, "maxRoot:", maxRoot);
      result = calc(n, maxRoot);
      memo.put(key, result);
      db("storing", n, "|", maxRoot, "=>", result);
    }
    return result;
  }

  private int calc(int n, int maxRoot) {
    // Find largest j such that j*j <= n
    int j = (int) Math.sqrt(n);

    // Examine all solutions made with squares not exceeding j, j-1, j-2, ..., 
    // since the best solution is not necessarily j.
    //
    // Stop when n / square exceeds the best result so far 
    //
    var bestResult = Integer.MAX_VALUE;
    for (int k = j; k > 0; k--) {
      var square = k * k;
      var factor = n / square;
      // If this first term coefficient exceeds the best sum we found so far, stop
      if (factor > bestResult) {
        break;
      }
      var remainder = n % square;
      var result = factor + aux(remainder, k - 1);
      if (result < bestResult) {
        bestResult = result;
      }
    }
    return bestResult;
  }

  private Map<Integer, Integer> memo = new HashMap<>();
}
