
package js.leetcode;

import static js.base.Tools.*;

/**
 * My initial intuition was wrong... this will need more work
 */
public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {

    x(4, 1, "1234");

    //    1 2 3
    //    1 3 2
    //    2 1 3
    //    2 3 1
    //    3 2 1
    //    3 1 2

    x(3, 5, "312");
    //    x(9, 1, "123456789");
    //    x(4, 1, "1234");
    //
    //    x(3, 3, "213");
    //    x(4, 9, "2314");
    //    x(3, 1, "123");

  }

  private void x(int n, int k, String expected) {
    String result = getPermutation(n, k);
    pr(VERT_SP, "n:", n, "k:", k, result);
    checkState(result.equals(expected), "expected:", expected);
  }

  private static void swap(StringBuilder sb, int i, int j) {
    pr("swap:", i, j);
    char c = sb.charAt(i);
    sb.setCharAt(i, sb.charAt(j));
    sb.setCharAt(j, c);
  }

  public String getPermutation(int n, int k) {
    pr("getPerm, n:", n, "k:", k);
    var sb = new StringBuilder("123456789".substring(0, n));

    for (int digit =  0; digit <n; digit++) {
      int w = (k / fact(n-digit-1)) + 1;
      pr("w:", w);

      swap(sb, digit, w-1);
      pr("sb now:", sb);
      k = k % fact(n - 1 - digit);
      pr("k:", k);
    }

    return sb.toString();
  }

  private static int[] sFact;

  private static int fact(int n) {
    if (sFact == null) {
      final int nMax = 9;
      sFact = new int[nMax]; // table of factorial(i-1)
      int f = 1;
      for (int i = 1; i <= nMax; i++) {
        f *= i;
        sFact[i - 1] = f;
      }
    }
    return sFact[n - 1];
  }

}
