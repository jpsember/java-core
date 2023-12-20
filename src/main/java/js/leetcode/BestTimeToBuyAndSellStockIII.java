package js.leetcode;

import static js.base.Tools.*;

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
    if (prices.length == 1)
      return 0;

    int n = prices.length;

    // Construct a flipped (and negated) version of the price list
    var flippedPrices = new int[n];
    for (int i = 0; i < n; i++)
      flippedPrices[n - 1 - i] = -prices[i];

    var fwd = calcInfo(prices);
    var bwd = calcInfo(flippedPrices);

    int bestProfit = 0;
    for (int i = 1; i < n; i++)
      bestProfit = Math.max(fwd[i] + bwd[(n - 1 - i)], bestProfit);
    return bestProfit;
  }

  private int[] calcInfo(int[] prices) {
    var result = new int[prices.length];
    var minPrice = prices[0];
    var maxProfit = 0;
    for (int i = 0; i < prices.length; i++) {
      var price = prices[i];
      maxProfit = Math.max(maxProfit, price - minPrice);
      minPrice = Math.min(price, minPrice);
      result[i] = maxProfit;
    }
    return result;
  }

}
