
package js.leetcode;

// 32. Longest Valid Parentheses
//

import static js.base.Tools.*;
import java.util.BitSet;

public class P32LongestValidParentheses {

  public static void main(String[] args) {
    new P32LongestValidParentheses().run();
  }

  private void run() {
//    x(")()())");
    x(")()())(()())())(");
  }

  private void x(String s) {
    pr(VERT_SP, "string:", s);
    int len = longestValidParentheses(s);
    pr("len:", len);
  }

  public int longestValidParentheses(String s) {
    stringLength = s.length();
    string = s;
    validFlags = new BitSet(stringLength * stringLength);

    int longest = 0;

    for (int y = 1; y <= stringLength; y++) {

      pr(INDENT, "y:", y);
      for (int x = 0; x < stringLength; x++) {
        pr("x:", x);
        if (y >= 2) {
          // Apply the wrap rule
          if (valid(x + 1, y - 2) && charIs(x, '(') && charIs(x + y - 1, ')')) {
            setValid(x, y);
            longest = y;
            pr("...wrap rule applied");
            continue;
          }
        }

        // Apply the concatenate rule
       // pr("concat rule, y:",y);
        for (int k = 2; k <= y - 2; k++) {
//          boolean dump = false && (y == 4);
          // If there are adjacent valid substrings
          // of length [k] [y-k] (positioned at x-k, k and x, y-k respectively)
          //
          // then the substring [y] at x-k is valid.

//          if (dump) {
//            pr("k:", k, "valid ", x - k, k, ":", valid(x - k, k));
//            pr("valid ", x, y - k, ":", valid(x, y - k));
//          }
          if (// 
          valid(x - k, k) //
              && valid(x, y - k)) {
            setValid(x-k, y);
            longest = y;
            pr("...concatenate rule applied");
          }
        }
      }

      for (int i = 1; i <= y; i++)
        dumpRow(i, i == 1);
    }
    return longest;
  }

  private void dumpRow(int row, boolean withString) {
    StringBuilder sb = new StringBuilder();

    if (withString) {
      sb.append("  ");
      for (int i = 0; i < string.length(); i++) {
        sb.append(i == 0 ? '|' : '|');
        sb.append(" ");
        sb.append(string.charAt(i));
        sb.append(" ");
      }
      sb.append("|\n");
    }
    sb.append(row);
    sb.append(" ");
    for (int i = 0; i < string.length(); i++) {
      sb.append(i == 0 ? '|' : ' ');
      sb.append(" ");
      if (valid(i, row))
        sb.append('Y');
      else
        sb.append(' ');
      sb.append(" ");
    }
    sb.append("|");
    pr(sb.toString());
  }

  private int bitIndex(int x, int y) {
    return (y - 1) * stringLength + x;
  }

  private boolean valid(int x, int y) {
    if (x < 0 || x >= stringLength)
      return false;
    if (y == 0)
      return true;
    return validFlags.get(bitIndex(x, y));
  }

  private void setValid(int x, int y) {
    validFlags.set(bitIndex(x, y));
  }

  private boolean charIs(int pos, char value) {
    if (pos < 0 || pos >= string.length())
      return false;
    return string.charAt(pos) == value;
  }

  // These instance fields would be refactored in 'real' code
  private BitSet validFlags;
  private String string;
  private int stringLength;
}
