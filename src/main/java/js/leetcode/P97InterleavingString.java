package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class P97InterleavingString {

  public static void main(String[] args) {
    new P97InterleavingString().run();
  }

  private void run() {
    x("aabcc", "dbbca", "aadbbbaccc", false);
    x("abc", "def", "abdcef", true);
    x("abc", "def", "abxcef", false);
  }

  private void x(String a, String b, String c, boolean expected) {
    var result = isInterleave(a, b, c);
    pr("a:", a, "b:", b, "c:", c, "result:", result);
    checkState(result == expected, "expected:", expected);
  }

  // ------------------------------------------------------------------

  private static final boolean sortHeuristic = false;

  public boolean isInterleave(String sa, String sb, String sc) {
    var a = sa.getBytes();
    var b = sb.getBytes();
    var c = sc.getBytes();
    if (a.length + b.length != c.length)
      return false;

    if (sortHeuristic) {
      if (!Arrays.equals(sorted(c), sorted(append(a, b))))
        return false;
    }

    memo.clear();
    return auxInterleave(a, b, c, 0, 0);
  }

  private static byte[] append(byte[] a, byte[] b) {
    var out = new byte[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  private static byte[] sorted(byte[] s) {
    var c = Arrays.copyOf(s, s.length);
    Arrays.sort(c);
    return c;
  }

  private boolean auxInterleave(byte[] a, byte[] b, byte[] c, int acurs, int bcurs) {
    var key = (acurs << 8) | bcurs;
    if (memo.containsKey(key))
      return memo.get(key);

    boolean res = true;
    outer: do {
      boolean adone = acurs == a.length;
      boolean bdone = bcurs == b.length;
      if (adone && bdone)
        break;

      for (int pass = 0; pass < 2; pass++) {
        boolean addA = (pass == 0);
        var next = c[acurs + bcurs];
        int newa = acurs;
        if (addA) {
          if (adone)
            continue;
          if (a[acurs] != next)
            continue;
          newa++;
        }
        int newb = bcurs;
        if (!addA) {
          if (bdone)
            continue;
          if (b[bcurs] != next)
            continue;
          newb++;
        }

        if (auxInterleave(a, b, c, newa, newb))
          break outer;
      }
      res = false;
    } while (false);
    memo.put(key, res);
    return res;
  }

  private Map<Integer, Boolean> memo = new HashMap<>();

}
