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
    x("[3,2,6,5,0,3]", 2, 7);
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

  private void verify() {
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
    if (trans.size() > sK)
      die(">k transactions in sequence:", trans);
  }

  public int maxProfit(int k, int[] prices) {
    if (prices.length == 1)
      return 0;
    sK = k;
    sPrices = prices;
    trans = new ArrayList<>();
    int n = prices.length;
    minTime = -1;

    db("Prices:", prices);
    for (int time = 1; time < n; time++) {
      db("time:", time, "price:", prices[time], "minTime:", minTime, "trans:", trans);

      verify();

      if (minTime < 0) {
        if (trans.isEmpty())
          minTime = 0;
        else
          minTime = lastTrans().sell;
      }

      if (prices[time] < prices[minTime])
        minTime = time;

      // If we haven't moved up from previous position, do nothing
      if (prices[time] <= prices[time - 1]) {
        db("...not moving up");
        continue;
      }

      // Construct candidate transaction, or extend previous (if it ended at previous time slot)

      if (trans.size() != 0) {
        var prev = lastTrans();
        if (prev.sell == time - 1 && prices[time] >= prices[prev.sell]) {
          db("...extending previous transaction");
          trans.set(trans.size() - 1, new Tr(prev.buy, time));
          minTime = time;
          continue;
        }
      }

      // Construct candidate transaction from the previous minimum to this price
      var newTrans = new Tr(minTime, time);
      if (newTrans.profit <= 0)
        continue;

      addTrans(newTrans);
      db("...added candidate transaction:", newTrans);

      if (trans.size() <= k) {
        db("...not more than k transactions");
        continue;
      }

      // Determine which of the transaction to delete.
      // Choose the one that minimizes the drop in profit, and take into
      // consideration the neighbors being able to reposition to use the deleted one's date
      todo(
          "cache the value of deleting a transaction; if we change a transaction, delete this cached flag for both its neighbors as well");
      int bestImprovement = Integer.MIN_VALUE;
      int minSlot = -1;
      Tr minLeft = null;
      Tr minRight = null;
      for (int i = 0; i < trans.size(); i++) {
        var t = tr(i);
        var improvement = -t.profit;

        // The transactions to the left (resp, right) side of this one might
        // be improved by using its sell (resp, buy) date

        var leftSlot = i - 1;
        var rightSlot = i + 1;
        Tr altLeft = null;
        Tr altRight = null;
        var leftImprovement = Integer.MIN_VALUE;
        var rightImprovement = Integer.MIN_VALUE;
        if (leftSlot >= 0) {
          var left = tr(leftSlot);
          altLeft = new Tr(left.buy, t.sell);
          leftImprovement = altLeft.profit - left.profit;
        }
        if (rightSlot < trans.size()) {
          var right = tr(rightSlot);
          altRight = new Tr(t.buy, right.sell);
          rightImprovement = altRight.profit - right.profit;
        }

        int sideImp = Math.max(leftImprovement, rightImprovement);
        if (sideImp > 0) {
          improvement += sideImp;
          if (leftImprovement > rightImprovement)
            altRight = null;
          else
            altLeft = null;
        }

        if (improvement > bestImprovement) {
          bestImprovement = improvement;
          minSlot = i;
          minLeft = altLeft;
          minRight = altRight;
        }
      }

      db("...removing suboptimal transaction:", tr(minSlot));
      if (minLeft != null)
        trans.set(minSlot - 1, minLeft);
      else if (minRight != null)
        trans.set(minSlot + 1, minRight);
      trans.remove(minSlot);

      db("...candidate isn't an improvement over existing ones");
      // Can we improve the most recent transaction by extending its sell date to the current time?
      {
        int j = trans.size() - 1;
        var t = tr(j);
        if (sPrices[t.sell] < sPrices[time]) {
          trans.remove(j);
          db("...improving most recent transaction by extending its sell date to current time");
          addTrans(new Tr(t.buy, time));
          continue;
        }
      }
    }
    int sum = 0;
    for (var t : trans)
      sum += t.profit;
    return sum;
  }

  private void addTrans(Tr t) {
    trans.add(t);
    minTime = t.sell;
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
      return "(" + buy + ".." + sell + ": " + sPrices[buy] + ".." + sPrices[sell] + " = " + profit + ")";
    }
  }

  private static Tr lastTrans() {
    return tr(trans.size() - 1);
  }

  private static Tr tr(int slot) {
    return trans.get(slot);
  }

  private int sK;
  private static List<Tr> trans;
  private static int minTime;
  private static int[] sPrices;
}
