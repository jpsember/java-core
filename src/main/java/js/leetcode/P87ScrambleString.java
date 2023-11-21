package js.leetcode;

import static js.base.Tools.*;

import java.util.Map;

// There is a complication if there are duplicate characters in the string.
//
// I threw out my old (O(n) !) algorithm  and am using a different, recursive one.
//
public class P87ScrambleString {

  public static void main(String[] args) {
    new P87ScrambleString().run();
  }

  private static final boolean log = false;
  private static final boolean sameCharHeuristic = true;

  private void run() {
    checkpoint("starting");
    //go2("abc", "acb");
      go2("great", "rgeat");
    //  go2("abcde", "caebd");
    //    go2("a", "a");

    // This takes way too long; there must be a simpler algorithm
    go2("eebaacbcbcadaaedceaaacadccd", "eadcaacabaddaceacbceaabeccd");
    checkpoint("stopping");
  }

  private void go2(String orig, String scr) {
    resultCache.clear();
    iter = 0;
    pr(CR, orig, CR, scr, isScramble(orig, scr), "iter:", iter);
  }

  private int depth;

  private static final int MAX_STR_LEN = 30;
  private static byte[] work1 = new byte[MAX_STR_LEN];
  private static byte[] work2 = new byte[MAX_STR_LEN];

  private static boolean sameChars(String s1, String s2) {
    if (log)
      pr("same chars:", s1, s2);
    int n = s1.length();
    for (int i = 0; i < n; i++) {
      work1[i] = (byte) (s1.charAt(i) - 'a');
      work2[i] = (byte) (s2.charAt(i) - 'a');
    }
    int work2Length = n;

    outer: for (int i = 0; i < n; i++) {
      byte a = work1[i];

      for (int j = 0; j < work2Length; j++) {
        if (work2[j] == a) {
          work2Length--;
          work2[j] = work2[work2Length];
          continue outer;
        }
      }
      if (log)
        pr("...no!");
      return false;
    }
    if (log)
      pr("...yes");
    return true;
  }

  private int iter;

  private Map<String, Boolean> resultCache = hashMap();

  public boolean isScramble(String s1, String s2) {

    String key = s1 + " " + s2;
    Boolean cached = resultCache.get(key);
    if (cached != null)
      return cached;

    iter++;

    String sp = null;
    if (log) {
      sp = "| " + spaces(depth);
      depth++;
      pr(VERT_SP, sp, s1, s2);
    }

    // If s2 is a scrambled version of s1, then 
    //
    //  a) the same characters (with corresponding multiplicities) must exist in s1 and s2
    //  b) there must exist for some 0<i<ceil(n/2) a prefix s1[0..i] and suffix s1[i..n] that are 
    //      scrambled forms of either (s2[0..i] and s2[i..n]) or (s2[0..n-i] and s2[n-i..n]) respectively.
    // 
    // If b) is true then a) is also true, but for optimization a) might be useful

    // Recursion base case

    boolean result = false;
    outer: do {

      if (s1.length() <= 1) {
        result = s1.equals(s2);
        break;
      }

      if (sameCharHeuristic) {
        if (!sameChars(s1, s2))
          break;
      }

      result = true;

      int n = s1.length();

      for (int i = 1; i < n; i++) {
        int j = n - i;

        var prefix = s1.substring(0, i);
        var suffix = s1.substring(i);
        if (log)
          pr(sp, "n:", n, "i:", i, "prefix:", prefix, "suffix:", suffix);

        if (isScramble(prefix, s2.substring(0, i)) && isScramble(suffix, s2.substring(i))) {
          break outer;
        }
        if (isScramble(prefix, s2.substring(j)) && isScramble(suffix, s2.substring(0, j))) {
          break outer;
        }
      }
      result = false;
    } while (false);
    if (log) {
      pr(sp, result);
      depth--;
    }
    resultCache.put(key, result);
    return result;
  }
}
