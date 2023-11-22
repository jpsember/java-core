
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class P44WildcardMatching {

  public static void main(String[] args) {
    new P44WildcardMatching().run();
  }

  private void run() {
    String[] t = { //
        "aaabbbaabaaaaababaabaaabbabbbbbbbbaabababbabbbaaaaba", "a*******b", "false", //
        "aaabbbaabaaaaababaabaaabbabbbbbbbbaabababbabbbaaaab", "a*******b", "true", //
        "aa", "a", "false", //

        "aa", "*", "true", //
        "cb", "?a", "false", //

        "abbabaaabbabbaababbabbbbbabbbabbbabaaaaababababbbabababaabbababaabbbbbbaaaabababbbaabbbbaabbbbababababbaabbaababaabbbababababbbbaaabbbbbabaaaabbababbbbaababaabbababbbbbababbbabaaaaaaaabbbbbaabaaababaaaabb",
        "**aa*****ba*a*bb**aa*ab****a*aaaaaa***a*aaaa**bbabb*b*b**aaaaaaaaa*a********ba*bbb***a*ba*bb*bb**a*b*bb",
        "false", //
    };

    checkpoint("start");
    for (int i = 0; i < t.length; i += 3) {
      String s = t[i];
      String p = t[i + 1];
      boolean expected = t[i + 2].equals("true");
      boolean result = isMatch(s, p);

      pr("s:", s, "p:", p, result, expected);
      checkState(result == expected);
    }
    checkpoint("stop");
  }

  public boolean isMatch(String s, String p) {
    byte[] sb;
    byte[] pb;
    sb = (s + "!").getBytes();
    pb = (p + "!").getBytes();
    patLength = pb.length + 1;

    stack.clear();
    BitSet visited = new BitSet(sb.length * pb.length);
    stack.add(0);

    while (!stack.isEmpty()) {

      int val = stack.remove(stack.size() - 1);
      if (visited.get(val))
        continue;
      visited.set(val);

      int strCursor = val / patLength;
      int patCursor = val % patLength;

      boolean result = false;
      do {

        byte sc = sb[strCursor];
        byte pc = pb[patCursor];

        if (pc == '!') {
          result = sc == '!';
          break;
        }
        if (pc == '?') {
          if (sc != '!')
            push((strCursor + 1), (patCursor + 1));
          break;
        }
        if (pc == '*') {
          while (pb[patCursor + 1] == '*')
            patCursor++;

          push(strCursor, patCursor + 1);

          if (sc != '!') {
            push(strCursor + 1, patCursor);
          }
          break;
        }
        if (sc == pc) {
          push(strCursor + 1, patCursor + 1);
        }
      } while (false);
      if (result)
        return true;
    }
    return false;
  }

  private void push(int strCursor, int patCursor) {
    stack.add(strCursor * patLength + patCursor);
  }

  private List<Integer> stack = new ArrayList<>();
  private int patLength;
}