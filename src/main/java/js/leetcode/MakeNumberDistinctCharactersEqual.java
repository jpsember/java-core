package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

/**
 * My approach is not satisfying and has a bunch of special cases.
 * 
 * Simplified the algorithm to do a double loop over all possible swaps (igoring
 * multiple copies)
 */
public class MakeNumberDistinctCharactersEqual extends LeetCode {

  public static void main(String[] args) {
    new MakeNumberDistinctCharactersEqual().run();
  }

  public void run() {
    x("eeee", "eeee", true);

    x("az", "a", true);
    x("aa", "bcd", false);

    x("aa", "bb", true);

    x("zzzzzyyyywvvwwwxxxy", "abbbbaaaacccceee", false);

    x("ac", "b", false);
    x("abcc", "aab", true);
    x("abcde", "fghij", true);
    x("fafdd", "fbdc", true);
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

    var aind = indices(a);
    var bind = indices(b);

    for (var ai : aind) {
      for (var bi : bind) {

        int deltaA = 0;
        int deltaB = 0;
        if (a[ai] == 1)
          deltaA--;
        if (a[bi] == 0)
          deltaA++;
        if (b[bi] == 1)
          deltaB--;
        if (b[ai] == 0)
          deltaB++;
        if (ac + deltaA == bc + deltaB)
          return true;
      }
    }

    return false;
  }

  private int[] indices(int[] freq) {
    int[] res = new int[26];
    int c = 0;
    for (int i = 0; i < 26; i++)
      if (freq[i] != 0) {
        res[c++] = i;
      }
    return Arrays.copyOf(res, c);
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

}
