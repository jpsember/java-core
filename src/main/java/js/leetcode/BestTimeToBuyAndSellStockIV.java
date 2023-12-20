package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;

import js.base.BasePrinter;
import js.json.JSList;

public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  //  private static void print(Tr x, BasePrinter p) {
  //    p.appendString("(" + x.buy + ".." + x.sell + ": " + x.profit + ")");
  //  }

  public void run() {
    xx("[3,2,6,5,0,3]", 2, 7);
    //    x("[1,2,3,4,5]", 2, 4);
    //    x("[7,6,4,3,1]", 2, 0);
    //    x("[72]", 2, 0);
    x("[2,8,4,9,3,8]", 2, 12);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    var result = maxProfit(k, prices);
    pr("k:", k, "prices", prices, "result:", result);
    verify(result, expected);
  }

  private static String dTrans(Tr[] trans, int count) {
    return BasePrinter.toString(Arrays.copyOfRange(trans, 0, count));
  }

  public int maxProfit(int k, int[] prices) {
    if (prices.length == 1)
      return 0;
    sPrices = prices;
    int n = prices.length;

    var trans = new Tr[k];
    int tCursor = 0;
    int minTime = 0;
    todo("mintime accounting is problematic");

    for (int time = 1; time < n; time++) {
      db("time:", time, "price:", prices[time], "trans:", dTrans(trans, tCursor));
      if (prices[time] < prices[minTime])
        minTime = time;

      // If we haven't moved up from previous position, do nothing
      if (prices[time] <= prices[time - 1])
        continue;

      // Construct candidate transaction from the previous minimum to this price
      var newTrans = new Tr(minTime, time);
      if (newTrans.profit <= 0)
        continue;

      if (tCursor < k) {
        trans[tCursor++] = newTrans;
        continue;
      }

      // We're already at the maximum number of transactions.
      // Determine if this transaction should replace one of the previous ones
      int minTransSlot = 0;
      Tr minTrans = trans[minTransSlot];
      for (int i = 1; i < tCursor; i++) {
        var t = trans[i];
        if (t.profit < minTrans.profit) {
          minTrans = t;
          minTransSlot = i;
        }
      }

      // Is the new one greater profit than an existing one?

      if (minTrans.profit < newTrans.profit) {

        // For now, just throw out the old trans and replace with the new.  
        // Later we will see if we can improve things by rejiggering the buy/sell points.
        while (minTransSlot + 1 < tCursor) {
          trans[minTransSlot] = trans[minTransSlot + 1];
          minTransSlot++;
        }
        trans[tCursor - 1] = newTrans;
        minTime = time;
        continue;
      }

      // Can we improve the most recent transaction by extending its sell date to the current time?
      {
        var t = trans[tCursor - 1];
        if (sPrices[t.sell] < sPrices[time]) {
          trans[tCursor - 1] = new Tr(t.buy, time);
          minTime = time;
          continue;
        }
      }

    }
    int sum = 0;
    for (int i = 0; i < tCursor; i++)
      sum += trans[i].profit;
    return sum;
  }

  private static class Tr {
    Tr(int buyTime, int sellTime) {
      checkArgument(sellTime > buyTime);
      this.buy = buyTime;
      this.sell = sellTime;
      this.profit = sPrices[sell] - sPrices[buy];
    }

    final int buy;
    final int sell;
    final int profit;

    @Override
    public String toString() {
      return "(" + buy + ".." + sell + ": " + profit + ")";
    }
  }

  // Global variable for debug usage
  private static int[] sPrices;
}
