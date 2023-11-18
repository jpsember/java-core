package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.json.JSList;

// 47. Permutations II
//
// At present, this is just a better version of 46.

public class P47PermutationsII {

  public static void main(String[] args) {
    new P47PermutationsII().run();
  }

  private void run() {
    x(5);
  }

  private void x(int n) {
    int[] nums = new int[n];
    for (int i = 1; i <= n; i++)
      nums[i - 1] = i;
    var result = permute(nums);
    Set<String> us = new HashSet<>();
    for (var x : result) {
      var s = str(x);
      checkState(us.add(s));
      pr(s);
    }
  }

  private static String str(List<Integer> lst) {
    return JSList.withUnsafeList(lst).toString();
  }

  private List<List<Integer>> results;

  public List<List<Integer>> permute(int[] nums) {

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

    for (int i = index; i < nums.length; i++) {
      swap(nums, index, i);
      auxPermute(nums, index + 1);
      swap(nums,index,i);
    }

  }

}
