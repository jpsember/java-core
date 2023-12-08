
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.base.BasePrinter;

/**
 * Thoughts:
 * 
 * This looks like a straightforward dynamic programming problem, but perhaps
 * the minimum initial health is the wrinkle.
 * 
 * First attempt: for each state, keeping a map of current values => best
 * minimum current value on any path to that state
 * 
 * Now I think a simple array of these values, sorted by current value, is the
 * best approach.
 */
public class P174DungeonGame {

  public static void main(String[] args) {
    loadTools();
    new P174DungeonGame().run();
  }

  private void run() {
    x(3, 3, 7, "[[-2,-3,3],[-5,-10,1],[10,30,-5]]");

  }

  private void x(int w, int h, int expected, String s) {
    while (true) {
      var x = s.length();
      s = s.replace('[', ' ').replace(']', ' ').replace(',', ' ').replace("  ", " ").trim();
      if (s.length() == x)
        break;
    }
    var items = split(s, ' ');
    checkArgument(items.size() == w * h);
    int[][] d = new int[h][w];
    int i = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++, i++) {
        d[y][x] = Integer.parseInt(items.get(i));
      }
    }

    dump(d, 0, 0);

    var result = calculateMinimumHP(d);
    pr("result:", result);

    checkState(result == expected, "expected", expected);

  }

  private static void dump(int[][] matrix, int cx, int cy, Object... prompt) {
    var s = BasePrinter.toString(prompt);
    pr(s);
    int y = -1;
    for (var row : matrix) {
      y++;
      var sb = new StringBuilder();
      int x = -1;
      for (var c : row) {
        x++;
        var v = Integer.toString(c);
        sb.append(spaces(3 - v.length()));
        sb.append(v);
        if (y == cy && x == cx)
          sb.append("*");
        tab(sb, (x + 1) * 4);
      }
      pr(sb);
    }
  }

  // ------------------------------------------------------------------

  public int calculateMinimumHP(int[][] dungeon) {
    int dw = dungeon[0].length;
    int dh = dungeon.length;

    State[][] stats = new State[dh][dw];

    for (int s = 0; s < dw + dh - 1; s++) {
      int x = s;
      int y = 0;
      if (s >= dw) {
        x = dw - 1;
        y = s - (dw - 1);
      }
      while (x >= 0 && y < dh) {

        dump(dungeon, x, y);

        var val = dungeon[y][x];

        var state = new State();

        State left = null;
        State up = null;
        if (x > 0)
          left = stats[y][x - 1];
        if (y > 0)
          up = stats[y - 1][x];
        if (x == 0 && y == 0) {
          state.add(val, val);
        } else {
          if (left != null) {
            state.merge(left, val);
          }
          if (up != null)
            state.merge(up, val);
        }
        stats[y][x] = state;
        pr("stored state:", state);
        x--;
        y++;

      }
    }

    var last = stats[dh - 1][dw - 1];
    Integer bestMin = null;
    for (var ent : last.valuePairs) {
      var min = ent.minimumValue;
      if (bestMin == null || min > bestMin)
        bestMin = min;
    }
    return Math.max(1, 1 - bestMin);
  }

  private static class ValuePair {
    ValuePair(int currentValue, int minValue) {
      this.currentValue = currentValue;
      this.minimumValue = minValue;
    }

    ValuePair extend(int cost) {
      return new ValuePair(currentValue + cost, Math.min(minimumValue, currentValue + cost));
    }

    boolean dominates(ValuePair other) {
      return currentValue >= other.currentValue && minimumValue >= other.minimumValue;
    }

    @Override
    public String toString() {
      return "(c" + currentValue + " m" + minimumValue + ")";
    }

    int currentValue;
    int minimumValue;
  }

  private static class State {

    void add(int currentValue, int minValue) {
      valuePairs.add(new ValuePair(currentValue, minValue));
    }

    /**
     * Extend a previous state's values to add the cost of an edge to get to
     * this state, and merge the resulting ValuePairs into this states'.
     */
    void merge(State prevState, int edgeCost) {

      int prevCursor = 0;
      var prevVals = prevState.valuePairs;

      var currVals = valuePairs;
      var currCursor = 0;

      var merged = new ArrayList<ValuePair>(prevVals.size() + currVals.size());

      ValuePair srcValue = null;
      ValuePair destValue = null;

      int origVals = currVals.size();

      while (true) {
        if (srcValue == null && prevCursor != prevVals.size())
          srcValue = prevVals.get(prevCursor++).extend(edgeCost);

        if (destValue == null && currCursor < currVals.size())
          destValue = currVals.get(currCursor++);

        if (srcValue != null) {
          if (destValue != null) {
            if (srcValue.dominates(destValue)) {
              destValue = null;
            } else if (destValue.dominates(srcValue)) {
              srcValue = null;
            } else if (destValue.currentValue < srcValue.currentValue) {
              merged.add(destValue);
              destValue = null;
            } else {
              merged.add(srcValue);
              srcValue = null;
            }
          } else {
            merged.add(srcValue);
            srcValue = null;
          }
        } else if (destValue != null) {
          merged.add(destValue);
          destValue = null;

        } else
          break;
      }

      valuePairs = merged;

      pr("...merge for cost:", edgeCost, "prev vals:", prevVals.size(), "orig src vals:", origVals, "merged:",
          merged.size());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      pr("State, values:");
      for (var v : valuePairs) {
        sb.append(' ');
        sb.append(v);
      }
      return sb.toString();
    }

    // Pairs of current value, historical min value
    List<ValuePair> valuePairs = new ArrayList<>();
  }

}
