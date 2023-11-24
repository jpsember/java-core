
package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

public class P76MinimumWindowSubstring {

  public static void main(String[] args) {
    new P76MinimumWindowSubstring().run();
  }

  private void run() {
//    x("ADOBECODEBANC", "ABC");
    x("a","aa");
  }

  private void x(String s, String t) {
    pr("s:", s, "t:", t, INDENT, minWindow(s, t));
  }

  public String minWindow(String s, String t) {

    // Set frequency table to -n for each character that appears in t n times.
    // For characters that don't appear in t, set it to FREQ_UNUSED+x so it doesn't affect the logic.

    int[] freq = new int['z' - 'A' + 1];
    final int FREQ_NOTUSED = 1000000;
    Arrays.fill(freq, FREQ_NOTUSED);
    int uniqueCount = 0;
    for (var i = 0; i < t.length(); i++) {
      int j = t.charAt(i) - 'A';
      if (freq[j] == FREQ_NOTUSED) {
        freq[j] = 0;
        uniqueCount++;
      }
      freq[j]--;
    }

    int a = 0;
    int b = 0;
    int satisfyCount = -uniqueCount;
    String bestAnswer = "";

    while (true) {
       pr("a:", a, "b:", b, s.substring(a, b), "sat:", satisfyCount);
      if (satisfyCount < 0) {
        if (b == s.length())
          break;
        char cb = s.charAt(b);
        b++;

        int i = cb - 'A';
        if (++freq[i] == 0) {
          satisfyCount++;

        }
      } else {
        char ca = s.charAt(a);
        a++;
        int i = ca - 'A';
        if (freq[i]-- == 0) {
          satisfyCount--;
        }
      }
      if (satisfyCount >= 0) {
        if (bestAnswer.isEmpty() || bestAnswer.length() > b - a) {
          bestAnswer = s.substring(a, b);
           pr(INDENT, "new best answer:", bestAnswer);
        }
      }
    }
    return bestAnswer;
  }
}