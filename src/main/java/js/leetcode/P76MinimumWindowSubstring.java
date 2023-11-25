
package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

public class P76MinimumWindowSubstring {

  public static void main(String[] args) {
    new P76MinimumWindowSubstring().run();
  }

  private void run() {
         x("ADOBECODEBANC", "ABC");
    
    //x("a", "aa");
  }

  private void x(String s, String t) {
    pr("s:", s, "t:", t, INDENT, minWindow(s, t));
  }

  public String minWindow(String s, String t) {

    // Convert string S to an array of bytes, where 0 is the first valid character (A)
    final int offset = 'A';

    final byte[] sCodes = new byte[s.length()];
    for (var i = 0; i < s.length(); i++) {
      sCodes[i] = (byte) (s.charAt(i) - offset);
    }

    // Set frequency table to -n for each character that appears in t n times,
    // and set uniqueCount to the number of such (unique) characters.
    //
    // For characters that don't appear in t, set it to FREQ_UNUSED+x so it doesn't affect the logic.
    int[] freq = new int['z' - 'A' + 1];
    final int FREQ_NOTUSED = 1000000;
    Arrays.fill(freq, FREQ_NOTUSED);
    int uniqueCount = 0;
    for (var i = 0; i < t.length(); i++) {
      int j = t.charAt(i) - offset;
      if (freq[j] == FREQ_NOTUSED) {
        freq[j] = 0;
        uniqueCount++;
      }
      freq[j]--;
    }

    int a = 0;
    int b = 0;
    int satisfyCount = -uniqueCount;
    int aBest = -s.length();
    int bBest = 0;

    while (true) {
      pr("a:", a, "b:", b, s.substring(a, b), "sat:", satisfyCount);
      if (satisfyCount < 0) {
        // If we can't extend the tail, we're done.
        if (b == sCodes.length)
          break;
        // If frequency of character appearing at the tail b increases to the satisfy level (0),
        // increment the satisfy count.
        if (++freq[sCodes[b++]] == 0)
          satisfyCount++;
      } else {
        // If frequency of character dropping off the head at a decreases to below the satisfy level (0),
        // decrement the satisfy count.
        if (freq[sCodes[a++]]-- == 0)
          satisfyCount--;
      }

      // If still satisfied after the above action,
      // check if we have a new best result.
      if (satisfyCount >= 0) {
        if (bBest - aBest > b - a) {
          aBest = a;
          bBest = b;
          //pr(INDENT, "new best answer:", s.substring(aBest, bBest));
        }
      }
    }
    if (aBest < 0)
      aBest = 0;
    return s.substring(aBest, bBest);
  }
}