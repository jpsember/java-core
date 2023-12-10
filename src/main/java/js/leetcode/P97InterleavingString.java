package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Random;

public class P97InterleavingString {

  public static void main(String[] args) {
    new P97InterleavingString().run();
  }

  private void run() {
    x("abc", "def", "abdcef");
    x("abc", "def", "abxcef");
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

  private void x(String a, String b, String c) {
    var result = isInterleave(a, b, c);
    pr("a:", a, "b:", b, "c:", c, "result:", result);
    boolean expected = slowisInterleave(a, b, c);
    checkState(result == expected, "expected:", expected);
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
    boolean adone = acurs == a.length();
    boolean bdone = bcurs == b.length();
    if (adone && bdone)
      return true;

    if (!adone && a.charAt(acurs) == c.charAt(acurs + bcurs)) {
      if (auxInterleave(a, b, c, acurs + 1, bcurs))
        return true;
    }
    if (!bdone && b.charAt(bcurs) == c.charAt(acurs + bcurs)) {
      if (auxInterleave(a, b, c, acurs, bcurs + 1))
        return true;
    }
    return false;
  }
}
