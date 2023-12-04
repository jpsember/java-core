
package js.leetcode;

import static js.base.Tools.*;

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
 * 
 * This is *not* a problem for dynamic programming, as palindromes do not really
 * exhibit the 'overlapping subproblems' property. It is true that each
 * palindrome contains n-1 nested palindromes within them, but for palindromes
 * that are centered at distinct locations, that is not true.
 * 
 */
public class P214ShortestPalindrome {

  public static void main(String[] args) {

    new P214ShortestPalindrome().run();
  }

  private void run() {

    xRep("a", 20);
    if (true)
      return;

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
    var expected = auxShortestPalindrome(s);
    pr(VERT_SP,"calculating result, s:",s);
    var result = shortestPalindrome(s);
    if (s.length() < 40)
      pr(s, "=>", result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String shortestPalindrome(String s) {
    if (s.length() < 20)
      return auxShortestPalindrome(s);
    s = compressString(s);
    s = auxShortestPalindrome(s);
    s = decodeString(s);
    return s;
  }

  /**
   * Encode long runs of single characters in a form that is decodeable and is
   * itself a palindrome
   */
  private String compressString(String s) {
    var n = s.length();
    var sb = sStringBuilder;
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

  private String decodeString(String s2) {
    var sb = sStringBuilder;
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
    return sb.toString();
  }

  private String auxShortestPalindrome(String s) {
    int prefixLength = s.length();
    while (true) {
      int a = 0;
      int b = prefixLength - 1;
      while (a < b && s.charAt(a) == s.charAt(b)) {
        a++;
        b--;
      }
      if (a >= b)
        break;
      prefixLength--;
    }
    return reverse(s.substring(prefixLength)) + s;
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
