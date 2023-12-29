package js.leetcode;

import static js.base.Tools.*;

import java.util.BitSet;

public class BullsAndCows extends LeetCode {

  public static void main(String[] args) {
    new BullsAndCows().run();
  }

  public void run() {
    x("1807", "7810", "1A3B");
    x("71711112773447", "11112666336664", "2A5B");
  }

  private void x(String secret, String guess, String expected) {

    var result = getHint(secret, guess);
    pr("secret:", secret, "guess:", guess, "hint:", result);
    verify(result, expected);
  }
  // ------------------------------------------------------------------

  public String getHint(String secret, String guess) {
    int bulls = 0;
    int cows = 0;

    int n = secret.length();

    byte[] secretb = new byte[n];
    byte[] guessb = new byte[n];
    for (int i = 0; i < n; i++) {
      secretb[i] = (byte) (secret.charAt(i) - '0');
      guessb[i] = (byte) (guess.charAt(i) - '0');
    }

    var used = new BitSet(n);

    for (int i = 0; i < n; i++) {
      if (secretb[i] == guessb[i]) {
        used.set(i);
        bulls++;
      }
    }

    var guessFreq = freq(guessb, used);
    var secretFreq = freq(secretb, used);
    for (int i = 0; i < 10; i++) {
      var gf = guessFreq[i];
      var sf = secretFreq[i];
      cows += (gf < sf) ? gf : sf;
    }
    var sb = new StringBuilder(20);
    sb.append(bulls);
    sb.append('A');
    sb.append(cows);
    sb.append('B');
    return sb.toString();
  }

  private int[] freq(byte[] s, BitSet used) {
    int[] result = new int[10];
    for (int slot = 0; slot < s.length; slot++) {
      if (used.get(slot))
        continue;
      result[s[slot]]++;
    }
    return result;
  }

}
