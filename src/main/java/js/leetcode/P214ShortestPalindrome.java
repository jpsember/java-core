
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
 * 
 * Reworking the state storage to attempt to deal with very large strings
 * without running out of memory.
 * 
 * Now running out of time with large inputs.
 * 
 * I think I need a different algorithm...
 * 
 */
public class P214ShortestPalindrome {

  public static void main(String[] args) {

    new P214ShortestPalindrome().run();
  }

  private void run() {

    for (int i = 1; i < 20; i++)
      xRep("a", i);
    checkpoint("before big");
    xRep("a", 42000);
    checkpoint("after big"); // 6.749; 7.608 after refactor to use state objects

    x("");
    x("AH");

    x("aacecaaa");

    x("a");
    x("abcd");

  }

  private void xRep(String pattern, int repCount) {
    var sb = new StringBuilder(pattern.length() * repCount);
    for (var i = 0; i < repCount; i++)
      sb.append(pattern);
    var s = sb.toString();
    x(s, s);
  }

  private void x(String s) {
    x(s, SLOWshortestPalindrome(s));
  }

  private void x(String s, String expected) {
    var result = shortestPalindrome(s);
    if (s.length() < 40)
      pr(s, "=>", result);
    checkState(result.equals(expected), "expected:", expected);
  }

  private static class State {
    int h;
    int x;
  }

  private List<State> mStateBin = new ArrayList<>();

  private State newState(int h, int x) {
    int slot = mStateBin.size() - 1;
    State s = null;
    if (slot < 0) {
      s = new State();
    } else {
      s = mStateBin.remove(slot);
    }
    s.h = h;
    s.x = x;
    return s;
  }

  public String SLOWshortestPalindrome(String s) {
    final var n = s.length();
    if (n == 0)
      return s;

    // Define a state T(h, x) to mean that a palindrome of length h exists in s starting at character x.

    // Determine which values of x might be useful.  x must be <= this value.  Note, special care is
    // taken to make sure this is correct for both even- and odd-length strings.
    var xUseful = n / 2;

    var stack0 = new ArrayList<State>();
    var stack1 = new ArrayList<State>();

    for (int i = 0; i <= xUseful; i++) {
      stack0.add(newState(0, i));
      stack0.add(newState(1, i));
    }

    int longestPrefixLength = 0;

    while (!stack0.isEmpty()) {

      for (var st : stack0) {
        var x = st.x;
        var h = st.h;
        if (x == 0) {
          if (h > longestPrefixLength) {
            longestPrefixLength = h;
          }
        } else {
          int scanLeft = x;
          int scanRight = x + h - 1;
          while (scanLeft > 0 && scanRight < n - 1 && s.charAt(scanLeft - 1) == s.charAt(scanRight + 1)) {
            scanLeft--;
            scanRight++;
          }
          if (scanLeft < x)
            stack1.add(newState(scanRight + 1 - scanLeft, scanLeft));
        }
      }

      var tmp = stack0;
      stack0 = stack1;
      stack1 = tmp;
      stack1.clear();
    }
    var sb = new StringBuilder(2 * n - longestPrefixLength);
    for (int j = n - 1; j >= longestPrefixLength; j--)
      sb.append(s.charAt(j));
    sb.append(s);
    return sb.toString();
  }

  public String shortestPalindrome(String s) {
    return s;
  }

}