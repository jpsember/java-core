package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.data.DataUtil;

/**
 * This recursive approach is close to working but I suspect it will be very
 * slow.
 */
public class BestTimeToBuyAndSellStockIV extends LeetCode {

  public static void main(String[] args) {
    new BestTimeToBuyAndSellStockIV().run();
  }

  public void run() {

    x("[ 662,674,271,235,745,430,827,317,876 ]", 3, -1);

    x("[5,2,3,0,3,5,6,8,1,5]", 2, 12);

    if (false) {
      final int max = 100;
      add(1, 2);
      for (int i = 0; i < 48; i++)
        add(i + 2, i + 4);
      add(1, max + 1);
      dup(9);
      xa(10, max * 10);
      return;
    }

    if (false) {
      x(100, 1000, 85988);
      return;
    }

    if (true)

      x("[70,4,83,56,94,72,78,43,2,86,65,100,94,56,41,66,3,33,10,3,45,94,15,12,78,60,58,0,58,"
          + "15,21,7,11,41,12,96,83,77,47,62,27,19,40,63,30,4,77,52,17,57,21,66,63,29,51,40,"
          + "37,6,44,42,92,16,64,33,31,51,36,0,29,95,92,35,66,91,19,21,100,95,40,61,15,83,31,"
          + "55,59,84,21,99,45,64,90,25,40,6,41,5,25,52,59,61,51,37,92,90,20,20,96,66,79,28,83,"
          + "60,91,30,52,55,1,99,8,68,14,84,59,5,34,93,25,10,93,21,35,66,88,20,97,25,63,80,20,86,"
          + "33,53,43,86,53,55,61,77,9,2,56,78,43,19,68,69,49,1,6,5,82,46,24,33,85,24,56,51,45,100,"
          + "94,26,15,33,35,59,25,65,32,26,93,73,0,40,92,56,76,18,2,45,64,66,64,39,77,1,55,90,10,27,"
          + "85,40,95,78,39,40,62,30,12,57,84,95,86,57,41,52,77,17,9,15,33,17,68,63,59,40,5,63,30,86,"
          + "57,5,55,47,0,92,95,100,25,79,84,93,83,93,18,20,32,63,65,56,68,7,31,100,88,93,11,43,20,13,"
          + "54,34,29,90,50,24,13,44,89,57,65,95,58,32,67,38,2,41,4,63,56,88,39,57,10,1,97,98,25,45,96,"
          + "35,22,0,37,74,98,14,37,77,54,40,17,9,28,83,13,92,3,8,60,52,64,8,87,77,96,70,61,3,96,"
          + "83,56,5,99,81,94,3,38,91,55,83,15,30,39,54,79,55,86,85,32,27,20,74,91,99,100,46,69,77,"
          + "34,97,0,50,51,21,12,3,84,84,48,69,94,28,64,36,70,34,70,11,89,58,6,90,86,4,97,63,10,37,"
          + "48,68,30,29,53,4,91,7,56,63,22,93,69,93,1,85,11,20,41,36,66,67,57,76,85,37,80,99,63,23,"
          + "71,11,73,41,48,54,61,49,91,97,60,38,99,8,17,2,5,56,3,69,90,62,75,76,55,71,83,34,2,36,56,"
          + "40,15,62,39,78,7,37,58,22,64,59,80,16,2,34,83,43,40,39,38,35,89,72,56,77,78,14,45,0,57,"
          + "32,82,93,96,3,51,27,36,38,1,19,66,98,93,91,18,95,93,39,12,40,73,100,17,72,93,25,35,45,"
          + "91,78,13,97,56,40,69,86,69,99,4,36,36,82,35,52,12,46,74,57,65,91,51,41,42,17,78,49,75,"
          + "9,23,65,44,47,93,84,70,19,22,57,27,84,57,85,2,61,17,90,34,49,74,64,46,61,0,28,57,78,75,"
          + "31,27,24,10,93,34,19,75,53,17,26,2,41,89,79,37,14,93,55,74,11,77,60,61,2,68,0,15,12,47,"
          + "12,48,57,73,17,18,11,83,38,5,36,53,94,40,48,81,53,32,53,12,21,90,100,32,29,94,92,83,80,"
          + "36,73,59,61,43,100,36,71,89,9,24,56,7,48,34,58,0,43,34,18,1,29,97,70,92,88,0,48,51,53,0,"
          + "50,21,91,23,34,49,19,17,9,23,43,87,72,39,17,17,97,14,29,4,10,84,10,33,100,86,43,20,22,"
          + "58,90,70,48,23,75,4,66,97,95,1,80,24,43,97,15,38,53,55,86,63,40,7,26,60,95,12,98,"
          + "15,95,71,86,46,33,68,32,86,89,18,88,97,32,42,5,57,13,1,23,34,37,13,65,13,47,55,"
          + "85,37,57,14,89,94,57,13,6,98,47,52,51,19,99,42,1,19,74,60,8,48,28,65,6,12,57,"
          + "49,27,95,1,2,10,25,49,68,57,32,99,24,19,25,32,89,88,73,96,57,14,65,34,8,82,9,"
          + "94,91,19,53,61,70,54,4,66,26,8,63,62,9,20,42,17,52,97,51,53,19,48,76,40,80,"
          + "6,1,89,52,70,38,95,62,24,88,64,42,61,6,50,91,87,69,13,58,43,98,19,94,65,56,"
          + "72,20,72,92,85,58,46,67,2,23,88,58,25,88,18,92,46,15,18,37,9,90,2,38,0,16,"
          + "86,44,69,71,70,30,38,17,69,69,80,73,79,56,17,95,12,37,43,5,5,6,42,16,44,22,"
          + "62,37,86,8,51,73,46,44,15,98,54,22,47,28,11,75,52,49,38,84,55,3,69,100,54,"
          + "66,6,23,98,22,99,21,74,75,33,67,8,80,90,23,46,93,69,85,46,87,76,93,38,77,"
          + "37,72,35,3,82,11,67,46,53,29,60,33,12,62,23,27,72,35,63,68,14,35,27,98,94,"
          + "65,3,13,48,83,27,84,86,49,31,63,40,12,34,79,61,47,29,33,52,100,85,38,24,1,"
          + "16,62,89,36,74,9,49,62,89]", 29, 2818);

    x("[1,2,4,2,5,7,2,4,9,0]", 2, 13);

    x("[0,5,4,9,0,6]", 2, 15);

    x("[3,2,6,5,0,3]", 2, 7);

    x("[8,6,4,3,3,2,3,5,8,3,8,2,6]", 2, 11);

    x("[3,3]", 1, 0);

    x(2, 8, -1);

    x("[3,3,5,0,0,3,1,4]", 2, 6);
    x("[5,2]", 2, 0);
    x("[3,2,6,5,0,3]", 2, 7);
    x("[1,2,3,4,5]", 2, 4);
    x("[2,8,4,9,3,8]", 2, 12);
    x("[7,6,4,3,1]", 2, 0);
    x("[5]", 2, 0);
    x("[72]", 2, 0);
  }

  private List<Integer> bprices = new ArrayList<>();

  private void add(int... prices) {
    for (var price : prices)
      bprices.add(price);
  }

  private void dup(int times) {
    int sz = bprices.size();
    for (int i = 0; i < times; i++)
      bprices.addAll(bprices.subList(0, sz));
  }

  private void xa(int k, int expected) {
    loadTools();
    int[] prices = DataUtil.intArray(bprices);
    bprices.clear();
    x3(prices, k, expected);
  }

  private void x(int k, int count, int expected) {
    xs(2000 + count, k, count, expected);
    int[] prices = new int[count];
    for (int i = 0; i < count; i++) {
      prices[i] = rand().nextInt(1001);
    }
    x3(prices, k, expected);
  }

  private void xs(int seed, int k, int count, int expected) {
    rand(seed);
    int[] prices = new int[count];
    for (int i = 0; i < count; i++) {
      prices[i] = rand().nextInt(1001);
    }
    // pr(JSList.with(prices));
    x3(prices, k, expected);
  }

  private void x(String s, int k, int expected) {
    var prices = extractNums(s);
    x3(prices, k, expected);
  }

  private void x3(int[] prices, int k, int expected) {
    if (prices.length >= 500)
      checkpoint("Calculating max profit for n", prices.length);
    var result = maxProfit(k, prices);
    if (prices.length >= 500) {
      checkpoint("Done calculating");
      db("Cache requests:", cacheRequests, "Misses:", cacheMisses, "Miss %:",
          (cacheMisses * 100) / cacheRequests);
    }
    db("k:", k, "prices", darray(prices), "result:", result);
    if (expected < 0) {
      expected = new Slow().maxProfit(k, prices);
      //      pr("...expected is:", expected);
    }
    verify(result, expected);
  }

  private static class Slow {

    public int maxProfit(final int k, final int[] prices) {
      buySellPrices = findBuySellPoints(prices);
      db("prices:", darray(prices));
      db("buy/sell prices:", darray(buySellPrices));
      memo.clear();
      cacheMisses = 0;
      cacheAttempts = 0;
      var result = aux(k, 0);
      db("cache attempts:", cacheAttempts, "misses:", cacheMisses, "miss %:",
          ((cacheMisses * 100) / (cacheAttempts + 1)));
      return result;
    }

    /**
     * Determine best profit for choosing at most k transactions from the list
     * of buy/sell points starting at cursor c
     */
    private int aux(int k, int c) {
      if (k == 0 || c >= buySellPrices.length)
        return 0;
      cacheAttempts++;
      int key = (c << 7) | k;
      int profit = memo.getOrDefault(key, -1);
      if (profit < 0) {
        cacheMisses++;
        // Consider not using the next buy point,
        // or using it with any of the following sell points (but only if later sell points
        // have higher profit than earlier ones)
        profit = aux(k, c + 2);
        var bestPreviousProfit = 0;
        for (int d = c; d < buySellPrices.length; d += 2) {
          var thisProfit = buySellPrices[d + 1] - buySellPrices[c];
          // Optimization #1: skip later sell points that don't produce higher profits than an earlier one
          if (thisProfit < bestPreviousProfit)
            continue;
          bestPreviousProfit = thisProfit;
          var other = aux(k - 1, d + 2);
          var profit2 = thisProfit + other;
          if (profit2 > profit)
            profit = profit2;
        }
        memo.put(key, profit);
      }
      return profit;
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

    private int[] buySellPrices;
    private Map<Integer, Integer> memo = new HashMap<>();
    private int cacheAttempts;
    private int cacheMisses;

  }

  // ------------------------------------------------------------------

  public int maxProfit(final int k, final int[] prices) {
    var buySellPrices = findBuySellPoints(prices);
    db = prices.length < 10; // && !alert("always false");
    db("prices:", darray(prices));
    db("buy/sell prices:", darray(buySellPrices));
    mPoints = buildPoints(buySellPrices);
    db("points:", Arrays.asList(mPoints));

    initMemo(k);
    return aux(0, mPoints.length, k);
  }

  private void initMemo(int k) {
    memo.clear();
    cacheRequests = 0;
    cacheMisses = 0;
  }

  private static boolean cacheDisabled = false && alert("CACHE DISABLED!");

  private int aux(int ptStart, int ptEnd, int k) {
    db("aux", ptStart, "..", ptEnd, "k:", k);
    pushIndent();
    int result = -1;
    do {
      if (k == 0 || ptStart == ptEnd) {
        result = 0;
        break;
      }
      int key = k | (ptStart << 7) | (ptEnd << (7 + 10));
      result = memo.getOrDefault(key, -1);
      cacheRequests++;
      if (result < 0 || cacheDisabled) {
        cacheMisses++;
        result = calcAux(ptStart, ptEnd, k);
        db("...memoizing", ptStart, "..", ptEnd, "k:", k, "as:", result);
        memo.put(key, result);
      }
    } while (false);
    popIndent();
    db("...profit:", result);
    return result;
  }

  private int calcAux(int u, int v, int k) {
    db("calcAux, u:", u, "v:", v, "k:", k);
    if (db)
      db("trans:", Arrays.asList(Arrays.copyOfRange(mPoints, u, v)));
    if (k >= v - u) {
      var sum = 0;
      for (int i = u; i < v; i++)
        sum += mPoints[i].profit();
      db("...sum of all transactions");
      return sum;
    }
    if (k == 1)
      return findBestTransaction(u, v);
    if (k == 2)
      return findBestTwoTransactions(u, v);

    var k1 = k / 2;
    var k2 = k - k1;

    var n = v - u;

    // Find most profitable k1 transactions for each prefix up to length n
    var fwdResult = new int[n];
    for (int i = u; i < v; i++) {
      fwdResult[i - u] = aux(u, i + 1, k1);
    }
    // Find most profitable k2 transactions for each suffix
    var bwdResult = new int[n];
    for (int i = v - 1; i > u; i--) {
      bwdResult[i - u] = aux(i, v, k2);
    }

    int bestProfit = fwdResult[n - 1];
    for (int i = 0; i < n - 1; i++)
      bestProfit = Math.max(fwdResult[i] + bwdResult[i + 1], bestProfit);

    db("fwdResult:", fwdResult);
    db("bwdResult:", bwdResult);
    db("profit:", bestProfit);

    return bestProfit;
  }

  private int findBestTransaction(int u, int v) {
    pushIndent();
    db("best transaction", u, "..", v);

    var minBuyPrice = mPoints[u].buyPrice;
    var maxProfit = 0;
    for (int i = u; i < v; i++) {
      var pt = mPoints[i];
      //      var sellPrice = pt.sellPrice;
      minBuyPrice = Math.min(minBuyPrice, pt.buyPrice);
      maxProfit = Math.max(maxProfit, pt.sellPrice - minBuyPrice);
    }
    db("...result:", maxProfit);
    popIndent();
    return maxProfit;
  }

  private int findBestTwoTransactions(int u, int v) {

    //  todo("is it worth caching single result?");
    pushIndent();
    db("findBestTwoTrans, u:", u, "v:", v);

    int n = v - u;
    // Find most profitable transaction for each prefix up to length n
    var fwdResult = new int[n];
    {
      var minBuyPrice = mPoints[u].buyPrice;
      var maxProfit = 0;
      for (int i = u; i < v; i++) {
        var pt = mPoints[i];
        //        var sellPrice = mPoints[i].sellPrice;
        minBuyPrice = Math.min(pt.buyPrice, minBuyPrice);
        maxProfit = Math.max(maxProfit, pt.sellPrice - minBuyPrice);
        db("minPrice:", minBuyPrice, "price:", pt.sellPrice, "profit:", maxProfit);
        fwdResult[i - u] = maxProfit;
      }
    }

    // Find most profitable transaction for each suffix starting after 0...n-1

    var bwdResult = new int[n];
    {
      var maxSellPrice = mPoints[v - 1].sellPrice;
      var maxProfit = 0;
      // We can omit the first point on the backward pass, as the 
      // 'all points' result will be included in the last entry of the forward pass
      for (int i = v - 1; i > u; i--) {
        var pt = mPoints[i];
        //        var buyPrice = mPoints[i].buyPrice;
        //        var sellPrice = mPoints[i].sellPrice;
        maxSellPrice = Math.max(pt.sellPrice, maxSellPrice);
        maxProfit = Math.max(maxProfit, maxSellPrice - pt.buyPrice);
        // pr("i:", i, "maxPrice:", maxPrice, "maxProfit:", maxProfit);
        bwdResult[i - u] = maxProfit;
      }
    }
    db("fwdRes:", fwdResult);
    db("bwdRes:", bwdResult);

    int bestProfit = fwdResult[v - u - 1];
    for (int i = 0; i < v - u - 1; i++)
      bestProfit = Math.max(fwdResult[i] + bwdResult[i + 1], bestProfit);

    db("...bestProfit:", bestProfit);
    popIndent();

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

  private Pt[] buildPoints(int[] buySellPoints) {
    var out = new Pt[buySellPoints.length / 2];
    int j = 0;
    for (int i = 0; i < buySellPoints.length; i += 2)
      out[j++] = new Pt(buySellPoints[i], buySellPoints[i + 1]);
    return out;
  }

  private Pt[] mPoints;
  private Map<Integer, Integer> memo = new HashMap<>();
  private int cacheRequests;
  private int cacheMisses;

}
