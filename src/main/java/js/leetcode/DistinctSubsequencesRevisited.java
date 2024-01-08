package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

/**
 * I solved this problem before, but it has been a while and I want to see if I
 * can solve it again (efficiently).
 */
public class DistinctSubsequencesRevisited extends LeetCode {

  public static void main(String[] args) {
    new DistinctSubsequencesRevisited().run();
  }

  public void run() {
    x("babgbag", "bag");
    x("rabbbit", "rabbit");
  }

  private void x(String s, String target) {
    x(s, target, -1);
  }

  private void x(String s, String target, int expected) {
    db = s.length() <= 20;

    var res = numDistinct(s, target);
    pr("Result:", res);
    if (expected < 0)
      expected = numDistinctMemo(s, target);
    verify(res, expected);
  }

  public int numDistinctMemo(String s, String t) {
    mMemo.clear();
    var sb = s.getBytes();
    var tb = t.getBytes();
    // Not worth doing(?): remove bytes from s that don't appear in target
    return aux(sb, 0, tb, 0);
  }

  private int aux(byte[] s, int sStart, byte[] t, int tStart) {
    if (tStart == t.length)
      return 1;

    // Avoid looking for the target character in parts of the source string where
    // insufficient characters follow it to contain the remaining target chars

    int sMax = s.length - (t.length - tStart - 1);

    var tFirst = t[tStart];
    // Find first occurrence of tFirst within s
    var found = -1;
    int i = sStart;
    while (i < sMax) {
      if (s[i] == tFirst) {
        found = i;
        break;
      }
      i++;
    }
    if (found < 0) {
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
      mMemo.put(key, result);
    }
    return result;
  }

  private Map<Integer, Integer> mMemo = new HashMap<>();

  // ------------------------------------------------------------------

  public int numDistinct(String s, String t) {

    // A dynamic programming approach.  We have a table of cells indexed horizontally
    // by the cursor position within the source string, and vertically by the cursor
    // position within the target string. 
    //
    // The cells contain the number of distinct paths that have reached the cell.
    //
    // As we process each cell, we propagate its path count horizontally, for the
    // case where we choose not to match the current source and target characters
    // (only the source cursor advances).
    //
    // If the source and target characters do match, we also propagate the cell's
    // path count horizontally *and* vertically, to represent both cursors being
    // advanced.

    // Add a sentinel value to both strings to simplify the logic
    s = s + "$";
    t = t + "$";

    // For efficiency, read string lengths to local variables
    final int slen = s.length();
    final int tlen = t.length();
    final var cell = new int[t.length()][s.length()];

    // There is a single path initially (no splitting)
    cell[0][0] = 1;

    for (int sCursor = 0; sCursor < s.length() - 1; sCursor++) {

      if (db) {
        db(VERT_SP, "s:", sCursor, INDENT, strTable(cell, s, t));
      }

      for (int tCursor = 0; tCursor < tlen; tCursor++) {
        var count = cell[tCursor][sCursor];
        if (count == 0) // not strictly necessary, but more efficient
          continue;
        cell[tCursor][sCursor + 1] += count;
        if (s.charAt(sCursor) == t.charAt(tCursor)) {
          cell[tCursor + 1][sCursor + 1] += count;
          db("...set", tCursor + 1, sCursor + 1, "=>", cell[tCursor + 1][sCursor + 1]);
        }
      }
    }
    if (db)
      db(VERT_SP, "final table:", INDENT, strTable(cell, s, t));
    return cell[tlen - 1][slen - 1];
  }

}
