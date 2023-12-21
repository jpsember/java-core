package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * About to redo the algorithm.
 * 
 * I think we can look at the input and find distinct buy/sell points ahead of
 * time, and choose accordingly.
 *
 */
public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  public void run() {
    x("[8,6,4,3,3,2,3,5,8,3,8,2,6]", 2, 11);

    x("[3,3]", 1, 0);

    x("[0,5,4,9,0,6]", 2, 15);

    x(2, 8, -1);
    if (false)
      for (int i = 7; i < 30; i++) {
        pr(INDENT, "i:", i);
        x(2, i, -1);
      }
    x(2, 2000, -1);
    x("[3,3,5,0,0,3,1,4]", 2, 6);
    x("[5,2]", 2, 0);
    x("[3,2,6,5,0,3]", 2, 7);
    x("[1,2,3,4,5]", 2, 4);
    x("[2,8,4,9,3,8]", 2, 12);
    x("[7,6,4,3,1]", 2, 0);
    x("[5]", 2, 0);
    x("[72]", 2, 0);
  }

  private void x(int k, int count, int expected) {
    rand(2000 + count);
    int[] prices = new int[count];
    for (int i = 0; i < count; i++) {
      prices[i] = rand().nextInt(30) + 2;
    }
    var result = maxProfit(k, prices);
    pr("k:", k, "prices", darray(prices), "result:", result);
    if (expected >= 0)
      verify(result, expected);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    var result = maxProfit(k, prices);
    pr("k:", k, "prices", darray(prices), "result:", result);
    if (expected < 0)
      expected = result;
    verify(result, expected);
  }

  // ------------------------------------------------------------------

  public int maxProfit(final int k, final int[] prices) {
    buySellPrices = findBuySellPoints(prices);
    db("prices:", darray(prices));
    db("buy/sell prices:", darray(buySellPrices));
    memo.clear();
    cacheMisses = 0;
    cacheAttempts = 0;
    var result = aux(k, 0);
    db("cache attempts:", cacheAttempts, "misses:", cacheMisses,
        ((cacheMisses * 100) / (cacheAttempts + 1)) + "%");
    return result;
  }

  /**
   * Determine best profit for choosing at most k transactions from the list of
   * buy/sell points starting at cursor c
   */
  private int aux(int k, int c) {
    if (k == 0 || c >= buySellPrices.length)
      return 0;
    cacheAttempts++;
    int key = (c << 7) | k;
    int profit = memo.getOrDefault(key, -1);
    if (profit < 0) {
      cacheMisses++;
      // Consider not using the next buy point,
      // or using it with any of the following sell points
      profit = aux(k, c + 2);
      for (int d = c; d < buySellPrices.length; d += 2) {
        var thisProfit = buySellPrices[d + 1] - buySellPrices[c];
        var other = aux(k - 1, d + 2);
        var profit2 = thisProfit + other;
        if (profit2 > profit)
          profit = profit2;
      }
      memo.put(key, profit);
    }
    return profit;
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

  private int[] buySellPrices;
  private Map<Integer, Integer> memo = new HashMap<>();
  private int cacheAttempts;
  private int cacheMisses;
}
