package js.leetcode;

import static js.base.Tools.*;

public class LongestCommonSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubsequence().run();
  }

  public void run() {
    loadTools();
    // x("abcde", "ace", 3);
    x(1965, 17, 5);
  }

  private void x(String a, String b, int expected) {
    var res = longestCommonSubsequence(a, b);
    pr(a, CR, b, INDENT, res);
    verify(res, expected);
  }

  private void x(int seed, int length) {
    x(seed, length, -1);
  }

  private void x(int seed, int length, int expected) {
    rand(seed);
    var a = randString(length);
    var b = randString(length);
    var ca = a.toCharArray();
    var cb = b.toCharArray();

    checkpoint("Starting");
    var res = longestCommonSubsequence(a, b);
    checkpoint("Done");
    pr(a, CR, b, INDENT, res);
    if (expected < 0) {
      expected = new RecursionNaive().longestCommonSubsequence(ca, cb);
    }
    verify(res, expected);
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

  // ------------------------------------------------------------------

  public int longestCommonSubsequence(String text1, String text2) {
    var ca = text1.toCharArray();
    var cb = text2.toCharArray();
    return new RecursionNaive().longestCommonSubsequence(ca, cb);
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
      if (aCursor == a.length || bCursor == b.length)
        return 0;
      if (a[aCursor] == b[bCursor])
        return 1 + aux(a, aCursor + 1, b, bCursor + 1);
      int ap = find(a, aCursor + 1, b[bCursor]);
      int bp = find(b, bCursor + 1, a[aCursor]);
      return Math.max(aux(a, ap, b, bCursor), aux(a, aCursor, b, bp));
    }

    private int find(char[] array, int cursor, char seekChar) {
      while (cursor < array.length && array[cursor] != seekChar)
        cursor++;
      return cursor;
    }
  }

}
