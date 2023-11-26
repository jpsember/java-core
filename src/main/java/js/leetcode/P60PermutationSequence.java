
package js.leetcode;

import static js.base.Tools.*;

public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {
    //  x(4,1,"1234");
    x(4, 1, "1234");

    x(3, 3, "213");
    x(4, 9, "2314");
    x(3, 1, "123");

  }

  private void x(int n, int k, String expected) {
    String result = getPermutation(n, k);
    pr(VERT_SP, "n:", n, "k:", k, result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String getPermutation(int n, int k) {
    var sb = new StringBuilder("123456789".substring(0, n));
    for (int digit = n; digit > 1; digit--) {
      // In calculations, work with k (0..n-1), not (1..n)
      var a = (k - 1) % fact(digit);
      var b = fact(digit - 1);
      if (a >= b) {
        int i = n - digit;
        int j = i + 1;
        var tmp = sb.charAt(i);
        sb.setCharAt(i, sb.charAt(j));
        sb.setCharAt(j, tmp);
      }
    }
    return sb.toString();
  }

  private static long fact(int n) {
    if (factorials == null) {
      final int maxN = 9;
      factorials = new long[maxN + 1];
      {
        long f = 1;
        for (int i = 0; i <= maxN; i++) {
          if (i > 0)
            f *= i;
          factorials[i] = f;
        }
      }
    }
    return factorials[n];
  }

  private static long[] factorials;

}
