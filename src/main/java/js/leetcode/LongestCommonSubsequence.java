package js.leetcode;

import static js.base.Tools.*;

public class LongestCommonSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubsequence().run();
  }

  public void run() {
    loadTools();
    //    db = true;

    // x("zbbddcc", "aaabbcc");

    // x("abcde", "ace", 3);
    x(1965, 30, 7);
    x(1966, 50, 14);
  }

  private void x(String a, String b) {
    x(a, b, -1);
  }

  private void x(String a, String b, int expected) {
    var ca = a.toCharArray();
    var cb = b.toCharArray();

    //    var res = longestCommonSubsequence(a, b);
    //    pr(a, CR, b, INDENT, res);
    //    
    //   
    pr(a, CR, b);
    //  if (db)
    checkpoint("Starting");
    var res = longestCommonSubsequence(a, b);
    // if (db)
    checkpoint("Done");
    pr(INDENT, res);
    if (expected < 0) {
      expected = new Recursion2().longestCommonSubsequence(ca, cb);
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

  interface Alg {
    int longestCommonSubsequence(char[] a, char[] b);
  }

  private String dbstr(char[] b, int bCursor) {
    if (!db)
      return null;
    return "\"" + new String(b, bCursor, b.length - bCursor) + "\"";
  }

  // ------------------------------------------------------------------

  public int longestCommonSubsequence(String text1, String text2) {
    var ca = text1.toCharArray();
    var cb = text2.toCharArray();
    return new Recursion2().longestCommonSubsequence(ca, cb);
  }

  private class RecursionNaive implements Alg {
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

  private class Recursion2 implements Alg {
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
        // Otherwise, the answer is greater of LCS(a+1,b+1), LCS(a, b'), LCS(a',b),
        // where a' is the smallest value that satisfies   A[a'>a] == B[b] (or A.length if not found).
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

    private int find(char[] array, int cursor, char seekChar) {
      while (cursor < array.length && array[cursor] != seekChar)
        cursor++;
      return cursor;
    }
  }

}
