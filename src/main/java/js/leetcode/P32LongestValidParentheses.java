
package js.leetcode;

// 32. Longest Valid Parentheses
//
// Uses a stack to store length of valid substrings preceding an opening paren '('.
//
// Runs in linear time.
//

import static js.base.Tools.*;

public class P32LongestValidParentheses {

  public static void main(String[] args) {
    new P32LongestValidParentheses().run();
  }

  private void run() {
    x(")()())");
    x("((())())(()))(()()(()(()))(()((((()))))))((()())()))()()(()(((((()()()())))()())(()()))((((((())))((()))()()))))(()))())))()))()())((()()))))(()(((((())))))()((()(()(())((((())(())((()()(()())))())(()(())()()))())(()()()))()(((()())(((()()())))(((()()()))(()()))()))()))))))())()()((()(())(()))()((()()()((())))()(((()())(()))())())))(((()))))())))()(())))()())))())()((()))((()))()))(((())((()()()(()((()((())))((()()))())(()()(()))))())((())))(()))()))))))()(()))())(()())))))(()))((())(()((())(((((()()()(()()())))(()())()((()(()()))(()(())((()((()))))))))(()(())()())()(()(()(()))()()()(()()())))(())(()((((()()))())))(())((()(())())))))())()()))(((())))())((()(()))(()()))((())(())))))(()(()((()((()()))))))(()()()(()()()(()(())()))()))(((()(())()())(()))())))(((()))())(()((()))(()((()()()(())()(()())()(())(()(()((((())()))(((()()(((()())(()()()(())()())())(()(()()((()))))()(()))))(((())))()()))(()))((()))))()()))))((((()(())()()()((()))((()))())())(()((()()())))))))()))(((()))))))(()())))(((()))((()))())))(((()(((())))())(()))))(((()(((((((((((((())(((()))((((())())()))())((((())(((())))())(((()))))()())()(())())(()))))()))()()()))(((((())()()((()))())(()))()()(()()))(())(()()))()))))(((())))))((()()(()()()()((())((((())())))))((((((()((()((())())(()((()))(()())())())(()(())(())(()((())((())))(())())))(()()())((((()))))((()(())(()(()())))))))))((()())()()))((()(((()((()))(((((()()()()()(()(()((()(()))(()(()((()()))))()(()()((((((()((()())()))((())()()(((((()(()))))()()((()())((()())()(())((()))()()(()))");
    x("(()");
  }

  private void x(String s) {
    pr(VERT_SP, "string:", s);
    int len = longestValidParentheses(s);
    pr("valid length:", len);
  }

  public int longestValidParentheses(String s) {
    int[] stack = new int[s.length()];
    int sp = 0;

    int bestValidLength = 0;
    int validLength = 0;

    for (int i = 0; i < s.length(); i++) {

      if (s.charAt(i) == '(') {
        stack[sp++] = validLength;
        validLength = 0;
      } else {

        if (sp != 0) {

          // We have scanned a valid substring q = "(....)" , so update the valid length to be
          // [valid length preceding q]   +    1    +    validLength     +     1
          //    ^ on the stack               '('          "...."              ')'

          validLength += stack[--sp] + 2;

          if (validLength > bestValidLength)
            bestValidLength = validLength;

        } else {
          // Encountered ')' that has no matching '(' before it
          validLength = 0;
        }
      }
    }
    return bestValidLength;
  }

}