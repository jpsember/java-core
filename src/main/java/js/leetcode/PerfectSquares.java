package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class PerfectSquares extends LeetCode {

  public static void main(String[] args) {
    new PerfectSquares().run();
  }

  public void run() {
    x(12, 3);
    x(13, 2);
    for (int n = 1; n <= 10000; n = (int) (n * 1.1 + 1)) {
      pr("n:", n);
      memo.clear();
      x(n, -1);
    }
  }

  private void x(int n, int expected) {
    db = true;
    var result = numSquares(n);
    db("n:", n, result);
    if (expected >= 0)
      verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int numSquares(int n) {
    return aux(n, 100);
  }

  private int aux(int n, int maxRoot) {
    if (n == 0)
      return 0;
    int key = (n << 8) | maxRoot;
    int result = memo.getOrDefault(key, -1);
    checkState(result != -2);
    if (result < 0) {
      db("aux, n:", n, "maxRoot:", maxRoot);
      memo.put(key, -2);
      result = calc(n, maxRoot);
      memo.put(key, result);
      db("storing", n, "|", maxRoot, "=>", result);
    }
    return result;
  }

  private int calc(int n, int maxRoot) {
    int j = maxRoot;

    while (n < (j - 1) * (j - 1)) {
      j--;
    }
    var bestResult = -1;
    for (int k = j; k > 0; k--) {
      var square = k * k;
      var factor = n / square;
      var remainder = n % square;
      var result = factor + aux(remainder, k - 1);
      if (bestResult < 0 || bestResult > result)
        bestResult = result;
    }
    return bestResult;
  }

  private Map<Integer, Integer> memo = new HashMap<>();
}
