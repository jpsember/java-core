package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  public void run() {
    x("[2,8,4,9,3,8]", 2, 12);
    xx("[3,2,6,5,0,3]", 2, 7);
    x("[1,2,3,4,5]", 2, 4);
    x("[7,6,4,3,1]", 2, 0);
    x("[72]", 2, 0);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    var result = maxProfit(k, prices);
    pr("k:", k, "prices", prices, "result:", result);
    verify(result, expected);
  }

  private static void verify() {
    if (trans.isEmpty())
      return;
    Tr prev = null;
    for (var t : trans) {
      if (prev != null) {
        if (prev.sell >= t.buy) {
          die("bad transaction sequence:", trans);
        }
      }
      prev = t;
    }
  }

  public int maxProfit(int k, int[] prices) {
    if (prices.length == 1)
      return 0;
    sPrices = prices;
    trans = new ArrayList<>();
    int n = prices.length;

    // var trans = new Tr[k];
    //    int tCursor = 0;
    int minTime = 0;
    todo("mintime accounting is problematic");

    for (int time = 1; time < n; time++) {
      db("time:", time, "price:", prices[time], "trans:", trans);

      verify();

      if (prices[time] < prices[minTime])
        minTime = time;

      // If we haven't moved up from previous position, do nothing
      if (prices[time] <= prices[time - 1])
        continue;

      // Construct candidate transaction from the previous minimum to this price
      var newTrans = new Tr(minTime, time);
      if (newTrans.profit <= 0)
        continue;

      if (trans.size() < k) {
        trans.add(newTrans);
        continue;
      }

      // We're already at the maximum number of transactions.
      // Determine if this transaction should replace one of the previous ones
      int minTransSlot = 0;
      Tr minTrans = trans.get(0);
      for (int i = 1; i < trans.size(); i++) {
        var t = trans.get(i);
        if (t.profit < minTrans.profit) {
          minTrans = t;
          minTransSlot = i;
        }
      }

      // Is the new one greater profit than an existing one?

      if (minTrans.profit < newTrans.profit) {

        // For now, just throw out the old trans and replace with the new.  
        // Later we will see if we can improve things by rejiggering the buy/sell points.
        trans.remove(minTransSlot);
        trans.add(newTrans);
        minTime = time;
        continue;
      }

      // Can we improve the most recent transaction by extending its sell date to the current time?
      {
        int j = trans.size() - 1;
        var t = trans.get(j);
        if (sPrices[t.sell] < sPrices[time]) {
          trans.set(j, new Tr(t.buy, time));
          minTime = time;
          continue;
        }
      }
    }
    int sum = 0;
    for (var t : trans)
      sum += t.profit;
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

  private static List<Tr> trans;
  // Global variable for debug usage
  private static int[] sPrices;
}
