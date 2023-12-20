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
    for (int i = 1; i < n; i++) {
      var profit = fwd[i * 2 + 0] + bwd[(n - 1 - i) * 2 + 0];
      bestProfit = Math.max(profit, bestProfit);
      //      var profit = fwd[i].maxProfit + bwd[n - 1 - i].maxProfit;
      //      bestProfit = Math.max(profit, bestProfit);
    }
    return bestProfit;
  }

  //  private static class Info {
  //    int minPrice; // minimum price seen so far
  //    int maxProfit; // maximal (nonnegative) profit seen so far
  //  }

  private int[] calcInfo(int[] prices) {
    //  private Info[] calcInfo(int[] prices) {
    //    var result = new Info[prices.length];
    var result = new int[prices.length * 2];

    //
    //    Info prevInfo = new Info();
    //    prevInfo.minPrice = prices[0];
    //
    var prevMinPrice = prices[0];
    var prevMaxProfit = 0;

    for (int i = 0; i < prices.length; i++) {
      var price = prices[i];
      //      var info = new Info();
      //
      var maxProfit = Math.max(prevMaxProfit, price - prevMinPrice);

      //      // See if selling at this price improves the best profit
      //      info.maxProfit = Math.max(prevInfo.maxProfit, price - prevInfo.minPrice);
      //      info.minPrice = prevInfo.minPrice;
      //
      var minPrice = Math.min(price, prevMinPrice);

      //      if (price < info.minPrice)
      //        info.minPrice = price;
      //
      prevMinPrice = minPrice;
      prevMaxProfit = maxProfit;

      // result[i * 2 + 0] = prevMinPrice;
      result[i * 2 + 0] = prevMaxProfit;
      //      prevInfo = info;
      //      result[i] = info;
    }
    return result;
  }

}
