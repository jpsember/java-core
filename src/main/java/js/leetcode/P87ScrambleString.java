package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// There is a complication if there are duplicate characters in the string.
//
public class P87ScrambleString {

  public static void main(String[] args) {
    new P87ScrambleString().run();
  }

  private void run() {
    go2("great", "rgeat");
    go2("abcde", "caebd");
    go2("a", "a");
    // This doesn't work, since there are duplicates
    go2("abcdbdacbdac","bdacabcdbdac");
  }

  private void go2(String orig, String scr) {
    pr(orig, CR, scr, INDENT, isScramble(orig, scr));
  }

  public boolean isScramble(String s1, String s2) {
    // Define a mapping over the characters to convert them to their position within the string
    Map<Character, Integer> m = new HashMap<>();
    int[] dig1 = new int[s1.length()];
    int[] dig2 = new int[s2.length()];
    for (int i = 0; i < s1.length(); i++) {
      m.put(s1.charAt(i), i + 1);
      dig1[i] = i;
    }
    for (int i = 0; i < s2.length(); i++) {
      Integer j = m.get(s2.charAt(i));
      if (j == null)
        return false;
      dig2[i] = j;
    }

    //    pr("dig1:", dig1);
    //    pr("dig2:", dig2);

    return auxScramble(dig2, 0, dig2.length);
  }

  private boolean less(MinMax a, MinMax b, boolean fwd) {
    if (fwd) {
      return a.max < b.min;
    } else {
      return b.max < a.min;
    }
  }

  private boolean auxScramble(int[] s, int i0, int i1) {
    // pr("auxScramble", Arrays.copyOfRange(s, i0, i1), i0, i1);

    int len = i1 - i0;
    if (len <= 2)
      return true;

    int j = i0;
    int k = i1 - 1;

    boolean fwd = s[j] < s[k];

    var jM = new MinMax(s[j]);
    var kM = new MinMax(s[k]);

    while (true) {
      //  pr("j:", j, "k:", k, "jM:", jM, "kM:", kM);

      if (j + 1 == k)
        break;

      var jM2 = jM.add(s[j + 1]);
      if (less(jM2, kM, fwd)) {
        jM = jM2;
        j++;
        continue;
      }

      var kM2 = kM.add(s[k - 1]);
      if (less(jM, kM2, fwd)) {
        kM = kM2;
        k--;
        continue;
      }

      return false;
    }
    return auxScramble(s, i0, k) && auxScramble(s, k, i1);
  }

  private static class MinMax {
    int min;
    int max;

    MinMax(int... vals) {
      for (int i = 0; i < vals.length; i++) {
        int v = vals[i];
        if (i == 0) {
          min = v;
          max = v;
        } else {
          if (min > v)
            min = v;
          if (max < v)
            max = v;
        }
      }
    }

    MinMax add(int val) {
      return new MinMax(min, max, val);
    }

    @Override
    public String toString() {
      return "{min:" + min + " max:" + max + "}";
    }

  }
}
