package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class P97InterleavingString {

  public static void main(String[] args) {
    new P97InterleavingString().run();
  }

  private void run() {
    x("aabcc", "dbbca", "aadbbbaccc", false);
    x("abc", "def", "abdcef", true);
    x("abc", "def", "abxcef", false);
    if (false)
      x(100);
    if (false)
      x(100, 1965);
    x(100, 1965, "aaaaaab", "bbbbbba");
  }

  private String rndString(Random rnd, int len, String alpha) {

    var sb = new StringBuilder();
    for (int i = 0; i < len; i++)
      sb.append(alpha.charAt(rnd.nextInt(alpha.length())));
    return sb.toString();
  }

  private static final String def = "abcdefghijklmnopqrstuvwxyz";

  private void x(int len, int seed) {
    x(len, seed, null, null);
  }

  private void x(int len) {
    x(len, 1965, null, null);
  }

  private void x(int len, int seed, String alphaA, String alphaB) {
    if (alphaA == null)
      alphaA = def;
    if (alphaB == null)
      alphaB = def;
    var rnd = new Random(seed);
    var a = rndString(rnd, len, alphaA);
    var b = rndString(rnd, len, alphaB);
    var sb = new StringBuilder();
    int ac = 0;
    int bc = 0;
    while (sb.length() < len + len) {
      if (ac < a.length() && (bc == b.length() || rnd.nextBoolean()))
        sb.append(a.charAt(ac++));
      else
        sb.append(b.charAt(bc++));
    }
    x(a, b, sb.toString());
  }

  private void x(String a, String b, String c, boolean expected) {
    var result = isInterleave(a, b, c);
    pr("a:", a, "b:", b, "c:", c, "result:", result);
    checkState(result == expected, "expected:", expected);
  }

  private void x(String a, String b, String c) {
    x(a, b, c, slowisInterleave(a, b, c));
  }

  private boolean slowisInterleave(String a, String b, String c) {
    if (a.length() + b.length() != c.length())
      return false;
    if (!sorted(c).equals(sorted(a + b)))
      return false;
    return auxInterleave(a, b, c, 0, 0);
  }

  public boolean isInterleave(String a, String b, String c) {
    if (a.length() + b.length() != c.length())
      return false;
    if (!sorted(c).equals(sorted(a + b)))
      return false;

    memo.clear();
    return auxInterleave(a, b, c, 0, 0);
  }

  private String sorted(String s) {
    var lst = new ArrayList<Character>();
    for (int i = 0; i < s.length(); i++)
      lst.add(s.charAt(i));
    lst.sort(null);
    StringBuilder sb = new StringBuilder(s.length());
    for (char c : lst) {
      sb.append(c);
    }
    return sb.toString();
  }

  private boolean auxInterleave(String a, String b, String c, int acurs, int bcurs) {
    var key = (acurs << 8) | bcurs;
    if (memo.containsKey(key))
      return memo.get(key);

    boolean res = true;
    outer: do {
      boolean adone = acurs == a.length();
      boolean bdone = bcurs == b.length();
      if (adone && bdone)
        break;

      for (int pass = 0; pass < 2; pass++) {
        boolean addA = (pass == 0);
        int newa = acurs;
        if (addA) {
          if (adone)
            continue;
          if (a.charAt(acurs) != c.charAt(acurs + bcurs))
            continue;
          newa++;
        }
        int newb = bcurs;
        if (!addA) {
          if (bdone)
            continue;
          if (b.charAt(bcurs) != c.charAt(acurs + bcurs))
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
