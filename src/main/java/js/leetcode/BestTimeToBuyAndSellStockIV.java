package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  public void run() {
    x(100, 1000, 85988);
    if (true)
      return;
    if (false) {
      for (int n = 5; n < 100; n++) {
        solveRandom(n * 31, 30, n, -1);
      }
      return;
    }

    x("[527,92,902,746,778]", 3, 842);

    x("[1,2,3,4,5]", 2, 4);

    x("[70,4,83,56,94,72,78,43,2,86,65,100,94,56,41,66,3,33,10,3,45,94,15,12,78,60,58,0,58,"
        + "15,21,7,11,41,12,96,83,77,47,62,27,19,40,63,30,4,77,52,17,57,21,66,63,29,51,40,"
        + "37,6,44,42,92,16,64,33,31,51,36,0,29,95,92,35,66,91,19,21,100,95,40,61,15,83,31,"
        + "55,59,84,21,99,45,64,90,25,40,6,41,5,25,52,59,61,51,37,92,90,20,20,96,66,79,28,83,"
        + "60,91,30,52,55,1,99,8,68,14,84,59,5,34,93,25,10,93,21,35,66,88,20,97,25,63,80,20,86,"
        + "33,53,43,86,53,55,61,77,9,2,56,78,43,19,68,69,49,1,6,5,82,46,24,33,85,24,56,51,45,100,"
        + "94,26,15,33,35,59,25,65,32,26,93,73,0,40,92,56,76,18,2,45,64,66,64,39,77,1,55,90,10,27,"
        + "85,40,95,78,39,40,62,30,12,57,84,95,86,57,41,52,77,17,9,15,33,17,68,63,59,40,5,63,30,86,"
        + "57,5,55,47,0,92,95,100,25,79,84,93,83,93,18,20,32,63,65,56,68,7,31,100,88,93,11,43,20,13,"
        + "54,34,29,90,50,24,13,44,89,57,65,95,58,32,67,38,2,41,4,63,56,88,39,57,10,1,97,98,25,45,96,"
        + "35,22,0,37,74,98,14,37,77,54,40,17,9,28,83,13,92,3,8,60,52,64,8,87,77,96,70,61,3,96,"
        + "83,56,5,99,81,94,3,38,91,55,83,15,30,39,54,79,55,86,85,32,27,20,74,91,99,100,46,69,77,"
        + "34,97,0,50,51,21,12,3,84,84,48,69,94,28,64,36,70,34,70,11,89,58,6,90,86,4,97,63,10,37,"
        + "48,68,30,29,53,4,91,7,56,63,22,93,69,93,1,85,11,20,41,36,66,67,57,76,85,37,80,99,63,23,"
        + "71,11,73,41,48,54,61,49,91,97,60,38,99,8,17,2,5,56,3,69,90,62,75,76,55,71,83,34,2,36,56,"
        + "40,15,62,39,78,7,37,58,22,64,59,80,16,2,34,83,43,40,39,38,35,89,72,56,77,78,14,45,0,57,"
        + "32,82,93,96,3,51,27,36,38,1,19,66,98,93,91,18,95,93,39,12,40,73,100,17,72,93,25,35,45,"
        + "91,78,13,97,56,40,69,86,69,99,4,36,36,82,35,52,12,46,74,57,65,91,51,41,42,17,78,49,75,"
        + "9,23,65,44,47,93,84,70,19,22,57,27,84,57,85,2,61,17,90,34,49,74,64,46,61,0,28,57,78,75,"
        + "31,27,24,10,93,34,19,75,53,17,26,2,41,89,79,37,14,93,55,74,11,77,60,61,2,68,0,15,12,47,"
        + "12,48,57,73,17,18,11,83,38,5,36,53,94,40,48,81,53,32,53,12,21,90,100,32,29,94,92,83,80,"
        + "36,73,59,61,43,100,36,71,89,9,24,56,7,48,34,58,0,43,34,18,1,29,97,70,92,88,0,48,51,53,0,"
        + "50,21,91,23,34,49,19,17,9,23,43,87,72,39,17,17,97,14,29,4,10,84,10,33,100,86,43,20,22,"
        + "58,90,70,48,23,75,4,66,97,95,1,80,24,43,97,15,38,53,55,86,63,40,7,26,60,95,12,98,"
        + "15,95,71,86,46,33,68,32,86,89,18,88,97,32,42,5,57,13,1,23,34,37,13,65,13,47,55,"
        + "85,37,57,14,89,94,57,13,6,98,47,52,51,19,99,42,1,19,74,60,8,48,28,65,6,12,57,"
        + "49,27,95,1,2,10,25,49,68,57,32,99,24,19,25,32,89,88,73,96,57,14,65,34,8,82,9,"
        + "94,91,19,53,61,70,54,4,66,26,8,63,62,9,20,42,17,52,97,51,53,19,48,76,40,80,"
        + "6,1,89,52,70,38,95,62,24,88,64,42,61,6,50,91,87,69,13,58,43,98,19,94,65,56,"
        + "72,20,72,92,85,58,46,67,2,23,88,58,25,88,18,92,46,15,18,37,9,90,2,38,0,16,"
        + "86,44,69,71,70,30,38,17,69,69,80,73,79,56,17,95,12,37,43,5,5,6,42,16,44,22,"
        + "62,37,86,8,51,73,46,44,15,98,54,22,47,28,11,75,52,49,38,84,55,3,69,100,54,"
        + "66,6,23,98,22,99,21,74,75,33,67,8,80,90,23,46,93,69,85,46,87,76,93,38,77,"
        + "37,72,35,3,82,11,67,46,53,29,60,33,12,62,23,27,72,35,63,68,14,35,27,98,94,"
        + "65,3,13,48,83,27,84,86,49,31,63,40,12,34,79,61,47,29,33,52,100,85,38,24,1,"
        + "16,62,89,36,74,9,49,62,89]", 29, 2818);

    x("[70,4,83,56,94,72,78,43,2,86,65,100,94,56,41,66,3,33,10,3,45,94,15,12,78,60,58,0,58,15,21,7,11,41,12,96,83,77,47,62,27,19,40,63,30,4,77,52,17,57,21,66,63,29,51,40,37,6,44,42,92,16,64,33,31,51,36,0,29,95,92,35,66,91,19,21,100,95,40,61,15,83,31,55,59,84,21,99,45,64,90,25,40,6,41,5,25,52,59,61,51,37,92,90,20,20,96,66,79,28,83,60,91,30,52,55,1,99,8,68,14,84,59,5,34,93,25,10,93,21,35,66,88,20,97,25,63,80,20,86,33,53,43,86,53,55,61,77,9,2,56,78,43,19,68,69,49,1,6,5,82,46,24,33,85,24,56,51,45,100,94,26,15,33,35,59,25,65,32,26,93,73,0,40,92,56,76,18,2,45,64,66,64,39,77,1,55,90,10,27,85,40,95,78,39,40,62,30,12,57,84,95,86,57,41,52,77,17,9,15,33,17,68,63,59,40,5,63,30,86,57,5,55,47,0,92,95,100,25,79,84,93,83,93,18,20,32,63,65,56,68,7,31,100,88,93,11,43,20,13,54,34,29,90,50,24,13,44,89,57,65,95,58,32,67,38,2,41,4,63,56,88,39,57,10,1,97,98,25,45,96,35,22,0,37,74,98,14,37,77,54,40,17,9,28,83,13,92,3,8,60,52,64,8,87,77,96,70,61,3,96,83,56,5,99,81,94,3,38,91,55,83,15,30,39,54,79,55,86,85,32,27,20,74,91,99,100,46,69,77,34,97,0,50,51,21,12,3,84,84,48,69,94,28,64,36,70,34,70,11,89,58,6,90,86,4,97,63,10,37,48,68,30,29,53,4,91,7,56,63,22,93,69,93,1,85,11,20,41,36,66,67,57,76,85,37,80,99,63,23,71,11,73,41,48,54,61,49,91,97,60,38,99,8,17,2,5,56,3,69,90,62,75,76,55,71,83,34,2,36,56,40,15,62,39,78,7,37,58,22,64,59,80,16,2,34,83,43,40,39,38,35,89,72,56,77,78,14,45,0,57,32,82,93,96,3,51,27,36,38,1,19,66,98,93,91,18,95,93,39,12,40,73,100,17,72,93,25,35,45,91,78,13,97,56,40,69,86,69,99,4,36,36,82,35,52,12,46,74,57,65,91,51,41,42,17,78,49,75,9,23,65,44,47,93,84,70,19,22,57,27,84,57,85,2,61,17,90,34,49,74,64,46,61,0,28,57,78,75,31,27,24,10,93,34,19,75,53,17,26,2,41,89,79,37,14,93,55,74,11,77,60,61,2,68,0,15,12,47,12,48,57,73,17,18,11,83,38,5,36,53,94,40,48,81,53,32,53,12,21,90,100,32,29,94,92,83,80,36,73,59,61,43,100,36,71,89,9,24,56,7,48,34,58,0,43,34,18,1,29,97,70,92,88,0,48,51,53,0,50,21,91,23,34,49,19,17,9,23,43,87,72,39,17,17,97,14,29,4,10,84,10,33,100,86,43,20,22,58,90,70,48,23,75,4,66,97,95,1,80,24,43,97,15,38,53,55,86,63,40,7,26,60,95,12,98,15,95,71,86,46,33,68,32,86,89,18,88,97,32,42,5,57,13,1,23,34,37,13,65,13,47,55,85,37,57,14,89,94,57,13,6,98,47,52,51,19,99,42,1,19,74,60,8,48,28,65,6,12,57,49,27,95,1,2,10,25,49,68,57,32,99,24,19,25,32,89,88,73,96,57,14,65,34,8,82,9,94,91,19,53,61,70,54,4,66,26,8,63,62,9,20,42,17,52,97,51,53,19,48,76,40,80,6,1,89,52,70,38,95,62,24,88,64,42,61,6,50,91,87,69,13,58,43,98,19,94,65,56,72,20,72,92,85,58,46,67,2,23,88,58,25,88,18,92,46,15,18,37,9,90,2,38,0,16,86,44,69,71,70,30,38,17,69,69,80,73,79,56,17,95,12,37,43,5,5,6,42,16,44,22,62,37,86,8,51,73,46,44,15,98,54,22,47,28,11,75,52,49,38,84,55,3,69,100,54,66,6,23,98,22,99,21,74,75,33,67,8,80,90,23,46,93,69,85,46,87,76,93,38,77,37,72,35,3,82,11,67,46,53,29,60,33,12,62,23,27,72,35,63,68,14,35,27,98,94,65,3,13,48,83,27,84,86,49,31,63,40,12,34,79,61,47,29,33,52,100,85,38,24,1,16,62,89,36,74,9,49,62,89]",
        100, 8740);

    x("[5,2,3,0,3,5,6,8,1,5]", 2, 12);

    x("[ 662,674,271,235,745,430,827,317,876 ]", 3, 1466);

    x("[1,2,4,2,5,7,2,4,9,0]", 2, 13);

    x("[0,5,4,9,0,6]", 2, 15);

    x("[3,2,6,5,0,3]", 2, 7);

    x("[8,6,4,3,3,2,3,5,8,3,8,2,6]", 2, 11);

    x("[3,3]", 1, 0);

    x(2, 8, 1443);

    x("[3,3,5,0,0,3,1,4]", 2, 6);
    x("[5,2]", 2, 0);
    x("[3,2,6,5,0,3]", 2, 7);
    x("[2,8,4,9,3,8]", 2, 12);
    x("[7,6,4,3,1]", 2, 0);
    x("[5]", 2, 0);
  }

  private void x(int k, int count, int expected) {
    solveRandom(2000 + count, k, count, expected);
  }

  private void solveRandom(int seed, int k, int count, int expected) {
    rand(seed);
    int[] prices = new int[count];
    for (int i = 0; i < count; i++) {
      prices[i] = rand().nextInt(1001);
    }
    solveWithVerify(prices, k, expected);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    solveWithVerify(prices, k, expected);
  }

  private void solveWithVerify(int[] prices, int k, int expected) {

    final boolean withTiming = (prices.length >= 100) || true;
    pr(VERT_SP, "prices.length:", prices.length, "withTiming:", withTiming);

    if (withTiming)
      checkpoint("Calculating max profit for n", prices.length);

    for (int i = 0; i < 100; i++)
      maxProfit(k, prices);

    var result = maxProfit(k, prices);
    if (withTiming)
      checkpoint("Done calculating");
    db("k:", k, "prices", darray(prices), "result:", result);

    verify(result, expected);
  }

  private boolean stateDominates(int profit1, int minPrice1, int profit2, int minPrice2) {
    return profit1 >= profit2 && minPrice1 <= minPrice2;
  }

  private void dumpTable(Level levels[]) {
    if (!db)
      return;
    db("---------------------------------------------------");
    var sb = new StringBuilder();
    for (int k = 0; k < levels.length; k++) {
      sb.setLength(0);
      sb.append("k: " + k);
      tab(sb, 7);
      sb.append("|");
      var cs = sb.length() + 2;
      int i = INIT_INDEX;
      var lvl = levels[k];
      var values = lvl.values;
      for (var cursor = 0; cursor < lvl.size; cursor += 2) {
        for (var c2 = 0; c2 < lvl.size; c2 += 2) {
          if (cursor != c2
              && stateDominates(values[cursor], values[cursor + 1], values[c2], values[c2 + 1])) {
            die("level", k, "has dominated state:", dumpState(lvl, c2), INDENT, "dominated by:",
                dumpState(lvl, cursor));
          }
        }
        tab(sb, cs + i * 10);
        sb.append(dumpState(lvl, cursor));
      }
      db(sb);
    }
    db("---------------------------------------------------");

  }

  private String dumpState(int profit, int minPrice) {
    return "( " + profit + " " + (minPrice < Integer.MAX_VALUE ? "" + minPrice : "x") + " )";
  }

  private String dumpState(Level level, int cursor) {
    var values = level.values;
    return dumpState(values[cursor], values[cursor + 1]);
  }

  // ------------------------------------------------------------------

  public int maxProfit(final int k, final int[] prices) {
    var buySellPoints = findBuySellPoints(prices);
    db = buySellPoints.length < 10;
    if (db) {
      db("prices:", darray(prices));
      db("buy/sell prices:", darray(buySellPoints));
    }

    Level[] levels = new Level[k + 1];
    for (int i = 0; i <= k; i++) {
      levels[i] = new Level(i == k ? 4 : buySellPoints.length + 8);
      levels[i].k = i;
    }

    levels[0].add(0, Integer.MAX_VALUE);

    for (int i = 0; i < buySellPoints.length; i += 2) {
      int buyPricex = buySellPoints[i];
      int sellPricex = buySellPoints[i + 1];

      db(VERT_SP, "buySell point:", buyPricex, sellPricex);
      dumpTable(levels);
      
      for (int j = k - 1; j >= 0; j--) {
        final var level = levels[j];
        final var values = level.values;
        db("extending level", j);

        // If we make a transaction, the time will advance to this point's sell point;
        // so among all of this level's states, choose the one whose existing profit
        // and buy price will produce the best profit.

        var bestProfit = 0;
        int cursor = level.size;
        while (cursor != 0) {
          cursor -= 2;
          var buyPrice = Math.min(buyPricex, values[cursor + 1]);
          var profit = values[cursor] + sellPricex - buyPrice;
          bestProfit = Math.max(bestProfit, profit);
        }

        if (bestProfit > 0) {
          db("add state", dumpState(bestProfit, Integer.MAX_VALUE), "to level", j + 1);
          addStateToLevel(bestProfit, levels[j + 1]);
        }

        // All further extensions to this level will happen with a buy price that is at
        // most this point's buy price.  Update any buy prices lower than this,
        // and trim any states dominated by the max profit state at this level.

        cursor = level.size;
        var maxProfitState = 0;
        while (cursor != 0) {
          cursor -= 2;
          if (values[cursor] > values[maxProfitState])
            maxProfitState = cursor;
          values[cursor + 1] = Math.min(buyPricex, values[cursor + 1]);
        }

        // If the max profit state dominates any *other* states at this level, remove them
        removeDominatedStates(level, maxProfitState);
      }
    }

    db(VERT_SP, "extracting most profitable state from ANY levels");
    dumpTable(levels);

    int maxProfit = 0;
    for (var level : levels) {
      var values = level.values;
      var cursor = level.size;
      while (cursor != 0) {
        cursor -= 2;
        maxProfit = Math.max(maxProfit, values[cursor]);
      }
    }
    db("...best profit:", maxProfit);
    return maxProfit;
  }

  private void addStateToLevel(int profit, Level level) {
    if (profit <= level.maxProfit)
      return;
    final var minValue = Integer.MAX_VALUE;
    level.add(profit, minValue);
    removeDominatedStates(level, level.size - 2);
  }

  private void removeDominatedStates(Level level, int dominatingCursor) {
    var values = level.values;
    var i = level.size;
    // The position of the dominating state might change if we delete states!
    // But I don't think that would happen, and I won't elaborate here
    var domProfit = values[dominatingCursor];
    var domMinPrice = values[dominatingCursor + 1];
    while (i != 0) {
      i -= 2;
      if (i == dominatingCursor)
        continue;
      if (domProfit >= values[i] && domMinPrice <= values[i + 1]) {
        db("...new state dominates existing");
        level.delete(i);
      }
    }
  }

  private static class Level {

    int k;
    public int[] values;
    public int maxProfit;
    public int size;

    public Level(int initialCapacity) {
      values = new int[initialCapacity];
    }

    public void add(int profit, int minValue) {
      ensureCapacity(size + 2);
      values[size] = profit;
      values[size + 1] = minValue;
      if (profit > maxProfit)
        maxProfit = profit;
      size += 2;
    }

    public void ensureCapacity(int capacity) {
      if (values.length < capacity) {
        int[] newValues = new int[8 + capacity * 2];
        System.arraycopy(values, 0, newValues, 0, size);
        pr("resizing array from:", values.length, "to:", newValues.length);
        values = newValues;
      }
    }

    /**
     * Delete a state from this level. Assumes this is NOT the maxProfit state.
     */
    public void delete(int index) {
      // Move last value into this slot's position
      int src = size - 2;
      values[index] = values[src];
      values[index + 1] = values[src + 1];
      size -= 2;
    }
  }

  private int[] findBuySellPoints(int[] prices) {
    final int len = prices.length;
    int[] result = new int[len * 2];
    var dir = -1;
    int outCursor = 0;
    for (int inCursor = 0; inCursor < len; inCursor++) {
      var price = prices[inCursor];
      var nextPrice = (inCursor + 1 < len) ? prices[inCursor + 1] : -1001;
      // If price isn't moving, do nothing
      var newDir = dir;
      if (nextPrice != price)
        newDir = (nextPrice < price) ? -1 : 1;
      // If direction hasn't changed, do nothing
      if (newDir == dir)
        continue;
      dir = newDir;
      result[outCursor++] = price;
    }
    return Arrays.copyOf(result, outCursor);
  }

}
