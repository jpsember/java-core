// 22. Generate Parenthesis

// Insight: what is a grammar for such parenthesis?

//

package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

public class P22GenerateParenthesis {

  public static void main(String[] args) {
    new P22GenerateParenthesis().run();
  }

  private void run() {
    pr(generateParenthesis(3));
  }

  public List<String> generateParenthesis(int n) {

    List<String> results = new ArrayList<>();

    var sb = new StringBuilder(n * 2);
    int expected = 1 << (n-1);
    for (int i = 0; i < expected; i++) {
      int tailCount = 0;
      sb.setLength(0);
      var choices = i;
      int j = 0;
      while (j < n) {
        if (tailCount != 0) {
          boolean flag = (choices & 1) != 0;
          choices >>= 1;
          if (flag) {
            tailCount--;
            sb.append(')');
            continue;
          }
        }
        sb.append('(');
        tailCount++;
        j++;
      }
      while (tailCount-- != 0)
        sb.append(')');
      results.add(sb.toString());
    }
    return results;
  }


}
