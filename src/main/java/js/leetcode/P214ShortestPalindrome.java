
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 
 * This looks like a dynamic programming application.
 * 
 * It seems that we are looking for 'embedded' palindromes within the input string s.
 * 
 * An embedded palindrome will be a prefix of s that is a palindrome, e.g.:
 * 
 *    abcbaxyz
 *    ^^^^^
 *    
 * Note that this palindrome is NOT USEFUL for the solution, as it is not a prefix:
 * 
 *    abacabxyz
 *     ^^^^^
 * 
 * </pre>
 *
 * I'm thinking that I won't construct explicit tables for the DP
 * implementation, rather lists of states.
 */
public class P214ShortestPalindrome {

  public static void main(String[] args) {

    new P214ShortestPalindrome().run();
  }

  private void run() {
    x("AH", "HAH");

    x("aacecaaa", "aaacecaaa");
    // x("aacaaa", "aaacaaa");

    x("a", "a");
    x("abcd", "dcbabcd");

    // This string of 50000 identical characters is causing memory problems.
   if (false) {
      int t = 50000;
      var s = "a";
      while (s.length() < t) {
        s = s + s;
      }
      s = s.substring(0, t);
      pr(s.length());
      x(s, s);
    }

  }

  private void x(String s, String expected) {
    var result = shortestPalindrome(s);
    pr(s, "=>", result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String shortestPalindrome(String s) {
    pr(s);

    final var n = s.length();

    // Define a state T(h, x) to mean that a palindrome of length h exists in s starting at character x.

    // We'll store all states for a particular h in its own list, so we can just store the state as x.

    var xUseful = (n - 1) / 2;
    var stackX = new ArrayList<Integer>(n);
    var stackXPlus1 = new ArrayList<Integer>(n);
    var stackXPlus2 = new ArrayList<Integer>(n);
    for (int i = 0; i <= xUseful; i++) {
      stackX.add(i);
      stackXPlus1.add(i);
    }

    int longestPrefixLength = 0;

    int h = 0;
    while (!(stackX.isEmpty() && stackXPlus1.isEmpty())) {

      pr(VERT_SP, quote(s), "h:", h, "stackX:", stackX, "X1:", stackXPlus1, "X2:", stackXPlus2);

      for (var x : stackX) {
        pr("state h:", h, "x:", x, "pal:", quote(s.substring(x, x + h)));
        if (x == 0) {
          if (h > longestPrefixLength) {
            longestPrefixLength = h;
            pr("...new longest prefix:", longestPrefixLength);
          }
        } else {
          int j = x + h;
          if (j < n && s.charAt(x - 1) == s.charAt(j)) {
            pr("....extending:", s.charAt(x - 1), s.substring(x, x + h), s.charAt(j));
            stackXPlus2.add(x - 1);
          }
        }
      }

      var tmp = stackX;
      stackX = stackXPlus1;
      stackXPlus1 = stackXPlus2;
      stackXPlus2 = tmp;
      stackXPlus2.clear();
      pr("new stack X:", stackX, "X1:", stackXPlus1, "stackX2", stackXPlus2);
      h++;
    }

    var sb = new StringBuilder(n * 2);
    for (int j = n - 1; j >= longestPrefixLength; j--)
      sb.append(s.charAt(j));
    sb.append(s);
    return sb.toString();
  }
}
