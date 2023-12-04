
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

    x("abccccccccc");

    xRep("a", 20);

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
    var expected = slow(s);
    var result = shortestPalindrome(s);
    if (s.length() < 40)
      pr(s, "=>", result);
    checkState(result.equals(expected), "expected:", expected);
  }

  // ------------------------------------------------------------------
  // This much simpler algorithm is at least as fast as mine, but
  // the call to the system 'hasPrefix' is still expensive (and 
  // the speed is relying on its native implementation probably).
  //
  private String slow(String s) {
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

  // ------------------------------------------------------------------

  public String shortestPalindrome(String s) {
    sStringBytes = s.getBytes();
    sStringLength = sStringBytes.length;
    compressString(sStringLength);
    auxShortestPalindrome();
    return decodeString(s);
  }

  /**
   * Encode long runs of single characters in a form that is decodeable and is
   * itself a palindrome
   */
  private void compressString(int n) {
    var cursor = 0;
    var b = sStringBytes;
    byte cp = 0;
    int count = 0;
    for (int i = 0; i < n; i++) {
      var c = b[i];
      if (c != cp) {
        cursor = write(cp, count, cursor);
        cp = c;
        count = 0;
      }
      count++;
    }
    sStringLength = write(cp, count, cursor);
  }

  private static int write(byte charByte, int count, int c) {
    var w = sStringBytes;
    if (count > 9) { // we don't save any space until we write more than 9 chars as 
                     // Ahhhh{hhhA}  where {hhhA} are redundant chars to make it a palindrome
      byte j = (byte) (charByte - 0x20);
      byte c0 = (byte) ('0' + (count & 0xf));
      byte c1 = (byte) ('0' + ((count >> 4) & 0xf));
      byte c2 = (byte) ('0' + ((count >> 8) & 0xf));
      byte c3 = (byte) ('0' + ((count >> 12) & 0xf));
      w[c + 0] = w[c + 8] = j;
      w[c + 1] = w[c + 7] = c0;
      w[c + 2] = w[c + 6] = c1;
      w[c + 3] = w[c + 5] = c2;
      w[c + 4] = c3;
      return c + 9;
    } else {
      for (int j = 0; j < count; j++)
        w[c + j] = charByte;
      return c + count;
    }
  }

  private String decodeString(String s2) {
    var sb = sStringBuilder;
    sb.setLength(0);

    var src = sStringBytes;
    int blen = sStringLength;

    int i = 0;
    while (i < blen) {
      int c = src[i++] & 0xff;
      if (c >= 'a') {
        sb.append((char) c);
      } else {
        c ^= 0x20;
        var count = ((int) (src[i] - '0')) //
            | ((int) (src[i + 1] - '0') << 4) //
            | ((int) (src[i + 2] - '0') << 8) //
            | ((int) (src[i + 3] - '0') << 12);
        i += 8;
        while (count-- != 0)
          sb.append((char) c);
      }
    }
    return sb.toString();
  }

  private void auxShortestPalindrome() {
    var s = sStringBytes;
    int prefixLength = sStringLength;
    while (true) {
      int a = 0;
      int b = prefixLength - 1;
      while (a < b && s[a] == s[b]) {
        a++;
        b--;
      }
      if (a >= b)
        break;
      prefixLength--;
    }

    byte[] aux = new byte[sStringLength * 2 - prefixLength];

    int t = 0;

    // Reverse the suffix to the front
    int src = sStringLength - 1;
    int suffixLength = sStringLength - prefixLength;
    while (t < suffixLength) {
      aux[t++] = s[src--];
    }

    for (int i = 0; i < sStringLength; i++)
      aux[t++] = s[i];
    sStringBytes = aux;
    sStringLength = t;
  }

  private static byte[] sStringBytes;
  private static int sStringLength;
  private static final StringBuilder sStringBuilder = new StringBuilder();
}
