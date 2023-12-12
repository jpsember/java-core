package js.leetcode;

import static js.base.Tools.*;

import java.util.BitSet;

public class P204CountPrimes extends LeetCode {

  public static void main(String[] args) {
    new P204CountPrimes().run();
  }

  public void run() {
    x(2, 0);
    x(10, 4);
    x(499979, 41537);
  }

  private void x(int n, int expected) {
    var y = countPrimes(n);
    pr("n:", n, y);
    verify(y, expected);
  }

  public int countPrimes0(int n) {
    int primeCount = 0;
    var bs = new BitSet(n + 1);

    for (int i = 2; i < n; i++) {
      if (!bs.get(i)) {
        primeCount++;
        var j = i + i;
        while (j < n) {
          bs.set(j);
          j += i;
        }
      }
    }
    return primeCount;
  }

  public int countPrimes(int n) {
    int primeCount = 0;
    byte[] flags = new byte[n];
    int limit = (int) Math.sqrt(Integer.MAX_VALUE);
    for (int i = 2; i < n; i++) {
      if (flags[i] == 0) {
        primeCount++;
        var j = (i >= limit ? i + i : i * i);
        while (j < n) {
          flags[j] = 1;
          j += i;
        }
      }
    }
    return primeCount;
  }
}
