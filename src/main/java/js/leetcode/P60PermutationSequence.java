
package js.leetcode;

import static js.base.Tools.*;

/**
 * I exploit a relationship between the digits of a permutation and the
 * factorial function to perform a series of swaps of the digits 1234...
 */
public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {
    //  x(4,1,"1234");
    x(3, 5, "312");
    x(9, 1, "123456789");
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
    pr("n:", n, "k:", k);
    int[] factorials = new int[n]; // table of factorial(i+1)
    int f = 1;
    for (int i = 1; i <= n; i++) {
        f *= i;
      factorials[i - 1] = f;
    }
    pr("factorials:", factorials);
    var sb = new StringBuilder("123456789".substring(0, n));
    for (int digit = n; digit > 1; digit--) {
      // In calculations, work with k (0..n-1), not (1..n)
      var a = (k - 1) % factorials[digit - 1];
      var b = factorials[digit - 2];
      pr("digit:", digit, "a:", a, "b:", b, "sb:", sb);
      if (a >= b) {
        int i = n - digit;
        int j = i + 1;
        var tmp = sb.charAt(i);
        sb.setCharAt(i, sb.charAt(j));
        sb.setCharAt(j, tmp);
        pr("...after swap", i, j, ":", sb);
      }
    }
    return sb.toString();
  }

}
