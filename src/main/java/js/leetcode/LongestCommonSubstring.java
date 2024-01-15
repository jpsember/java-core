package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class LongestCommonSubstring extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubstring().run();
  }

  public void run() {
    x("abcde", "bxbcdj");
    x(1966, 1000, -1);
  }

  private void x(String a, String b) {
    x(a, b, -1);
  }

  private void x(String a, String b, int expected) {
    db = Math.max(a.length(), b.length()) < 30;

    Alg alg1 = new DP();

    pr(a, CR, b);
    checkpoint(alg1.getClass().getSimpleName());
    var res = alg1.lengthOfLCS(a, b);
    checkpoint("Done");
    pr(INDENT, res);

    if (expected < 0) {
      db = false;
      Alg alg2 = new Recursion();
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

    protected String dbstr(byte[] b) {
      return dbstr(b, 0);
    }

    protected String dbstr(byte[] b, int bCursor) {
      if (!db)
        return null;
      return "\"" + new String(b, bCursor, b.length - bCursor) + "\"";
    }

  }

  // ------------------------------------------------------------------

  class Recursion extends Alg {

    @Override
    public int longestCommonSubstring(byte[] a, byte[] b) {
      mMemo.clear();
      return recursionAux(a, 0, b, 0, 0);
    }

    private int recursionAux(byte[] a, int aCursor, byte[] b, int bCursor, int prefixLength) {
      if (aCursor == a.length || bCursor == b.length)
        return prefixLength;

      var key = (aCursor << 20) | (bCursor << 10) | prefixLength;
      var result = mMemo.getOrDefault(key, -1).intValue();
      if (result >= 0)
        return result;

      result = prefixLength;

      // Base case: if either string has length zero
      if (aCursor == a.length || bCursor == b.length)
        ;
      else if (a[aCursor] == b[bCursor])
        result = recursionAux(a, aCursor + 1, b, bCursor + 1, prefixLength + 1);
      else {
        result = Math.max(result, recursionAux(a, aCursor + 1, b, bCursor, 0));
        result = Math.max(result, recursionAux(a, aCursor, b, bCursor + 1, 0));
      }

      mMemo.put(key, result);

      return result;
    }

    private Map<Integer, Integer> mMemo = new HashMap<>();
  }

  class DP extends Alg {

    @Override
    public int longestCommonSubstring(byte[] a, byte[] b) {
      int result = 0;

      int width = a.length + 1;
      int height = b.length + 1;
      var rowPrev = new int[width];
      var rowCurr = new int[width];

      for (int y = 1; y < height; y++) {
        for (int x = 1; x < width; x++) {
          var v = 0;
          if (a[x - 1] == b[y - 1]) {
            v = 1 + rowPrev[x - 1];
            result = result < v ? v : result;
          }
          rowCurr[x] = v;
        }
        var tmp = rowPrev;
        rowPrev = rowCurr;
        rowCurr = tmp;
      }
      return result;
    }

  }

}
