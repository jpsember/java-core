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
    x("[5]", 2, 0);
    x("[5,2]", 2, 0);
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

  private void verify(int k) {
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
    if (trans.size() > k)
      die(">k transactions in sequence:", trans);
  }

  public int maxProfit(final int k, final int[] prices) {
    if (prices.length == 1)
      return 0;
    sPrices = prices;
    trans = new ArrayList<>();
    int minTimeSinceLastTr = 0;

    db("Prices:", prices);
    int time = 0;
    int signOfLastPriceMovement = 0;

    while (true) {
      time++;
      if (time == prices.length)
        break;
      db("time:", time, "price:", prices[time], "mintime:", minTimeSinceLastTr, "trans:", trans);
      verify(k);

      if (false && alert("validating min time since")) {
        int expLast = 0;
        if (!trans.isEmpty()) {
          var t = tr(trans.size() - 1);
          expLast = t.sell + 1;
        }
        for (int t = expLast; t < time; t++) {
          if (prices[t] < prices[expLast])
            expLast = t;
        }
        if (minTimeSinceLastTr != expLast) {
        die("minTimeSince",minTimeSinceLastTr,"but expected it to be",expLast);
        }
        
      }

      int sign = prices[time] - prices[time - 1];
      if (sign != 0)
        signOfLastPriceMovement = sign;
      if (prices[time] < prices[minTimeSinceLastTr]) {
        db("...new low time of min price:", time);
        todo("why does this want to skip the following code?");
        minTimeSinceLastTr = time;
        continue;
      }

      // If not at a peak, we won't be selling; and if not at a trough, we won't be buying.
      // But we only figure out where we want to buy when we reach a selling point.

      {
        int nextVal = (time + 1 != prices.length) ? prices[time + 1] : Integer.MIN_VALUE;
        db("...mv sign:", signOfLastPriceMovement);
        if (!(signOfLastPriceMovement > 0 && nextVal < prices[time])) {
          db("...not at potential SELL point");
          continue;
        }
      }

      if (time == minTimeSinceLastTr) {
        db("...still at time of min price since last transaction");
        continue;
      }

      // Construct candidate transaction from the previous minimum to this price
      var newTrans = new Tr(minTimeSinceLastTr, time);
      db("...constructed candidate transaction:", newTrans);
      if (newTrans.profit <= 0)
        continue;

      addTrans(newTrans);
//      todo("this min adjustment might not be 'undoable'");
//      var oldMin = minTimeSinceLastTr;

      // We update the minimum time since last transaction here, 
      minTimeSinceLastTr = newTrans.sell + 1;
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

      if (!trans.isEmpty()) {
        var t = tr(trans.size() - 1);
        db("last trans sell:", t.sell, "minTimeSince:", minTimeSinceLastTr, "old:", oldMin);
      }

    }
    int sum = 0;
    for (var t : trans)
      sum += t.profit;
    return sum;
  }

  private void addTrans(Tr t) {
    trans.add(t);
  }

  private static class Tr {
    Tr(int buyTime, int sellTime) {
      checkArgument(sellTime > buyTime, "attempt to construct buyTime", buyTime, ">= sellTime", sellTime);
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

  private static Tr tr(int slot) {
    return trans.get(slot);
  }

  private static List<Tr> trans;
  private static int[] sPrices;

}