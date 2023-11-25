
package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class P115DistinctSubsequences {

  public static void main(String[] args) {
    new P115DistinctSubsequences().run();
  }

  private void run() {
    x("rabbbit", "rabbit");
    x("babgbag", "bag");
    x("", "hello");
    x("hello", "");
  }

  private void x(String s, String t) {
    pr("s:", s, "t:", t, INDENT, numDistinct(s, t));
  }

  public int numDistinct(String s, String t) {
    mMemo.clear();
    return lookup(s, t);
  }

  private int lookup(String s, String t) {
    if (t.isEmpty())
      return 1;
    if (s.length() < t.length())
      return 0;

    var key = s + "|" + t;
    //pr(VERT_SP, "lookup:", key);
    Integer cachedValue = mMemo.get(key);
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
        sum += lookup(s.substring(j + 1), tRemaining);
      }
    }

    //pr("...storing", key, "=>", count);
    mMemo.put(key, sum);
    return sum;
  }

  private Map<String, Integer> mMemo = new HashMap<>();

}