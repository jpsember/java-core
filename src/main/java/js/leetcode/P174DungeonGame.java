
package js.leetcode;

import static js.base.Tools.*;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

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
      x(32,
          " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"
              + " 1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1   1 1 1 1   1 1 1 1    1 1 1 1   1 1 1 1"

      );

    x(7, "[[0,-74,-47,-20,-23,-39,-48],[37,-30,37,-65,-82,28,-27],[-76,-33,7,42,3,49,-93],[37,-41,35,-16,-96,-56,38],[-52,19,-37,14,-65,-42,9],[5,-26,-30,-65,11,5,16],[-60,9,36,-36,41,-47,-86],[-22,19,-5,-41,-8,-96,-95]]");

    //x(3,"[[0,0,0],[-1,0,0],[2,0,-2]]");

    // x(3, "0 0 0 -20 x 0 40 0 0 x x 0 x x -30 x x 0");
    if (false)
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

    StateSLOW[][] stats = new StateSLOW[dh][dw];

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
        var state = new StateSLOW();

        StateSLOW left = null;
        StateSLOW up = null;
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

  private static class ValuePairSLOW {
    ValuePairSLOW(int currentValue, int minValue) {
      this.currentValue = currentValue;
      this.minimumValue = minValue;
    }

    ValuePairSLOW extend(int cost) {
      return new ValuePairSLOW(currentValue + cost, Math.min(minimumValue, currentValue + cost));
    }

    boolean dominates(ValuePairSLOW other) {
      return currentValue >= other.currentValue && minimumValue >= other.minimumValue;
    }

    @Override
    public String toString() {
      return "(c" + currentValue + "m" + minimumValue + ")";
    }

    int currentValue;
    int minimumValue;
  }

  private static class StateSLOW {

    void add(int currentValue, int minValue) {
      valuePairs.add(new ValuePairSLOW(currentValue, minValue));
    }

    /**
     * Extend a previous state's values to add the cost of an edge to get to
     * this state, and merge the resulting ValuePairs into this states'.
     */
    void merge(StateSLOW prevState, int edgeCost) {

      int prevCursor = 0;
      var prevVals = prevState.valuePairs;

      var currVals = valuePairs;
      var currCursor = 0;

      var merged = new ArrayList<ValuePairSLOW>(prevVals.size() + currVals.size());

      ValuePairSLOW srcValue = null;
      ValuePairSLOW destValue = null;

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
    List<ValuePairSLOW> valuePairs = new ArrayList<>();
  }

  // ------------------------------------------------------------------
  // 
  // ------------------------------------------------------------------

  public int calculateMinimumHP(int[][] dungeon) {
    dung = dungeon;
    dw = dungeon[0].length;
    dh = dungeon.length;
    memo = new int[dh * dw * 2];
    Arrays.fill(memo, 0x80000);
    stateBuffer = new ArrayList<>();

    priorityQueue = new PriorityQueue<State>(new Comparator<State>() {
      public int compare(State o1, State o2) {
        int result = -(o1.minimumValue - o2.minimumValue);
        if (result == 0)
          result = -(o1.value - o2.value);
        if (result == 0)
          result = (o1.y * 300 + o1.x) - (o2.y * 300 + o2.x);
        return result;
      }
    });

    var c00 = dungeon[0][0];
    priorityQueue.add(State.build(0, 0, c00, c00));

    int result;
    while (true) {
      var state = priorityQueue.remove();
      var x = state.x;
      var y = state.y;
      if (x == dw - 1 && y == dh - 1) {
        result = Math.max(1, 1 - state.minimumValue);
        break;
      }
      if (x + 1 < dw)
        tryExtend(state, x + 1, y);
      if (y + 1 < dh)
        tryExtend(state, x, y + 1);
      stateBuffer.add(state);
    }
    return result;
  }

  private void tryExtend(State state, int nx, int ny) {
    var ns = state.extend(nx, ny, dung[ny][nx]);
    var mi = (ny * dw + nx) << 1;

    int bestValue = memo[mi];
    var bestMinValue = memo[mi + 1];
    if (bestValue == 0x80000 || !(bestValue >= ns.value && bestMinValue >= ns.minimumValue)) {
      memo[mi] = ns.value;
      memo[mi + 1] = ns.minimumValue;
      priorityQueue.add(ns);
    } else {
      stateBuffer.add(ns);
    }
  }

  private static int[] memo;
  private static AbstractQueue<State> priorityQueue;
  private static int[][] dung;
  private static int dw, dh;

  private static List<State> stateBuffer;

  private static class State {

    public static State build(int x, int y, int value, int minValue) {
      State s = null;
      if (stateBuffer.isEmpty()) {
        s = new State();
      } else {
        s = stateBuffer.remove(stateBuffer.size() - 1);
      }
      s.value = value;
      s.minimumValue = minValue;
      s.x = x;
      s.y = y;
      return s;
    }

    public State extend(int x, int y, int cost) {
      int val = value + cost;
      return State.build(x, y, val, Math.min(val, minimumValue));
    }

    int x, y;
    int value, minimumValue;
  }
}
