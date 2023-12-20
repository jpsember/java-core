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
    var reversed = new int[n];
    for (int i = 0; i < n; i++)
      reversed[n - 1 - i] = -prices[i];

    var fwd = calcInfo(prices);
    var bwd = calcInfo(reversed);

    int bestProfit = 0;
    for (int i = 1; i < n; i++) {
      var profit = fwd[i].delta + bwd[n - 1 - i].delta;
      bestProfit = Math.max(profit, bestProfit);
    }
    return bestProfit;
  }

  private static class Info {
    int min;
    int delta;
    int deltaMin;
  }

  private Info[] calcInfo(int[] prices) {
    var res = new Info[prices.length];

    Info prevInfo = null;
    for (int i = 0; i < prices.length; i++) {
      var price = prices[i];
      var info = new Info();
      if (prevInfo == null) {
        prevInfo = new Info();
        prevInfo.min = price;
        prevInfo.deltaMin = price;
      }

      info.min = prevInfo.min;
      info.delta = prevInfo.delta;
      info.deltaMin = prevInfo.deltaMin;

      if (price < info.min)
        info.min = price;
      var delta = price - info.min;
      if (delta > info.delta) {
        info.delta = delta;
        info.deltaMin = info.min;
      }
      prevInfo = info;
      res[i] = info;
    }
    return res;
  }

  public static int[] reverse(int[] nums) {
    var rev = new int[nums.length];
    for (int i = 0; i < nums.length; i++)
      rev[nums.length - 1 - i] = nums[i];
    return rev;
  }
}
