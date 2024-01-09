package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MakeNumberDistinctCharactersEqual extends LeetCode {

  public static void main(String[] args) {
    new MakeNumberDistinctCharactersEqual().run();
  }

  public void run() {
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

    int af = 0;
    int bf = 0;
    int SINGLE = 1 << 0;
    int DOUBLE = 1 << 1;
    int SINGLE_MATCH = 1 << 2;
    int DOUBLE_MATCH = 1 << 3;
    int MULT_DIFFER = 1 << 4;
    int ca = 0;
    int cb = 0;

    List<State> fa = new ArrayList<>();
    List<State> fb = new ArrayList<>();

    while (true) {
      State sa = null;
      State sb = null;
      if (ca < a.length)
        sa = a[ca];
      if (cb < b.length)
        sb = b[cb];

      if (sa == null && sb == null)
        break;

      if (sb == null || sa.letter < sb.letter) {
        var flag = (sa.isDouble() ? DOUBLE : SINGLE);
        if ((af & flag) == 0) {
          af |= flag;
          fa.add(sa);
        }
        ca++;
      } else if (sa == null || sb.letter < sa.letter) {
        int flag = (sb.isDouble() ? DOUBLE : SINGLE);
        if ((bf & flag) == 0) {
          bf |= flag;
          fb.add(sb);
        }
        cb++;
      } else {
        var flag = sa.isDouble() == sb.isDouble() ? (sa.isDouble() ? DOUBLE_MATCH : SINGLE_MATCH)
            : MULT_DIFFER;
        if ((af & flag) == 0) {
          af |= flag;
          bf |= flag;
          fa.add(sa);
          fb.add(sb);
        }
        ca++;
        cb++;
      }

    }

    pr("fa states:", fa);
    pr("fb states:", fb);

    return false;
  }

  private State[] prep(String word) {
    var result = new State[word.length()];
    var rc = 0;

    var b = word.getBytes();
    Arrays.sort(b);
    int i = 0;

    var s = new State(b[0], 0);
    result[rc++] = s;
    while (i < b.length) {
      var x = b[i++];
      if (x != s.letter) {
        s = new State(x, 1);
        result[rc++] = s;
      } else
        s.multiplicity++;
    }

    var ret = Arrays.copyOf(result, rc);

    db("prepare", word, "=>", toStr(ret));
    return ret;
  }

  private static class State {
    final int letter;
    int multiplicity;

    State(int letter, int multiplicity) {
      this.letter = letter;
      this.multiplicity = multiplicity;
    }

    boolean isDouble() {
      return multiplicity >= 2;
    }

    @Override
    public String toString() {
      var c = Character.toString(letter);
      if (multiplicity == 1)
        return c;
      if (multiplicity == 0)
        return "!" + c + "x0!";
      return c + c;
    }

  }

}
