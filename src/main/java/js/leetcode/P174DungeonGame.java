
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

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
 * 
 * This works, but it is 20 times slower than the others... ...now I am thinking
 * a breadth-first search to minimize the cost?
 * 
 * 
 */
public class P174DungeonGame {

  public static void main(String[] args) {
    loadTools();
    new P174DungeonGame().run();
  }

  private void run() {
    if (false)
      x(3, "0 0 0 -20 x 0 40 0 0 x x 0 x x -30 x x 0");
    xr(1965, 200, 200);

    // x(  3, "[[-2,-3,3],[-5,-10,1],[10,30,-5]]");
  }

  private void x(int w, String s) {
    while (true) {
      var x = s.length();
      s = s.replace('[', ' ').replace(']', ' ').replace(',', ' ').replace("  ", " ").trim();
      if (s.length() == x)
        break;
    }
    var items = split(s, ' ');
    var h = items.size() / w;
    checkArgument(items.size() == w * h);
    int[][] d = new int[h][w];
    int i = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++, i++) {
        var st = items.get(i);
        int val = -1000;
        if (!st.equals("x"))
          val = Integer.parseInt(st);
        d[y][x] = val;
      }
    }

    dump(d, 0, 0);

    var result = calculateMinimumHP(d);
    pr("result:", result);

    var expected = SLOWcalculateMinimumHP(d);
    checkState(result == expected, "expected", expected);
  }

  private void xr(int seed, int w, int h) {
    var r = new Random(seed);
    int[][] d = new int[h][w];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        d[y][x] = r.nextInt(30) - 15;
      }
    }
    xtest(d);
  }

  private void xtest(int[][] d) {
    if (Math.max(d.length, d[0].length) < 30)
      dump(d, 0, 0);

    var result = calculateMinimumHP(d);
    pr("result:", result);

    var expected = SLOWcalculateMinimumHP(d);
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
        String v = "x";
        if (c != -1000)
          v = Integer.toString(c);
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

  private int SLOWcalculateMinimumHP(int[][] dungeon) {
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

        // dump(dungeon, x, y);

        var val = dungeon[y][x];

        //pr("x:", x, "y:", y, "val:", val);
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
      return "(c" + currentValue + "m" + minimumValue + ")";
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

  private static final boolean withMemo = false;

  public int calculateMinimumHP(int[][] dungeon) {
    this.dungeon = dungeon;
    int dw = dungeon[0].length;
    int dh = dungeon.length;
    if (withMemo)
      memo = new St[dh][dw];

    priorityQueue = new TreeSet<St>(new Comparator<St>() {
      public int compare(St o1, St o2) {
        int result = -(o1.minimumValue - o2.minimumValue);
        if (result == 0)
          result = (o1.y * 300 + o1.x) - (o2.y * 300 + o2.x);
        return result;
      }
    });

    var c00 = dungeon[0][0];
    priorityQueue.add(new St(0, 0, c00, c00));
    int popCount = 0;
    while (true) {
      var state = priorityQueue.first();
      popCount++;
      pr("popped state:", popCount, state);
      priorityQueue.remove(state);
      var x = state.x;
      var y = state.y;
      if (x == dw - 1 && y == dh - 1) {
        return Math.max(1, 1 - state.minimumValue);
      }
      if (x + 1 < dw)
        tryExtend(state, x + 1, y);
      if (y + 1 < dh)
        tryExtend(state, x, y + 1);
    }
  }

  private void tryExtend(St state, int nx, int ny) {
    var ns = state.extend(nx, ny, dungeon[ny][nx]);
    if (withMemo) {
      var best = memo[ny][nx];
      if (best != null) {
        pr("*** try extend, found memoized");
      }
      if (best == null || !(best.value >= ns.value && best.minimumValue >= ns.minimumValue)) {
        pr("memoizing:", nx, ny, ns);
        memo[ny][nx] = ns;
        priorityQueue.add(ns);
      } else {
        pr(VERT_SP, "*** Not extending due to memo:", ns);
      }
    } else {
      priorityQueue.add(ns);
    }
  }

  private St[][] memo;
  private TreeSet<St> priorityQueue;
  private int[][] dungeon;

  private static class St {
    St(int x, int y, int value, int minValue) {
      this.value = value;
      this.minimumValue = minValue;
      this.x = x;
      this.y = y;
    }

    public St extend(int x, int y, int cost) {
      int val = value + cost;
      var out = new St(x, y, val, Math.min(val, minimumValue));
      pr("...extending to:", out);
      return out;
    }

    @Override
    public String toString() {
      return "(" + x + " " + y + " v:" + value + " m:" + minimumValue + ")";
    }

    int x, y;
    int value, minimumValue;
  }
}
