
package js.leetcode;

import static js.base.Tools.*;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

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
    sb = (s + "!").getBytes(cs);
    pb = (p + "!").getBytes(cs);

    visitedStates.clear();
    return auxMatch(0, 0);
  }

  private boolean auxMatch(int strCursor, int patCursor) {

    if (!visitedStates.add(strCursor | (patCursor << 11)))
      return false;

    byte sc = sb[strCursor];
    byte pc = pb[patCursor];

    if (pc == '!')
      return sc == '!';
    if (pc == '?')
      return sc != '!' && auxMatch(strCursor + 1, patCursor + 1);
    if (pc == '*') {
      while (pb[patCursor + 1] == '*')
        patCursor++;
      return (sc != '!' && auxMatch(strCursor + 1, patCursor)) || auxMatch(strCursor, patCursor + 1);
    }
    return (sc == pc) && auxMatch(strCursor + 1, patCursor + 1);
  }

  private Set<Integer> visitedStates = new HashSet<>();

  private byte[] sb;
  private byte[] pb;
  private static Charset cs = Charset.forName("US-ASCII");

}