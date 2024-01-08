package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

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

    int ca = 0;
    int cb = 0;
    while (true) {
      if (ca < a.length) {
        
      } else if (cb < b.length) {
      } else
        break;
    }

    return false;
  }

  private byte[] prep(String word) {
    var b = word.getBytes();
    Arrays.sort(b);
    var c = new byte[b.length];
    int i = 0;
    int j = 0;
    byte prev = b[0];
    int count = 0;
    while (i < b.length) {
      var x = b[i++];
      if (x != prev) {
        c[j++] = prev;
        if (count >= 2)
          c[j++] = prev;
        prev = x;
        count = 0;
      }
      count++;
    }
    if (count > 0) {
      c[j++] = prev;
      if (count >= 2)
        c[j++] = prev;
    }
    var ret = Arrays.copyOf(c, j);

    db("prepare", word, "=>", new String(ret));
    return ret;
  }

}
