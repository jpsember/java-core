package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

/**
 * We want to find a dividing point that yields the best transaction before the
 * point plus the best transaction after it.
 * 
 * We construct a flipped version of the price history, such that a forward
 * traversal actually has the effect of walking backward in history.
 */
public class BestTimeToBuyAndSellStockIII extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIII().run();
  }

  public void run() {
    x("[3,3,5,0,0,3,1,4]", 6);
    x("[1,2,3,4,5]", 4);
    x("[7,6,4,3,1]", 0);
    x("[72]", 0);
    x("[2,8,4,9,3,8]", 12);
  }

  private void x(String s, int expected) {
    var prices = extractNums(s);
    var result = maxProfit(prices);
    pr("Prices", prices, "Result:", result);
    verify(result, expected);
  }

  public int maxProfit(int[] prices) {

    resetIndent();
    var buySellPrices = findBuySellPoints(prices);
    db("prices:", darray(prices));
    db("buy/sell prices:", darray(buySellPrices));
    mPoints = buildPoints(buySellPrices);
    db("points:", Arrays.asList(mPoints));

    int n = mPoints.length;
    if (n == 0)
      return 0;

    // Find most profitable transaction for each prefix up to length n
    var fwdResult = new int[n];
    {
      var minPrice = mPoints[0].buyPrice;
      var maxProfit = 0;
      for (int i = 0; i < n; i++) {
        var price = mPoints[i].sellPrice;
        maxProfit = Math.max(maxProfit, price - minPrice);
        minPrice = Math.min(price, minPrice);
        pr("i:", i, "minPrice:", minPrice, "maxProfit:", maxProfit);
        fwdResult[i] = maxProfit;
      }
    }

    // Find most profitable transaction for each suffix starting after 0...n-1

    var bwdResult = new int[n];
    {
      var maxPrice = mPoints[n - 1].sellPrice;
      var maxProfit = 0;
      // We can omit the first point on the backward pass, as the 
      // 'all points' result will be included in the last entry of the forward pass
      for (int i = n - 1; i > 0; i--) {
        var price = mPoints[i].buyPrice;
        maxProfit = Math.max(maxProfit, maxPrice - price);
        maxPrice = Math.max(price, maxPrice);
        pr("i:", i, "maxPrice:", maxPrice, "maxProfit:", maxProfit);
        bwdResult[i] = maxProfit;
      }
    }
    pr("fwdRes:", fwdResult);
    pr("bwdRes:", bwdResult);

    int bestProfit = fwdResult[n - 1];
    for (int i = 0; i < n - 1; i++)
      bestProfit = Math.max(fwdResult[i] + bwdResult[i + 1], bestProfit);
    return bestProfit;
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

  private Pt[] buildPoints(int[] buySellPoints) {
    var out = new Pt[buySellPoints.length / 2];
    int j = 0;
    for (int i = 0; i < buySellPoints.length; i += 2)
      out[j++] = new Pt(buySellPoints[i], buySellPoints[i + 1]);
    return out;
  }

  private static class Pt {

    Pt(int buy, int sell) {
      this.buyPrice = buy;
      this.sellPrice = sell;
    }

    public int profit() {
      return sellPrice - buyPrice;
    }

    @Override
    public String toString() {
      return "(" + buyPrice + " " + sellPrice + ")=" + profit();
    }

    int buyPrice;
    int sellPrice;
  }

  private Pt[] mPoints;

}
