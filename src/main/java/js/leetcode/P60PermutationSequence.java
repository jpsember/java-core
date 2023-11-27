
package js.leetcode;

import static js.base.Tools.*;

/**
 * My initial intuition was wrong... this will need more work.
 * 
 * The 'permutation sequence' depends upon the numerical value of the symbols
 * being permuted, which is something that probably prevents some simple
 * symmetry arguments.
 * 
 * I've been misled by an assumption that there is a relationship between n and
 * log k.
 * 
 * Final answer: determine which character needs to be moved to the front of the 
 * sequence S, then recursively process the suffix S[2,...] with n-1.
 */
public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {
    x(4, 1, "1234");
    x(3, 5, "312");
    x(3, 3, "213");
    x(4, 9, "2314");
    x(3, 1, "123");
  }

  /* private */void x(int n, int k, String expected) {
    String result = getPermutation(n, k);
    pr(VERT_SP, "n:", n, "k:", k, result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String getPermutation(int n, int k) {

    byte[] items = new byte[n];
    for (int i = 0; i < n; i++) {
      items[i] = (byte) ('1' + i);
    }

    var sequenceIndex = k - 1;
    for (int permutationDigit = 0; permutationDigit < n - 1; permutationDigit++) {
      // Determine which of the existing items should be moved to the current digit's position,
      // shifting others forward
      var divisor = sfact[n - 1 - permutationDigit];
      int slot = permutationDigit + sequenceIndex / divisor;
      var movedValue = items[slot];
      while (slot > permutationDigit) {
        items[slot] = items[slot - 1];
        slot--;
      }
      items[slot] = movedValue;
      sequenceIndex = sequenceIndex % divisor;
    }
    return new String(items);
  }

  private static int[] sfact = { 1, 1, 2, 6, 24, 120, 720, 720 * 7, 720 * 7 * 8 };

}
