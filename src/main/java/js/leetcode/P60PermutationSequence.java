
package js.leetcode;

import static js.base.Tools.*;

public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {
    x(3, 3, "214");
    x(4, 9, "2314");
    x(3, 1, "123");

  }

  private void x(int n, int k, String expected) {
    String result = getPermutation(n, k);
    pr("n:", n, "k:", k, result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String getPermutation(int n, int k) {
    return "1";
  }
}
