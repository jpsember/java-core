
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
 * I think I am not skipping exploring states that can't lead to a better result
 * than the one already found.
 */
public class P214ShortestPalindrome {

  public static void main(String[] args) {

    new P214ShortestPalindrome().run();
  }

  private void run() {
    x("a");

    x("ababbbabbaba");

    for (int i = 1; i < 20; i++)
      xRep("a", i);
    checkpoint("before big");
    xRep("a", 40002);
    checkpoint("after big"); // 6.749; 7.608 after refactor to use state objects

    x("");
    x("AH");

    x("aacecaaa");

    x("abcd");

    xRep("a", 5000);
  }

  private void xRep(String pattern, int repCount) {
    var sb = new StringBuilder(pattern.length() * repCount);
    for (var i = 0; i < repCount; i++)
      sb.append(pattern);
    var s = sb.toString();
    x(s);
  }

  private void x(String s) {
    var expected = SLOWshortestPalindrome(s);
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

    for (int i = xUseful; i >= 0; i--) {
      // if (  (n & 1) == 0) {
      stack0.add(newState(0, i));
      stack0.add(newState(1, i));
      //        } else {
      //          stack0.add(newState(1, i));
      //          stack0.add(newState(0, i));
      //        }
    }

    int longestPrefixLength = 1;

    while (!stack0.isEmpty()) {

      for (var st : stack0) {
        var x = st.x;
        var h = st.h;

        // If best possible result from this state won't improve things, skip
        if (h + (x << 1) <= longestPrefixLength)
          continue;

        int scanLeft = x;
        int scanRight = x + h - 1;
        while (scanLeft > 0 && scanRight + 1 < n && s.charAt(scanLeft - 1) == s.charAt(scanRight + 1)) {
          scanLeft--;
          scanRight++;
        }

        if (scanLeft == 0)
          longestPrefixLength = scanRight + 1;
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

  // This much simpler algorithm is at least as fast as mine, but
  // the call to the system 'hasPrefix' is still expensive (and 
  // the speed is relying on its native implementation probably).
  //
  public String shortestPalindrome(String s) {
    int n = s.length();
    String r0 = reverse(s);
    var r = r0;
    while (!s.startsWith(r))
      r = r.substring(1);
    return r0.substring(0, n - r.length()) + s;
  }

  private static StringBuilder sb = new StringBuilder();

  private static String reverse(String s) {
    sb.setLength(0);
    sb.ensureCapacity(s.length());
    for (int i = s.length() - 1; i >= 0; i--)
      sb.append(s.charAt(i));
    return sb.toString();
  }

}
