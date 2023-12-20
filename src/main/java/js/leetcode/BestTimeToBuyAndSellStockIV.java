package js.leetcode;

import static js.base.Tools.*;

public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  public void run() {
    x("[3,2,6,5,0,3]", 2, 7);
    x("[1,2,3,4,5]", 2, 4);
    x("[7,6,4,3,1]", 2, 0);
    x("[72]", 2, 0);
    x("[2,8,4,9,3,8]", 2, 12);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    var result = maxProfit(k, prices);
    pr("k:", k, "prices", prices, "result:", result);
    verify(result, expected);
  }

  public int maxProfit(int k, int[] prices) {
    if (prices.length == 1)
      return 0;
    int n = prices.length;

    return 42 + n;
  }

}
