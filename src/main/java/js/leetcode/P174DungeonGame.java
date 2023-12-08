
package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

import js.base.BasePrinter;

/**
 * Thoughts:
 * 
 * This looks like a straightforward dynamic programming problem, but perhaps
 * the minimum initial health is the wrinkle.
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

    var result = calculateMinimumHP(d);
    dump(d, 0, 0, "Result:", result);
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

    Info[][] stats = new Info[dh][dw];

    for (int s = 0; s < dw + dh - 1; s++) {
      int x = s;
      int y = 0;
      if (s >= dw) {
        x = dw - 1;
        y = s - (dw - 1);
      }
      while (x >= 0 && y < dh) {

        var val = dungeon[y][x];

        pr("x:", x, "y:", y, "val:", val);
        var state = new Info();

        Info left = null;
        Info up = null;
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
    for (var ent : last.paths.entrySet()) {
      var min = ent.getValue();
      if (bestMin == null || min > bestMin)
        bestMin = min;
    }
    return Math.max(1,  1-bestMin);
  }

  private static class Info {

    // A map of current values -> min value on path
    Map<Integer, Integer> paths = new HashMap<>();

    void add(int currentValue, int minValue) {
      Integer existing = paths.get(currentValue);
      if (existing == null || existing < minValue) {
        pr("...updating val", currentValue, "==> min", minValue);
        paths.put(currentValue, minValue);
      }
    }

    void merge(Info info, int cost) {
      pr("...merge for cost:",cost);
      for (var ent : info.paths.entrySet()) {
        // Get source val and min
        int val = ent.getKey();
        int min = ent.getValue();
        
        // Calculate target value
        int newVal = val + cost;
        int newMin = Math.min(min, newVal);
        
        add(newVal, newMin);
      }
    }
  }

}
