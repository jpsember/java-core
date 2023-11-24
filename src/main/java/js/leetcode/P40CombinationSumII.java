
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    if (true)
      x(30, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1);

  }

  private void x(int target, int... candidates) {
    var result = combinationSum2(candidates, target);
    pr(result);
  }

  public List<List<Integer>> combinationSum2(int[] coinValues, int target) {
    Arrays.sort(coinValues);

    // Build subsets of distinct values

    mValueSets = new HashMap<>();
    for (var value : coinValues) {
      Integer count = mValueSets.get(value);
      if (count == null)
        count = 0;
      mValueSets.put(value, count + 1);
    }

    mCoinValues = new int[mValueSets.size()];
    {
      int i = 0;
      for (var val : mValueSets.keySet()) {
        mCoinValues[i++] = val;
      }
    }

    mUniqueKeySet = new HashSet<>();
    resultList = new ArrayList<>();
    mResultBuffer = new ArrayList<Integer>(coinValues.length);
    aux(target, 0);
    return resultList;
  }

  /**
   * Helper function to support recursive calls
   * 
   * @param targetSum
   *          the sum of remaining coin values to be selected
   * @param coinCursor
   *          the index of the next coin type to be selected; ones prior to this
   *          are considered immutable
   */
  private void aux(int targetSum, int coinCursor) {
    //pr("target:", targetSum, "coinVals:", mCoinValues, "valueList:", mResultBuffer, "start:", coinCursor);

    // Base case: are there no more coin types to be selected?
    var coinTypes = mCoinValues.length;
    if (coinCursor == coinTypes) {
      // If we've reached the target value sum, we have a solution
      if (targetSum == 0) {
        ArrayList<Integer> copy = new ArrayList<>(mResultBuffer);

        var key = mUniqueKeyBuffer.toString();
        if (mUniqueKeySet.add(key))
          resultList.add(copy);
      }
      return;
    }

    int currentCoinValue = mCoinValues[coinCursor];

    // The number of copies of this coin we can include
    int frequency = mValueSets.get(currentCoinValue);

    // Make recursive call for case when we don't use any copies of this coin
    aux(targetSum, coinCursor + 1);

    // Save some values so we can restore them below
    var origLength = mResultBuffer.size();
    var origSbLen = mUniqueKeyBuffer.length();

    while (true) {
      targetSum -= currentCoinValue;
      if (targetSum < 0)
        break;
      mResultBuffer.add(currentCoinValue);
      mUniqueKeyBuffer.append(' ');
      mUniqueKeyBuffer.append(currentCoinValue);
      aux(targetSum, coinCursor + 1);

      frequency--;
      if (frequency == 0)
        break;

    }
    var sz = mResultBuffer.size();
    while (sz > origLength) {
      sz--;
      mResultBuffer.remove(sz);
    }
    mUniqueKeyBuffer.setLength(origSbLen);
  }

  private List<List<Integer>> resultList;
  private int[] mCoinValues;
  private StringBuilder mUniqueKeyBuffer = new StringBuilder();
  private Set<String> mUniqueKeySet;
  private ArrayList<Integer> mResultBuffer;
  private Map<Integer, Integer> mValueSets;

}