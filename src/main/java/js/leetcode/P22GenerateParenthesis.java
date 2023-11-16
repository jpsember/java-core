package js.leetcode;

//22. Generate Parenthesis

// Insight: what is a grammar for such parenthesis?
// ...that wasn't productive, but worth further research.
//
// The approach I figured out was a nested loop, one for each opening char '('.  
// 
// set up cursors
// while true
//   generate string based on cursor positions
//   advance the last cursor; if it has reached its limit, move back to the previous cursor
//    and repeat.  If we run out of cursors, stop.
//   initialize next cursors (if we moved back in the above step) to be the previous cursor + 1
// 

// I am constructing the strings from byte arrays to be slightly quicker than using StringBuilder.

import static js.base.Tools.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P22GenerateParenthesis {

  public static void main(String[] args) {
    new P22GenerateParenthesis().run();
  }

  private void run() {
    int n = 5;
    var res = generateParenthesis(n);
    pr(res);
    pr("n:", n, "len:", res.size());
  }

  public List<String> generateParenthesis(int n) {

    List<String> results = new ArrayList<>();

    int[] cursors = new int[n];

    for (int i = 0; i < n; i++) {
      cursors[i] = i;
    }

    byte[] str = new byte[n * 2];

    outer: while (true) {
      Arrays.fill(str, (byte) ')');
      for (int c : cursors) {
        str[c] = (byte) '(';
      }
      results.add(new String(str, StandardCharsets.US_ASCII));

      int slot = n - 1;
      while (true) {
        // Increment current cursor.
        // If reached limit, move back to the previous cursor and repeat.
        cursors[slot]++;
        if (cursors[slot] < 2 * slot + 1)
          break;

        slot--;
        // If we've run out of cursors, we're done.
        if (slot < 0)
          break outer;
      }

      while (++slot < n) {
        cursors[slot] = cursors[slot - 1] + 1;
      }
    }

    return results;
  }

}
