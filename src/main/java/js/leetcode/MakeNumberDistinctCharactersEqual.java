package js.leetcode;

import static js.base.Tools.*;

import java.util.HashSet;
import java.util.Set;

public class MakeNumberDistinctCharactersEqual extends LeetCode {

  public static void main(String[] args) {
    new MakeNumberDistinctCharactersEqual().run();
  }

  public void run() {
    x("az", "a", true);
    x("aa", "bcd", false);

    x("aa", "bb", true);

    x("zzzzzyyyywvvwwwxxxy", "abbbbaaaacccceee", false);

    x("ac", "b", false);
    x("abcc", "aab", true);
    x("abcde", "fghij", true);
  }

  private void x(String word1, String word2, boolean expected) {
    db = Math.max(word1.length(), word2.length()) <= 20;
    var res = isItPossible(word1, word2);
    pr(word1, word2, res);
    verify(res, expected);
  }

  public boolean isItPossible(String word1, String word2) {
    var a = prep(word1);
    var b = prep(word2);

    var ac = count(a);
    var bc = count(b);

    db("a:", word1, "counts:", toStr(a), "sum:", ac);
    db("b:", word2, "counts:", toStr(b), "sum:", bc);
    var inf = Integer.MAX_VALUE;
    int diff = bc - ac;
    if (diff < 0) {
      var tmp = b;
      b = a;
      a = tmp;
      diff = -diff;
    }

    var result = false;

    db("diff:", diff);

    switch (diff) {

    case 2: {
      var left = match(a, b, 2, inf, 1, inf);
      var right = match(a, b, 0, 0, 1, 1);
      removeCommon(left, right);
      result = nonEmpty(left, right);
    }
      break;

    case 1: {
      {
        var left = match(a, b, 2, inf, 0, 0);
        var right = match(a, b, 0, 0, 1, 1);
        removeCommon(left, right);
        result = nonEmpty(left, right);
      }
      if (!result) {
        var left = match(a, b, 2, inf, 1, inf);
        var right = match(a, b, 0, 0, 2, inf);
        removeCommon(left, right);
        result = nonEmpty(left, right);
      }
      if (!result) {
        var left = match(a, b, 1, 1, 1, inf);
        var right = match(a, b, 0, 0, 1, 1);
        removeCommon(left, right);
        result = nonEmpty(left, right);

      }

    }
      break;
    case 0: {
      db("unique singles on each side?");
      var left = match(a, b, 1, 1, 0, 0);
      var right = match(a, b, 0, 0, 1, 1);
      removeCommon(left, right);
      result = nonEmpty(left, right);
    }

      if (!result) {
        db("increment both?");
        var left = match(a, b, 2, inf, 0, 0);
        var right = match(a, b, 0, 0, 2, inf);
        removeCommon(left, right);
        result = nonEmpty(left, right);
      }
      if (!result) {
        db("decrement both?");
        var left = match(a, b, 1, 1, 1, inf);
        var right = match(a, b, 1, inf, 1, 1);
        removeCommon(left, right);
        result = nonEmpty(left, right);
      }
      if (!result) {
        db("swap same chars?");
        var left = match(a, b, 1, inf, 1, inf);
        var right = match(a, b, 1, inf, 1, inf);
        result = hasCommon(left, right);
      }
      if (!result) {
        db("swap multiple with multiple?");
        var left = match(a, b, 2, inf, 1, inf);
        var right = match(a, b, 2, inf, 1, inf);
        removeCommon(left, right);
        result = nonEmpty(left, right);
      }
      break;
    }

    return result;
  }

  private int[] prep(String word) {
    var result = new int[26];
    for (int i = 0; i < word.length(); i++) {
      result[word.charAt(i) - 'a']++;
    }
    return result;
  }

  private int count(int[] freq) {
    int a = 0;
    for (var c : freq) {
      if (c != 0)
        a++;
    }
    return a;
  }

  private Set<Integer> match(int[] a, int[] b, int aMin, int aMax, int bMin, int bMax) {
    var result = new HashSet<Integer>();
    for (int i = 0; i < 26; i++) {
      if (a[i] >= aMin && a[i] <= aMax && b[i] >= bMin && b[i] <= bMax) {
        result.add(i);
      }
    }
    return result;
  }

  private Set<Integer> setDiff(Set<Integer> a, Set<Integer> b) {
    Set<Integer> diff = new HashSet<Integer>();
    diff.addAll(a);
    diff.removeAll(b);
    return diff;
  }

  private void removeCommon(Set<Integer> a, Set<Integer> b) {
    Set<Integer> diff1 = setDiff(a, b);
    Set<Integer> diff2 = setDiff(b, a);
    a.clear();
    a.addAll(diff1);
    b.clear();
    b.addAll(diff2);
  }

  private boolean nonEmpty(Set<Integer> a, Set<Integer> b) {
    return !a.isEmpty() && !b.isEmpty();
  }

  private boolean hasCommon(Set<Integer> a, Set<Integer> b) {
    return !setDiff(a, b).isEmpty() || !setDiff(b, a).isEmpty();
  }
}
