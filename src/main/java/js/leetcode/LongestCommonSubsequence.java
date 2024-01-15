package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class LongestCommonSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubsequence().run();
  }

  public void run() {
    x("abcde", "ace");
    x(1966, 1000, 316);
    //    x("a", "a");
    //    x("a", "b");

  }

  private void x(String a, String b) {
    x(a, b, -1);
  }

  private void x(String a, String b, int expected) {

    //    var ca = a.toCharArray();
    //    var cb = b.toCharArray();

    db = Math.max(a.length(), b.length()) < 30;

    Alg alg1 = new DynamicProgrammingLinear();
    Alg alg2 = new DynamicProgrammingLinearHorzScan();

    pr(a, CR, b);
    checkpoint("DynamicProgramming");
    var res = alg1.longestCommonSubsequence(a, b);
    checkpoint("Done", res);

    checkpoint("DP Linear");
    res = alg2.longestCommonSubsequence(a, b);
    checkpoint("Done", res);

    pr(INDENT, res);
    if (expected < 0) {
      expected = new RecursionWithMemo().longestCommonSubsequence(a, b);
    }
    verify(res, expected);
  }

  private void x(int seed, int length) {
    x(seed, length, -1);
  }

  private void x(int seed, int length, int expected) {
    rand(seed);
    var a = randString(length);
    var b = randString(length);
    x(a, b, expected);
  }

  private String randString(int length) {
    var sb = sb();
    for (int i = 0; i < length; i++)
      sb.append((char) ('a' + rand().nextInt(26)));
    return sb.toString();
  }

  abstract class Alg {

    public final int longestCommonSubsequence(String text1, String text2) {
      var a = text1.toCharArray();
      var b = text2.toCharArray();
      return longestCommonSubsequence(a, b);
    }

    abstract int longestCommonSubsequence(char[] a, char[] b);
  }

  private String dbstr(char[] b, int bCursor) {
    if (!db)
      return null;
    return "\"" + new String(b, bCursor, b.length - bCursor) + "\"";
  }

  // ------------------------------------------------------------------

  private int find(char[] array, int cursor, char seekChar) {
    while (cursor < array.length && array[cursor] != seekChar)
      cursor++;
    return cursor;
  }

  class RecursionNaive extends Alg {
    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {

      return recursionAux(a, 0, b, 0);
    }

    private int recursionAux(char[] a, int aCursor, char[] b, int bCursor) {
      // Base cases:
      //
      // If length of either string is zero, answer is zero
      // 
      // Recursive case:
      //
      // If first chars are the same, answer is 1 plus the LCS of the suffixes.
      // Otherwise, the answer is greater of LCS(a+1,b+1), LCS(a, b+1), LCS(a+1,b).
      //
      if (aCursor == a.length || bCursor == b.length)
        return 0;
      if (a[aCursor] == b[bCursor])
        return 1 + recursionAux(a, aCursor + 1, b, bCursor + 1);
      return Math.max(recursionAux(a, aCursor + 1, b, bCursor), recursionAux(a, aCursor, b, bCursor + 1));
    }
  }

  class Recursion extends Alg {
    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {
      return aux(a, 0, b, 0);
    }

    private int aux(char[] a, int aCursor, char[] b, int bCursor) {
      pushIndent(2);
      if (db)
        db("aux", dbstr(a, aCursor), dbstr(b, bCursor));
      int result;
      do {
        // Base cases:
        //
        // If length of either string is zero, answer is zero
        // 
        // Recursive case:
        //
        // If first chars are the same, answer is 1 plus the LCS of the suffixes.
        // Otherwise, we either use the first character of A, 
        // use the 
        // Otherwise, the answer is greater of LCS(a+1,b+1), LCS(a, b'), LCS(a',b),
        // where a' is the smallest value that satisfies   A[a'>a] == B[b] (or A.length if not found).
        // In other words, look for the first appearance of A's character in B (and vice versa).
        //
        if (aCursor == a.length || bCursor == b.length) {
          result = 0;
          break;
        }
        if (a[aCursor] == b[bCursor]) {
          result = 1 + aux(a, aCursor + 1, b, bCursor + 1);
        } else {
          int ap = find(a, aCursor + 1, b[bCursor]);
          int bp = find(b, bCursor + 1, a[aCursor]);
          result = Math.max(aux(a, ap, b, bCursor), aux(a, aCursor, b, bp));
          if (aCursor + 1 != ap || bCursor + 1 != bp)
            result = Math.max(result, aux(a, aCursor + 1, b, bCursor + 1));
        }
      } while (false);
      db("...result:", result);
      popIndent();
      return result;
    }

  }

  class RecursionWithMemo extends Alg {

    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {
      mMemo.clear();
      return aux(a, 0, b, 0);
    }

    private Map<Integer, Integer> mMemo = new HashMap<>();

    private int aux(char[] a, int aCursor, char[] b, int bCursor) {
      // String length is at most 1000, which fits within 10 bits
      var key = (aCursor << 10) | bCursor;
      var result = mMemo.getOrDefault(key, -1).intValue();
      if (result >= 0)
        return result;

      if (db) {
        pushIndent(2);
        db("aux", dbstr(a, aCursor), dbstr(b, bCursor));
      }
      do {
        // Base cases:
        //
        // If length of either string is zero, answer is zero
        // 
        // Recursive case:
        //
        // If first chars are the same, answer is 1 plus the LCS of the suffixes.
        // Otherwise, we either use the first character of A, 
        // use the 
        // Otherwise, the answer is greater of LCS(a+1,b+1), LCS(a, b'), LCS(a',b),
        // where a' is the smallest value that satisfies   A[a'>a] == B[b] (or A.length if not found).
        // In other words, look for the first appearance of A's character in B (and vice versa).
        //
        if (aCursor == a.length || bCursor == b.length) {
          result = 0;
          break;
        }
        if (a[aCursor] == b[bCursor]) {
          result = 1 + aux(a, aCursor + 1, b, bCursor + 1);
        } else {
          int ap = find(a, aCursor + 1, b[bCursor]);
          int bp = find(b, bCursor + 1, a[aCursor]);
          result = Math.max(aux(a, ap, b, bCursor), aux(a, aCursor, b, bp));
          if (aCursor + 1 != ap || bCursor + 1 != bp)
            result = Math.max(result, aux(a, aCursor + 1, b, bCursor + 1));
        }
      } while (false);
      mMemo.put(key, result);

      if (db) {
        db("...result:", result);
        popIndent();
      }
      return result;
    }
  }

  class DynamicProgramming extends Alg {

    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {

      // The DP grid includes an extra row and column so that every cursor position from 0 to n (inclusive) has a
      // slot.

      int height = b.length + 1;
      int width = a.length + 1;
      var cells = new short[height][width];

      // On each iteration, we scan a diagonal line at increasing distance from the origin (0,0).
      // We don't need to scan the line that consists of the top right point (width-1,height-1).
      int diagonals = width + height - 2;

      for (int i = 0; i < diagonals; i++) {
        // Determine the left endpoint of the diagonal
        var x = 0;
        var y = i;
        var extra = y - (height - 1);
        if (extra > 0) {
          x += extra;
          y -= extra;
        }

        while (y >= 0 && x < width) {
          short currentLength = cells[y][x];

          boolean af = x < width - 1;
          boolean bf = y < height - 1;

          // Propagate length to neighboring cells as appropriate
          if (af && bf && a[x] == b[y]) // characters match, propagate up and to the right
            cells[y + 1][x + 1] = (short) (currentLength + 1);
          if (af && cells[y][x + 1] < currentLength) // propagate current length to right
            cells[y][x + 1] = currentLength;
          if (bf && cells[y + 1][x] < currentLength) // propagate current length upward
            cells[y + 1][x] = currentLength;

          // Advance to next point on diagonal
          x++;
          y--;
        }
      }

      return cells[height - 1][width - 1];
    }

  }

  class DynamicProgrammingLinear extends Alg {

    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {

      // The DP grid includes an extra row and column so that every cursor position from 0 to n (inclusive) has a
      // slot.

      int height = b.length + 1;
      int width = a.length + 1;
      var cells = new short[height * width];

      // On each iteration, we scan a diagonal line at increasing distance from the origin (0,0).
      // We don't need to scan the line that consists of the top right point (width-1,height-1).
      int diagonals = width + height - 2;

      for (int i = 0; i < diagonals; i++) {

        // Determine the left endpoint of the diagonal
        var x = 0;
        var y = i;
        var extra = y - (height - 1);
        if (extra > 0) {
          x += extra;
          y -= extra;
        }
        var ci = y * width + x;

        while (ci >= 0) {
          short currentLength = cells[ci];

          boolean af = x < width - 1;
          boolean bf = y < height - 1;

          // Propagate length to neighboring cells as appropriate
          if (af && bf && a[x] == b[y]) // characters match, propagate up and to the right
            cells[ci + width + 1] = (short) (currentLength + 1);
          if (af && cells[ci + 1] < currentLength) // propagate current length to right
            cells[ci + 1] = currentLength;
          if (bf && cells[ci + width] < currentLength) // propagate current length upward
            cells[ci + width] = currentLength;

          // Advance to next point on diagonal
          ci -= width - 1;
          x++;
          y--;
        }
      }

      return cells[cells.length - 1];
    }

  }

  class DynamicProgrammingLinearHorzScan extends Alg {

    @Override
    public int longestCommonSubsequence(char[] a, char[] b) {

      // No need for tricky diagonal scan! Just process grid from left to right, bottom to top.

      // The DP grid includes an extra row and column so that every cursor position from 0 to n (inclusive) has a
      // slot.

      int height = b.length + 1;
      int width = a.length + 1;
      var cells = new short[height * width];

      var ci = 0;

      for (int y = 0; y < height; y++) {
        var ciNext = ci + width;
        boolean bf = y < height - 1;

        // Determine the left endpoint of the diagonal

        var ciStart = ci;
        while (ci < ciNext) {

          short currentLength = cells[ci];

          boolean af = ci + 1 < ciNext;

          // Propagate length to neighboring cells as appropriate
          if (af && bf && a[ci - ciStart] == b[y]) // characters match, propagate up and to the right
            cells[ci + width + 1] = (short) (currentLength + 1);
          if (af && cells[ci + 1] < currentLength) // propagate current length to right
            cells[ci + 1] = currentLength;
          if (bf && cells[ci + width] < currentLength) // propagate current length upward
            cells[ci + width] = currentLength;

          ci++;
        }
      }

      return cells[cells.length - 1];
    }

  }
}
