package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import js.json.JSList;

/**
 * I solved this problem before, but it has been a while and I want to see if I
 * can solve it again (efficiently).
 */
public class DistinctSubsequencesRevisited extends LeetCode {

  public static void main(String[] args) {
    new DistinctSubsequencesRevisited().run();
  }

  public void run() {
    x("rabbbit", "rabbit", 3);
    x("babgbag", "bag", 5);
  }

  private void x(String s, String target, int expected) {
    db = s.length() <= 20;
    var res = numDistinct(s, target);
    pr("Result:", res);
    verify(res, expected);
  }

  public int numDistinct(String s, String t) {
    mMemo.clear();
    var sb = s.getBytes();
    var tb = t.getBytes();

    db("numDistinct, s:", s, "target:", t);
    // Not worth doing(?): remove bytes from s that don't appear in target

    return aux(sb, 0, tb, 0);
  }

  private String dc(byte[] bytes, int cursor) {
    return new String(bytes, cursor, bytes.length - cursor);
  }

  private String dc(byte b) {
    return Character.toString((char) b);
  }

  private int aux(byte[] s, int sStart, byte[] t, int tStart) {
    pushIndent(2);
    var r = aux0(s, sStart, t, tStart);
    popIndent();
    return r;
  }

  private int aux0(byte[] s, int sStart, byte[] t, int tStart) {
    db("aux s:", dc(s, sStart), "t:", dc(t, tStart));
    if (tStart == t.length)
      return 1;

    // Avoid looking for the target character in parts of the source string where
    // insufficient characters follow it to contain the remaining target chars

    int sMax = s.length - (t.length - tStart - 1);

    //sMax = s.length;

    var tFirst = t[tStart];
    // Find first occurrence of tFirst within s
    var found = -1;
    int i = sStart;
    while (i < sMax) {
      if (s[i] == tFirst) {
        found = i;
        db("...found:", dc(tFirst), "at:", i);
        break;
      }
      i++;
    }
    if (found < 0) {
      db("...can't find", dc(tFirst), "within s; s.len:", s.length, "t.len:", t.length, "sMax:", sMax);
      return 0;
    }

    int key = (i << 10) | tStart;
    var result = mMemo.getOrDefault(key, -1);
    if (result < 0) {

      // Skip past the first occurrence found
      i++;

      // count substrings of remainder of t within the string following our found one
      result = aux(s, i, t, tStart + 1);

      // Count number of other occurrences of tFirst within the remainder of s
      while (i < sMax) {
        if (s[i] == tFirst) {
          // count substrings of remainder of t within the string following our found one
          result += aux(s, i + 1, t, tStart + 1);
        }
        i++;
      }
      db("...memoizing", key, "=>", result);
      mMemo.put(key, result);
    }
    return result;
  }

  private Map<Integer, Integer> mMemo = new HashMap<>();
}
