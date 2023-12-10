package js.leetcode;

import static js.base.Tools.*;

import java.util.BitSet;

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

  public boolean isInterleave(String sa, String sb, String sc) {
    var a = sa.getBytes();
    var b = sb.getBytes();
    var c = sc.getBytes();
    if (a.length + b.length != c.length)
      return false;
    memo = new BitSet((a.length + 1) << 9);
    return auxInterleave(a, b, c, 0, 0);
  }

  private boolean auxInterleave(byte[] a, byte[] b, byte[] c, int acurs, int bcurs) {
    var memoIndex = ((acurs << 8) | bcurs) << 1;
    if (memo.get(memoIndex))
      return memo.get(memoIndex + 1);

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
    memo.set(memoIndex);
    memo.set(memoIndex + 1, res);
    return res;
  }

  private static BitSet memo;

}
