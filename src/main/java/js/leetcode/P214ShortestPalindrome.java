
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
    x("ah");

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

  /* private */ static boolean isPalindrome(String s, int prefixLength) {
    int i = 0;
    int j = prefixLength - 1;
    while (i < j) {
      if (s.charAt(i) != s.charAt(j))
        return false;
      i++;
      j--;
    }
    return true;
  }

  /**
   * This now beats 75% of the users (runtime)
   */
  public String shortestPalindrome(String s) {
    int n = s.length();
    if (n <= 1)
      return s;
    if (n < 20)
      return auxShortestPalindrome(s);

    var sb = sStringBuilder;

    // Encode long runs of single characters in a form that is decodeable and is itself a palindrome
    //
    {
      sb.setLength(0);
      char cp = 0;
      int count = 0;
      for (int i = 0; i < n; i++) {
        var c = s.charAt(i);
        if (c != cp) {
          write(cp, count);
          cp = c;
          count = 0;
        }
        count++;
      }
      write(cp, count);
      s = sb.toString();
    }

    var s2 = auxShortestPalindrome(s);

    // Decode the run length encoding expressions performed earlier
    //
    {
      sb.setLength(0);
      int i = 0;
      while (i < s2.length()) {
        var c = s2.charAt(i++);
        if (c >= 'a') {
          sb.append(c);
        } else {
          c ^= 0x20;
          var count = ((int) (s2.charAt(i) - '0')) //
              | ((int) (s2.charAt(i + 1) - '0') << 4) //
              | ((int) (s2.charAt(i + 2) - '0') << 8) //
              | ((int) (s2.charAt(i + 3) - '0') << 12);
          i += 8;
          while (count-- != 0)
            sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private static void write(char c, int count) {
    var sb = sStringBuilder;
    if (count > 9) { // we don't save any space until we write more than 9 chars as 
                     // Ahhhh{hhhA}  where {hhhA} are redundant chars to make it a palindrome
      char j = (char) (c - 0x20);
      sb.append(j);
      char c0 = (char) ('0' + (count & 0xf));
      char c1 = (char) ('0' + ((count >> 4) & 0xf));
      char c2 = (char) ('0' + ((count >> 8) & 0xf));
      char c3 = (char) ('0' + ((count >> 12) & 0xf));
      sb.append(c0);
      sb.append(c1);
      sb.append(c2);
      sb.append(c3);
      sb.append(c2);
      sb.append(c1);
      sb.append(c0);
      sb.append(j);
    } else {
      for (int j = 0; j < count; j++)
        sb.append(c);
    }
  }

  // This much simpler algorithm is at least as fast as mine, but
  // the call to the system 'hasPrefix' is still expensive (and 
  // the speed is relying on its native implementation probably).
  //
  private String auxShortestPalindrome(String s) {
    int n = s.length();
    String r0 = reverse(s);
    var r = r0;
    while (!hasPrefix(s, r))
      r = r.substring(1);
    return r0.substring(0, n - r.length()) + s;
  }

  private static boolean hasPrefix(String s, String r) {
    int rn = r.length();
    int i = 0;
    int step = 0;
    while (i < rn) {
      if (s.charAt(i) != r.charAt(i))
        return false;
      step++;
      i += step;
    }
    return s.startsWith(r);
  }

  private static String reverse(String s) {
    var sb = sStringBuilder;
    sb.setLength(0);
    sb.ensureCapacity(s.length());
    for (int i = s.length() - 1; i >= 0; i--)
      sb.append(s.charAt(i));
    return sb.toString();
  }

  private static final StringBuilder sStringBuilder = new StringBuilder();

}
