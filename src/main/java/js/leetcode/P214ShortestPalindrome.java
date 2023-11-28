
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;

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
 * 
 * Reworking the state storage to attempt to deal with very large strings
 * without running out of memory.
 */
public class P214ShortestPalindrome {

  public static void main(String[] args) {

    new P214ShortestPalindrome().run();
  }

  private void run() {
    for (int i = 1; i < 20; i++)
      xRep("a", i);

    x("AH", "HAH");

    x("aacecaaa", "aaacecaaa");

    x("a", "a");
    x("abcd", "dcbabcd");

    xRep("a", 5000);
  }

  private void xRep(String pattern, int repCount) {
    var sb = new StringBuilder(pattern.length() * repCount);
    for (var i = 0; i < repCount; i++)
      sb.append(pattern);
    var s = sb.toString();
    x(s, s);
  }

  private void x(String s, String expected) {
    var result = shortestPalindrome(s);
    if (s.length() < 40)
      pr(s, "=>", result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String shortestPalindrome(String s) {
    final boolean tr = false ; //s.length() < 15;
    if (tr)
      pr(s);

    final var n = s.length();

    // Define a state T(h, x) to mean that a palindrome of length h exists in s starting at character x.

    // We'll store all states for a particular h in its own list, so we can just store the state as x.

    // Determine which values of x might be useful.  x must be <= this value.  Note, special care is
    // taken to make sure this is correct for both even- and odd-length strings.
    var xUseful = n / 2;

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

      if (tr)
        pr(VERT_SP, quote(s), "S" + h + ":", stackX, "S" + (h + 1) + ":", stackXPlus1, "S" + (h + 2) + ":",
            stackXPlus2);

      //      if (!tr && h % 100 == 0)
      //        pr("h:", h);

      for (var x : stackX) {
        if (tr)
          pr("...x:", x, "pal:", quote(s.substring(x, x + h)));
        if (x == 0) {
          if (h > longestPrefixLength) {
            longestPrefixLength = h;
            if (tr)
              pr("...new longest prefix:", longestPrefixLength);
          }
        } else {
          int j = x + h;
          if (j < n && s.charAt(x - 1) == s.charAt(j)) {
            if (tr)
              pr("....extending to h" + (h + 2) + "[", x, "]:", s.charAt(x - 1), s.substring(x, x + h),
                  s.charAt(j));
            stackXPlus2.add(x - 1);
          }
        }
      }

      var tmp = stackX;
      stackX = stackXPlus1;
      stackXPlus1 = stackXPlus2;
      stackXPlus2 = tmp;
      stackXPlus2.clear();
      // pr("new stack X:", stackX, "X1:", stackXPlus1, "stackX2", stackXPlus2);
      h++;
    }

    var sb = new StringBuilder(n * 2);
    for (int j = n - 1; j >= longestPrefixLength; j--)
      sb.append(s.charAt(j));
    sb.append(s);
    return sb.toString();
  }
}
