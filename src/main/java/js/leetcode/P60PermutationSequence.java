
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

  private long[] factorials;

  private long fact(int n) {
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

  public String getPermutation(int n, int k) {

    int[] ordered = new int[n];
    for (int i = 0; i < n; i++)
      ordered[i] = i + 1;

    pr(VERT_SP, "n:", n, "k:", k);

    k--;
    for (int digit = n; digit > 1; digit--) {
      var a = k % fact(digit);
      var b = fact(digit - 1);
      pr("digit:", digit, "fact:", fact(digit), " fact-1:", fact(digit - 1), "a:", a, "b:", b);
      if (a >= b) {
        swap(ordered, n - digit, n - digit + 1);
        pr("...after swap:",ordered);
      }
    }


    var sb = new StringBuilder();
    for (int digit = 0; digit < n; digit++)
      sb.append((char) ('0' + ordered[digit]));
    return sb.toString();

    //    long kwork = k;
    //
    //    var sb = new StringBuilder();
    //    for (int position = n; position > 0; position--) {
    //      var f = factorials[position - 1];
    //      int digit = (int) ((kwork - 1) / f + 1);
    //      pr("position:", position, "digit:", digit, "kw:", kwork, "f:", f, "sb:", sb, "ordered:", ordered);
    //
    //      sb.append((char) (ordered[digit] + '0'));
    //
    //      swap(ordered, n - position, digit);
    //
    //      pr("...after swap:", ordered);
    //
    //      kwork = kwork % f;
    //      pr("...new k:", kwork);
    //    }
    //    return sb.toString();
  }

  private static void swap(int[] array, int a, int b) {
    int tmp = array[a];
    array[a] = array[b];
    array[b] = tmp;
  }
  /**
   * <pre>
  1 2 3 4
  1 2 4 3
  1 3 2 4
  1 3 4 2
  1 4 3 2
  1 4 2 3
  2 1 3 4
  2 1 4 3
  2 3 1 4
  2 3 4 1
  2 4 3 1
  2 4 1 3
  3 2 1 4
  3 2 4 1
  3 1 2 4
  3 1 4 2
  3 4 1 2
  3 4 2 1
  4 2 3 1
  4 2 1 3
  4 3 2 1
  4 3 1 2
  4 1 3 2
  4 1 2 3
   * </pre>
   */
}
