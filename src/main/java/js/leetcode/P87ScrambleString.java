package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

// There is a complication if there are duplicate characters in the string.
//
public class P87ScrambleString {

  public static void main(String[] args) {
    new P87ScrambleString().run();
  }

  private void run() {
//    go2("abc", "acb");
//    go2("great", "rgeat");
//    go2("abcde", "caebd");
//    go2("a", "a");
    // This doesn't work with my original algorithm, since there are duplicates
     go2("abcdbdacbdac", "bdacabcdbdac");
     
    go2("ab","aa");
  }

  private void go2(String orig, String scr) {
    pr(CR, orig, CR, scr, isScramble(orig, scr));
  }

  public boolean isScramble(String s1, String s2) {
    // If there are duplicate characters, use the second algorithm
    if (hasDuplicateChars(s1) || hasDuplicateChars(s2))
      return isScramble2(s1, s2);
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
    return auxScramble(dig2, 0, dig2.length);
  }

  private static boolean hasDuplicateChars(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (s.substring(i + 1).indexOf(c) >= 0)
        return true;
    }
    return false;
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

  private Map<String, String> sortedMap = new HashMap<>();
  private StringBuilder sb = new StringBuilder();

  private String sorted(String s) {
    String result = sortedMap.get(s);
    if (result == null) {
      sb.setLength(0);
      sb.append(s);
      for (int i = 0; i < s.length(); i++) {
        char ci = sb.charAt(i);
        for (int j = 0; j < s.length(); j++) {
          char cj = sb.charAt(j);
          if (ci > cj) {
            sb.setCharAt(j, ci);
            sb.setCharAt(i, cj);
            var tmp = ci;
            ci = cj;
            cj = tmp;
          }
        }
      }
      result = sb.toString();
      sortedMap.put(s, result);
    }
    return result;
  }

  private boolean isScramble2(String s1, String s2) {
    
    if (!sorted(s1).equals(sorted(s2))) {
      return false;
    }

    if (s1.length() == 1)
      return true;

    for (int i = 1; i < s1.length(); i++) {

      // Divide both a and b at i
      var s1a = s1.substring(0, i);
      var s1b = s1.substring(i);

      var s2a = s2.substring(0, i);
      var s2b = s2.substring(i);

      if (isScramble2(s1a, s2a) && isScramble(s1b, s2b))
        return true;

      // Simulate effect of swapping at i
      int j = s1.length() - i;
      var s3a = s2.substring(j);
      var s3b = s2.substring(0, j);

      if (isScramble2(s1a, s3a) && isScramble(s1b, s3b))
        return true;
    }

    return false;
  }

}
