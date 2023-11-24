
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class P38CountAndSay {

  public static void main(String[] args) {
    new P38CountAndSay().run();
  }

  private void run() {
    x(1);
    x(4);
    x(5);
  }

  private void x(int n) {
    var result = countAndSay(n);
    pr("n:", n, "result:", result);
  }

  public String countAndSay(int n) {
    //pr("countAndSay, n:", n);
    answers[0] = "1";
    int nIndex = n-1;
    String res = answers[nIndex];
    if (res != null) {
      //pr("result:", res);
      return res;
    }

    String aux = countAndSay(n - 1);

    //pr("aux value:", aux);
    StringBuilder sb = new StringBuilder();
    int j = 0;
    while (j < aux.length()) {
      char c = aux.charAt(j);
      int k = 1;
      while (j + k < aux.length() && aux.charAt(j + k) == c) {
        k++;
      }
     // pr("j:", j, "c:", Character.toString(c), "k:", k);
      sb.append(Integer.toString(k));
      sb.append(c);
      j += k;
    }
    res = sb.toString();
    answers[nIndex] = res;
    return res;
  }

  private static final int N_MAX = 30;
  private static String[] answers = new String[N_MAX + 1];

}