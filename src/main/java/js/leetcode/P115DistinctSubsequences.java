
package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Algorithm works, but is much slower than most solutions.
 * 
 * Alternative algorithm; works a bit faster
 * 
 */
public class P115DistinctSubsequences {

  public static void main(String[] args) {
    new P115DistinctSubsequences().run();
  }

  private void run() {
    //    x("bcbabaa", "bba");
    //    x("aaa", "aa");
    //    x("rabbbit", "rabbit");
    //    x("babgbag", "bag");
    //    x("a", "hello");
    //    x("hello", "e");
    //
    //    
    //    x("aaaaaaaaaaaaaaaaaaaaaa", "aaaaa");

    var s = "aaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    s = s + s;
    s = s + s;
    s = s + s;
    // s = s+s;
    var t = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    checkpoint("old");
    pr(numDistinct2(s, t));
    checkpoint("new");
    pr(numDistinct(s, t));
    checkpoint("old");
    pr(numDistinct2(s, t));
    checkpoint("new");
    pr(numDistinct(s, t));
    checkpoint("old");
    pr(numDistinct2(s, t));
    checkpoint("new");
    pr(numDistinct(s, t));
    checkpoint("stop");
    //    x(s,t);
  }

  private int[][] memo;

  public int numDistinct2(String s, String t) {
    int m = s.length();
    int n = t.length();
    memo = new int[m][n];
    for (int i = 0; i < m; i++) {
      Arrays.fill(memo[i], -1);
    }
    return dp(s, 0, t, 0);
  }

  public int dp(String s, int i, String t, int j) {
    if (t.length() - j > s.length() - i) {
      return 0;
    }
    if (j == t.length()) {
      return 1;
    }
    if (memo[i][j] != -1) {
      return memo[i][j];
    }
    int ans = 0;
    if (s.charAt(i) == t.charAt(j)) {
      ans = dp(s, i + 1, t, j + 1) + dp(s, i + 1, t, j);
    } else {
      ans = dp(s, i + 1, t, j);
    }
    memo[i][j] = ans;
    return memo[i][j];
  }

  /*private*/ void x(String s, String t) {
    var expected = slow(s, t);
    var result = numDistinct(s, t);

    pr("s:", s, "t:", t, result);
    checkState(result == expected, "expected:", expected);
  }

  private Map<String, Integer> mMemo0 = new HashMap<>();

  private int slow(String s, String t) {
    mMemo0.clear();
    return slowLookup(s, t);
  }

  private int slowLookup(String s, String t) {
    if (t.isEmpty())
      return 1;
    if (s.length() < t.length())
      return 0;

    var key = s + "|" + t;
    Integer cachedValue = mMemo0.get(key);
    if (cachedValue != null) {
      return cachedValue;
    }

    // We will look for occurrences of the first character of T in S,
    // and for each of them, recursively look for the remaining characters of T in
    // the remaining characters of S.

    char tFirst = t.charAt(0);
    String tRemaining = null; // avoid constructing this substring unless needed

    int sum = 0;
    for (int j = 0; j < s.length(); j++) {
      if (s.charAt(j) == tFirst) {
        if (tRemaining == null)
          tRemaining = t.substring(1);
        sum += slowLookup(s.substring(j + 1), tRemaining);
      }
    }

    //pr("...storing", key, "=>", count);
    mMemo0.put(key, sum);
    return sum;
  }

  // ---------------------------------------------------------

  /**
   * For each ti in T, we have an array of integers of length |S| that represent
   * the number p of distinct subsequences of s that equal T1...i that end at
   * that character.
   * 
   * The logic for each row of integers is as follows:
   * 
   * set buffer = [0,0,....]
   * 
   * For r = 0...|T|-1:
   * 
   * set newbuffer = [0,0,...]
   * 
   * Set sum = 0
   * 
   * For each i = 0...|S|-1:
   * 
   * If Si = Tr: set newBuffer[i] = sum
   * 
   * Add buffer[i] to sum
   * 
   * end
   * 
   * set buffer = newbuffer
   * 
   * end
   * 
   */
  private static byte[] extractChars(String x) {
    // We allocate an extra zero byte so both s and t will end with a distinct sentinal byte
    byte[] result = new byte[x.length() + 1];
    for (int i = 0; i < x.length(); i++)
      result[i] = (byte) x.charAt(i);
    return result;
  }

  public int numDistinct(String s, String t) {
    final byte[] sBytes = extractChars(s);
    final byte[] tBytes = extractChars(t);

    final int sLength = sBytes.length;
    int[] buffer1 = new int[sLength];
    int[] buffer2 = new int[sLength];

    int sum = 1;
    for (var tc : tBytes) {
      for (int i = 0; i < sLength; i++) {
        buffer2[i] = sBytes[i] == tc ? sum : 0;
        sum += buffer1[i];
      }
      sum = 0;
      var tmp = buffer1;
      buffer1 = buffer2;
      buffer2 = tmp;
    }
    return buffer1[sLength - 1];
  }

}
