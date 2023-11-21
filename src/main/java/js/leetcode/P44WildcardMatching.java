
package js.leetcode;

import static js.base.Tools.*;

import java.util.HashSet;
import java.util.Set;

public class P44WildcardMatching {

  public static void main(String[] args) {
    new P44WildcardMatching().run();
  }

  private void run() {
    String[] t = { //
        "aa", "a", "false", //

        "aa", "*", "true", //
        "cb", "?a", "false", //
        "aaabbbaabaaaaababaabaaabbabbbbbbbbaabababbabbbaaaaba", "a*******b", "false", //
        "aaabbbaabaaaaababaabaaabbabbbbbbbbaabababbabbbaaaab", "a*******b", "true", //

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
    p = p.replace("**", "*");
    visitedStates.clear();
    return auxMatch(s + "!", p + "!", 0, 0);
  }

  private boolean auxMatch(String s, String p, int strCursor, int patCursor) {

    if (!visitedStates.add(strCursor | (patCursor << 11)))
      return false;

    char sc = s.charAt(strCursor);
    char pc = p.charAt(patCursor);

    switch (pc) {
    case '!':
      return sc == '!';
    case '?':
      if (sc != '!') {
        return auxMatch(s, p, strCursor + 1, patCursor + 1);
      }
      return false;
    case '*':
      if (auxMatch(s, p, strCursor, patCursor + 1))
        return true;
      if (sc != '!') {
        return auxMatch(s, p, strCursor + 1, patCursor);
      }
      return false;
    default:
      if (sc == pc)
        return auxMatch(s, p, strCursor + 1, patCursor + 1);
      return false;
    }
  }

  private Set<Integer> visitedStates = new HashSet<>();

}