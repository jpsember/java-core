package js.leetcode;

import static js.base.Tools.*;

public class LongestCommonSubstring extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubstring().run();
  }

  public void run() {
    x("abcde", "bxbcdj");
    // x(1966, 1000, 316);
  }

  private void x(String a, String b) {
    x(a, b, -1);
  }

  private void x(String a, String b, int expected) {
    db = Math.max(a.length(), b.length()) < 30;

    Alg alg1 = new RecursionNaive();

    pr(a, CR, b);
    checkpoint(alg1.getClass().getSimpleName());
    var res = alg1.lengthOfLCS(a, b);
    checkpoint("Done");
    pr(INDENT, res);
    if (expected < 0) {
      Alg alg2 = new RecursionNaive();
      checkpoint(alg2.getClass().getSimpleName());
      expected = alg2.lengthOfLCS(a, b);
      checkpoint("Done");
    }

    verify(res, expected);
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

  private abstract class Alg {

    public final int lengthOfLCS(String text1, String text2) {
      var a = text1.getBytes();
      var b = text2.getBytes();
      return longestCommonSubstring(a, b);
    }

    abstract int longestCommonSubstring(byte[] a, byte[] b);

    protected String dbstr(byte[] b, int bCursor) {
      if (!db)
        return null;
      return "\"" + new String(b, bCursor, b.length - bCursor) + "\"";
    }

  }

  // ------------------------------------------------------------------

  class RecursionNaive extends Alg {

    @Override
    public int longestCommonSubstring(byte[] a, byte[] b) {
      return recursionAux(a, 0, b, 0, 0);
    }

    private int recursionAux(byte[] a, int aCursor, byte[] b, int bCursor, int prefixLength) {
      int result = prefixLength;
      pushIndent();
      db(dbstr(a, aCursor), dbstr(b, bCursor), prefixLength);
      // Base cases:
      //
      // If length of either string is zero, answer is zero
      // 
      // Recursive case:
      //
      // If first chars are the same, answer is 1 plus the LCS of the suffixes.
      // Otherwise, the answer is greater of LCS(a+1,b+1), LCS(a, b+1), LCS(a+1,b).
      //

      // Base case: if either string has length zero
      if (aCursor == a.length || bCursor == b.length)
        ;
      else if (a[aCursor] == b[bCursor])
        result = recursionAux(a, aCursor + 1, b, bCursor + 1, prefixLength + 1);
      else {
        result = Math.max(result, recursionAux(a, aCursor + 1, b, bCursor, 0));
        result = Math.max(result, recursionAux(a, aCursor, b, bCursor + 1, 0));
      }

      popIndent();
      return result;
    }
  }

}
