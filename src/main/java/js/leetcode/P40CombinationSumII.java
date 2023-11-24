
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Running out of time on some inputs.
// Memoization?
//
public class P40CombinationSumII {

  public static void main(String[] args) {
    new P40CombinationSumII().run();
  }

  private void run() {
    x(8, 10, 1, 2, 7, 6, 1, 5);

    x(30, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

  }

  private void x(int target, int... candidates) {
    var result = combinationSum2(candidates, target);
    pr(result);
  }

  public List<List<Integer>> combinationSum2(int[] coinValues, int target) {
    Arrays.sort(coinValues);
    unique = new HashSet<>();
    resultList = new ArrayList<>();
    mCoinValues = coinValues;
    var result = new ArrayList<Integer>(coinValues.length);
    aux(target, 0, result);
    return resultList;
  }

  private StringBuilder sb = new StringBuilder();
  private Set<String> unique;

  /**
   * Helper function to support recursive calls
   * 
   * @param coinUsedFlags
   * @param targetSum
   *          the sum of remaining coin values to be selected
   * @param coinCountStart
   *          the index of the next coin type to be selected; ones prior to this
   *          are considered immutable
   */
  private void aux(int targetSum, int coinCountStart, List<Integer> valueList) {
    //    pr(VERT_SP, "target:", targetSum, "coinVals:", mCoinValues, "valueList:", valueList, "start:",
    //        coinCountStart);
    pr("target", targetSum, "cs", coinCountStart);

    // Base case: are there no more coin types to be selected?
    var coinTypes = mCoinValues.length;
    if (coinCountStart == coinTypes) {
      // If we've reached the target value sum, we have a solution
      if (targetSum == 0) {
        ArrayList<Integer> copy = new ArrayList<>(valueList);

        var key = sb.toString();
        if (unique.add(key))
          resultList.add(copy);
      }
      return;
    }

    
    aux(targetSum, coinCountStart + 1, valueList);
    int currentCoinValue = mCoinValues[coinCountStart];
    //pr("coinValue #" + coinCountStart + ":", currentCoinValue);
    if (targetSum >= currentCoinValue) {
      valueList.add(currentCoinValue);
      var len = sb.length();
      sb.append(' ');
      sb.append(currentCoinValue);
      aux(targetSum - currentCoinValue, coinCountStart + 1, valueList);
      sb.setLength(len);
      valueList.remove(valueList.size() - 1);
    }
  }

  private List<List<Integer>> resultList;
  private int[] mCoinValues;

}