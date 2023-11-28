
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
    x("a", "a");
    x("aacecaaa", "aaacecaaa");
    x("abcd", "dcbabcd");

    {
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

    final var n = s.length();

    // Define a state T(h, x) to mean that a palindrome of length h exists in s starting at character x.

    // We'll represent a state as an integer, with h in the lower 16 bits and x in the upper.
    // Note that the length of the input can exceed the 15 bit capacity for signed shorts, so 
    // we must be careful to cast them as unsigned values when decoding them.

    List<Integer> stack = new ArrayList<>(n * 5);
    for (int i = 0; i < n; i++)
      stack.add(0 | (i << 16));
    for (int i = 0; i < n; i++)
      stack.add(1 | (i << 16));

    int longestPrefixLength = 0;
    int cursor = 0;
    while (cursor < stack.size()) {
      var state = stack.get(cursor++);
      var h = state & 0xffff;
      var x = ((state >> 16) & 0xffff);
      //pr("state h:", h, "x:", x, "pal:", s.substring(x, x + h));
      if (x == 0) {
        if (h > longestPrefixLength) {
          longestPrefixLength = h;
          //pr("...new longest prefix:",longestPrefixLength);
        }
      } else {
        int j = x + h;
        if (j < n && s.charAt(x - 1) == s.charAt(j)) {
          //pr("....extending:",s.charAt(x-1),s.substring(x,x+h),s.charAt(j));
          stack.add((h + 2) | ((x - 1) << 16));
        }
      }
    }

    var sb = new StringBuilder(n * 2);
    //pr("longestpref:",longestPrefixLength);
    for (int j = n - 1; j >= longestPrefixLength; j--)
      sb.append(s.charAt(j));
    sb.append(s);
    return sb.toString();
  }
}
