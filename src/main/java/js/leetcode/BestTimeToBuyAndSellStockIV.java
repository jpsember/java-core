package js.leetcode;

import static js.base.Tools.*;

import js.base.BasePrinter;

public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {

    BasePrinter.registerClassHandler(Tr.class, (x, p) -> print((Tr) x, p));

    new BestTimeToBuyAndSellStockIV().run();
  }

  private static void print(Tr x, BasePrinter p) {
    p.appendString("(" + x.buy + ".." + x.sell + ": " + profit(x.buy, x.sell) + ")");
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
    sPrices = prices;
    int n = prices.length;

    var y = new Tr();
    y.buy = 1;
    y.sell = 3;
    db(y);

    return 42 + n;
  }

  private static class Tr {
    int buy;
    int sell;
  }

  private static int profit(int buy, int sell) {
    checkArgument(sell > buy);
    return sPrices[sell] - sPrices[buy];
  }

  // Global variable for debug usage
  private static int[] sPrices;
}
