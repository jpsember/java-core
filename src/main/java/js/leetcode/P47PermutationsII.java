package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.json.JSList;

// 47. Permutations II
//
// I check if the 'swap' operation is useless (won't produce new unique results), and skip that
// iteration.  I have to keep track of the unique swap values processed already, and I use
// some preallocated buffers for this purpose.
//

public class P47PermutationsII {

  public static void main(String[] args) {
    new P47PermutationsII().run();
  }

  private void run() {
    x(1, 2, 3);
  }

  private void x(int... nums) {

    var result = permuteUnique(nums);
    Set<String> us = new HashSet<>();
    for (var x : result) {
      var s = str(x);
      checkState(us.add(s), "duplicate value:", s);
      pr(s);
    }
  }

  private static String str(List<Integer> lst) {
    return JSList.withUnsafeList(lst).toString();
  }

  private List<List<Integer>> results;

  public List<List<Integer>> permuteUnique(int[] nums) {

    mWorkArrays = new int[nums.length][];
    for (int i = 0; i < nums.length; i++)
      mWorkArrays[i] = new int[nums.length - i];

    results = new ArrayList<>();
    auxPermute(nums, 0);
    return results;
  }

  private static void swap(int[] nums, int a, int b) {
    int tmp = nums[a];
    nums[a] = nums[b];
    nums[b] = tmp;
  }

  private void auxPermute(int[] nums, int index) {

    if (index == nums.length - 1) {
      var x = new ArrayList<Integer>(index + 1);
      for (int j = 0; j <= index; j++) {
        x.add(nums[j]);
      }
      results.add(x);
      return;
    }

    int firstElementsCount = 0;
    int[] firstElements = mWorkArrays[index];

    outer: for (int i = index; i < nums.length; i++) {

      // If we've already processed this value as the first element, 
      // skip

      int firstElementValue = nums[i];

      for (int k = 0; k < firstElementsCount; k++)
        if (firstElements[k] == firstElementValue) {
          continue outer;
        }
      firstElements[firstElementsCount++] = firstElementValue;

      swap(nums, index, i);
      auxPermute(nums, index + 1);
      swap(nums, index, i);
    }

  }

  private int[][] mWorkArrays;
}
