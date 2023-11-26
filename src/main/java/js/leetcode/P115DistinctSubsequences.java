
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
    x("bcbabaa", "bba");
    x("aaa", "aa");
    x("rabbbit", "rabbit");
    x("babgbag", "bag");
    x("a", "hello");
    x("hello", "e");

    x("aaaaaaaaaaaaaaaaaaaaaa", "aaaaa");
  }

  private void x(String s, String t) {
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
  public int numDistinct(String s, String t) {
    // This doesn't seem to help, but doesn't hurt:
    // Remove all chars from s that don't occur in t
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char sc = s.charAt(i);
      if (t.indexOf(sc) >= 0)
        sb.append(sc);
    }

    // Add a unique sentinal character to the end of each string corresponding
    // to where we will read the final result

    sb.append(']');
    s = sb.toString();
    t = t + ']';

    int sLength = s.length();
    int[] buffer1 = new int[sLength];
    int[] buffer2 = new int[sLength];

    for (int ti = 0; ti < t.length(); ti++) {
      char tc = t.charAt(ti);
      int sum = (ti == 0) ? 1 : 0;
      for (int i = 0; i < sLength; i++) {
        buffer2[i] = (s.charAt(i) == tc) ? sum : 0;
        sum += buffer1[i];
      }
      var tmp = buffer1;
      buffer1 = buffer2;
      buffer2 = tmp;
    }
    return buffer1[sLength - 1];
  }

}
