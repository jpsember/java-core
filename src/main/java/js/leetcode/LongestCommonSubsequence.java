package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class LongestCommonSubsequence extends LeetCode {

  public static void main(String[] args) {
    new LongestCommonSubsequence().run();
  }

  public void run() {
    loadTools();
    x("abcde", "ace");
    x(1966, 1000, 316);
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
      expected = new Recursion().longestCommonSubsequence(ca, cb);
      //      var exp2 = new RecursionNaive().longestCommonSubsequence(ca, cb);
      //      checkState(exp2 == expected);
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
    return new RecursionWithMemo().longestCommonSubsequence(ca, cb);
  }

  private int find(char[] array, int cursor, char seekChar) {
    while (cursor < array.length && array[cursor] != seekChar)
      cursor++;
    return cursor;
  }

  class RecursionNaive implements Alg {
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

  class Recursion implements Alg {
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

  class RecursionWithMemo implements Alg {

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
}
