
package js.leetcode;

// 32. Longest Valid Parentheses
//
// Uses dynamic programming.  But there must be a better way as it is taking way too long. (Or I can optimize it somehow)
//

import static js.base.Tools.*;

import java.util.ArrayList;

public class P32LongestValidParentheses {

  public static void main(String[] args) {
    new P32LongestValidParentheses().run();
  }

  private void run() {
         x(")()())");
    
    // It takes a while, but it finds length 168 for this:
    // x("((())())(()))(()()(()(()))(()((((()))))))((()())()))()()(()(((((()()()())))()())(()()))((((((())))((()))()()))))(()))())))()))()())((()()))))(()(((((())))))()((()(()(())((((())(())((()()(()())))())(()(())()()))())(()()()))()(((()())(((()()())))(((()()()))(()()))()))()))))))())()()((()(())(()))()((()()()((())))()(((()())(()))())())))(((()))))())))()(())))()())))())()((()))((()))()))(((())((()()()(()((()((())))((()()))())(()()(()))))())((())))(()))()))))))()(()))())(()())))))(()))((())(()((())(((((()()()(()()())))(()())()((()(()()))(()(())((()((()))))))))(()(())()())()(()(()(()))()()()(()()())))(())(()((((()()))())))(())((()(())())))))())()()))(((())))())((()(()))(()()))((())(())))))(()(()((()((()()))))))(()()()(()()()(()(())()))()))(((()(())()())(()))())))(((()))())(()((()))(()((()()()(())()(()())()(())(()(()((((())()))(((()()(((()())(()()()(())()())())(()(()()((()))))()(()))))(((())))()()))(()))((()))))()()))))((((()(())()()()((()))((()))())())(()((()()())))))))()))(((()))))))(()())))(((()))((()))())))(((()(((())))())(()))))(((()(((((((((((((())(((()))((((())())()))())((((())(((())))())(((()))))()())()(())())(()))))()))()()()))(((((())()()((()))())(()))()()(()()))(())(()()))()))))(((())))))((()()(()()()()((())((((())())))))((((((()((()((())())(()((()))(()())())())(()(())(())(()((())((())))(())())))(()()())((((()))))((()(())(()(()())))))))))((()())()()))((()(((()((()))(((((()()()()()(()(()((()(()))(()(()((()()))))()(()()((((((()((()())()))((())()()(((((()(()))))()()((()())((()())()(())((()))()()(()))");
     x("(()");
  }

  private void x(String s) {
    pr(VERT_SP, "string:", s);
    int len = longestValidParentheses(s);
    pr("valid length:", len);
  }

  public int longestValidParentheses(String s) {
    var stack = new ArrayList<Integer>();

    int best = 0;
    int validLength = 0;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      pr("i:",i,"char:",c,"valid:",validLength,"best:",best,"stack:",stack);
      
      if (c == '(') {
        stack.add(validLength);
        validLength = 0;
      } else {
        if (!stack.isEmpty()) {
          validLength += stack.remove(stack.size() - 1) + 2; // add 2 for the opening and closing paren
          pr("...valid now",validLength);
          if (validLength > best) {
            best = validLength;
            pr("......new best");
          }
        } else {
          validLength = 0;
          pr("...reset valid");
        }
      }
    }
    return best;
  }

}
