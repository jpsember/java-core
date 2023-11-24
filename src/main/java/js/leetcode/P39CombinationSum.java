
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.data.DataUtil;

// I thought at first that I could use linear algebra, but a) the system of linear equations
// is under constrained, so the matrix is invertible; and b) we are only interested in INTEGER
// solutions.
//
// Given the relatively small number of candidates and target, I think a set of states is the best
// approach.
//
// I realize now that this is the integer knapsack problem.
//
// First attempt: runs slower than 95% of the accepted solutions.
//
// Second attempt: uses recursive approach, where each iteration handles a particular 'coin value';
// now in 99th percentile
//
public class P39CombinationSum {

  public static void main(String[] args) {
    new P39CombinationSum().run();
  }

  private void run() {
    x(7, 2, 3, 6, 7);
    //  x(1, 2);
  }

  private void x(int target, int... candidates) {
    var result = combinationSum(candidates, target);
    pr(result);
  }

  public List<List<Integer>> combinationSum(int[] coinValues, int target) {
    //pr("combinationSum, coinValues:", coinValues, "target:", target);
    resultList = new ArrayList<>();
    mCoinValues = coinValues;
    mCoinValueCount = coinValues.length;
    int[] counts = new int[mCoinValueCount];
    aux(counts, target, 0);
    return resultList;
  }

  /**
   * Helper function to support recursive calls
   * 
   * @param coinCounts
   *          the counts for each coin value
   * @param targetSum
   *          the sum of remaining coin values to be selected
   * @param coinCountStart
   *          the index of the next coin type to be selected; ones prior to this
   *          are considered immutable
   */
  private void aux(int[] coinCounts, int targetSum, int coinCountStart) {
    //pr(VERT_SP, "target:", target, "coinVals:", mCoinValues, "counts:", coinCounts, "start:", coinCountStart);

    // Base case: are there no more coin types to be selected?
    if (coinCountStart == mCoinValueCount) {
      // If we've reached the target value sum, we have a solution
      if (targetSum == 0) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < mCoinValueCount; i++) {
          var coinValue = mCoinValues[i];
          int count = coinCounts[i];
          for (int j = 0; j < count; j++)
            result.add(coinValue);
        }
        resultList.add(result);
      }
      return;
    }

    int currentCoinValue = mCoinValues[coinCountStart];
    //pr("coinValue #" + coinCountStart + ":", c);

    var newTargetSum = targetSum;
    while (newTargetSum >= 0) {
      aux(coinCounts, newTargetSum, coinCountStart + 1);
      coinCounts[coinCountStart]++;
      newTargetSum -= currentCoinValue;
    }
    // Restore current coin's count to zero for subsequent recursive calls
    coinCounts[coinCountStart] = 0;
  }

  private int mCoinValueCount;
  private List<List<Integer>> resultList;
  private int[] mCoinValues;

}