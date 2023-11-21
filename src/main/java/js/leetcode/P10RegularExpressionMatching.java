
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Beats 92.90% runtime, 27% memory
//
// Uses a linked list to solve the problem in linear time.
//
public class P10RegularExpressionMatching {

  public static void main(String[] args) {
    new P10RegularExpressionMatching().run();
  }

  public boolean isMatch(String s, String p) {

    final char END_OF_STRING = '!';
    final char END_OF_PATTERN = '&';

    Set<Integer> stateMap = new HashSet<>();
    List<State> stack = new ArrayList<>();

    var state = new State();
    addState(state, stateMap, stack);

    while (!stack.isEmpty()) {
      state = stack.get(stack.size() - 1);
      stack.remove(stack.size() - 1);
      if (state.patCursor == p.length() && state.strCursor == s.length())
        return true;

      char patChar = END_OF_PATTERN;
      if (state.patCursor < p.length())
        patChar = p.charAt(state.patCursor);

      char strChar = END_OF_STRING;
      if (state.strCursor != s.length()) {
        strChar = s.charAt(state.strCursor);
      }

      int subPatternLength = 1;
      // Determine possible next states
      if (state.patCursor + 2 <= p.length() && p.charAt(state.patCursor + 1) == '*') {
        subPatternLength = 2;
        // Add state for not consuming any input, but consuming the subpattern
        addState(state.adjustCursors(0, subPatternLength), stateMap, stack);

        // If next string character exists, and matches the pattern, add state for consuming input (but not the subpattern)
        if (strChar != END_OF_STRING) {
          if (strChar == patChar || patChar == '.') {
            addState(state.adjustCursors(1, 0), stateMap, stack);
          }
        }
      } else if (strChar != END_OF_STRING) {
        // If input char matches pattern char, add state for consuming both input and subpattern
        if (patChar == '.' || patChar == strChar) {
          addState(state.adjustCursors(1, subPatternLength), stateMap, stack);
        }
      }
    }
    return false;
  }

  private class State {
    int strCursor;
    int patCursor;

    public State adjustCursors(int strAdjust, int patAdjust) {
      var s = new State();
      s.strCursor = strCursor + strAdjust;
      s.patCursor = patCursor + patAdjust;
      return s;
    }

  }

  private boolean addState(State state, Set<Integer> stateSet, List<State> stack) {
    var key = (state.patCursor << 16) | state.strCursor;
    if (stateSet.add(key)) {
      stack.add(state);
      return true;
    }
    return false;
  }

  private void run() {
    String[] t = { //
        "a", ".*..a*", //

        "aa", "a", //
        "aa", "a*", //
        "ab", ".*", //
        "abc", "a*c",//
    };

    for (int i = 0; i < t.length; i += 2) {
      String s = t[i];
      String p = t[i + 1];
      pr(VERT_SP);
      pr("s:", s);
      pr("p:", p);
      pr(isMatch(s, p));
    }
  }
}