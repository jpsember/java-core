
package js.leetcode;

import static js.base.Tools.*;

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
    x("aaa", "aa");
    // x("rabbbit", "rabbit");
    x("babgbag", "bag");
    x("a", "hello");
    x("hello", "e");

    x("aaaaaaaaaaa", "aaaaa");
  }

  private void x(String s, String t) {
    var expected = slow(s, t);
    var result = numDistinct(s, t);

    pr("s:", s, "t:", t, result);
    checkState(result == expected, "expected:", expected);
  }

  private int slow(String s, String t) {
    mMemo0.clear();
    return lookup0(s, t);
  }

  private int lookup0(String s, String t) {
    if (t.isEmpty())
      return 1;
    if (s.length() < t.length())
      return 0;

    var key = s + "|" + t;
    //pr(VERT_SP, "lookup:", key);
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
        sum += lookup0(s.substring(j + 1), tRemaining);
      }
    }

    //pr("...storing", key, "=>", count);
    mMemo0.put(key, sum);
    return sum;
  }

  private Map<String, Integer> mMemo0 = new HashMap<>();

  // ---------------------------------------------------------

  public int numDistinct(String s, String t) {
    
    // This doesn't seem to help, but doesn't hurt:
    
    // Remove all chars from s that don't occur in t
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char sc = s.charAt(i);
      if (t.indexOf(sc) >= 0)
        sb.append(sc);
    }
    s = sb.toString();
    if (s.length() < t.length())
      return 0;
    return lookup(s, t);
  }

  private int lookup(String s, String t) {

    if (s.length() < t.length())
      return 0;

    if (t.length() == 0)
      return 1;

    var key = s + "|" + t;
    Integer cachedValue = mMemo.get(key);
    if (cachedValue != null) {
      return cachedValue;
    }
    int result = 0;

    // Base case: |T| = 1
    //
    if (t.length() == 1) {
      var tch = t.charAt(0);
      for (int j = 0; j < s.length(); j++) {
        if (s.charAt(j) == tch)
          result++;
      }
    } else {

      // Split T into Ta and Tb.

      int tMid = t.length() / 2;

      // Let tb = tStartChar|tRemainder
      //
      // Look for occurrences si of tStartChar within S.
      //
      // For each of these, add numDistinct(S..si, ta) * numDistinct(si+1...n, tRemainder).

      char tMidChar = t.charAt(tMid);
      String tRemainder = null;
      String ta = null;

      var scanStart = tMid;
      var scanStop = s.length() - tMid;

      for (int j = scanStart; j <= scanStop; j++) {
        if (s.charAt(j) == tMidChar) {
          if (ta == null) {
            ta = t.substring(0, tMid);
          }

          int resultA = lookup(s.substring(0, j), ta);
          if (resultA != 0) {
            int resultB;
            if (tMid + 1 == t.length())
              resultB = 1;
            else {
              if (tRemainder == null)
                tRemainder = t.substring(tMid + 1);
              resultB = lookup(s.substring(j + 1), tRemainder);
            }
            result += resultA * resultB;
          }
        }
      }
    }
    mMemo.put(key, result);
    return result;
  }

  private Map<String, Integer> mMemo = new HashMap<>();

}
