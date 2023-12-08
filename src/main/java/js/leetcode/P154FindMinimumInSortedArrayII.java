
package js.leetcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import js.data.DataUtil;
import static js.base.Tools.*;

/**
 * Thoughts:
 * 
 * Worst case running time cannot be better than O(n), since if the list was a
 * single value followed by n-1 duplicates of second value, after rotating, each
 * value must be examined to find the single value.
 * 
 * Maybe a binary search that is prepared for an "impossible" state (one that
 * wouldn't occur if the list was sorted, i.e., before the rotations).
 *
 * The binary search should be looking for the rotation index, which I guess is
 * equivalent to finding the smallest value anyways.
 * 
 * Unlike a binary search, I think we want to divide the array into three equal
 * sections, and just one of those will contain the 'rotation point', which
 * whose neighbors are the largest and smallest values.
 * 
 * I think I want to use modular arithmetic to do psuedo rotations, to simplify
 * the binary search parameters.
 */
public class P154FindMinimumInSortedArrayII {

  public static void main(String[] args) {
    loadTools();
    new P154FindMinimumInSortedArrayII().run();
  }

  private void run() {
    x("7777777777777777777777777777777777777777777776777777777");
    y(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 6, 7, 7, 7, 7,
        7, 7);
    x(1965);
  }

  private void x(int seed) {
    x(seed, 16);
  }

  private void x(String vals) {
    List<Integer> a = new ArrayList<>();
    for (int i = 0; i < vals.length(); i++)
      a.add(vals.charAt(i) - '0');
    y(DataUtil.intArray(a));
  }

  private void x(int seed, int length) {
    List<Integer> a = new ArrayList<>();
    var r = new Random(seed);
    int prev = 0;
    for (int i = 0; i < length; i++) {
      var value = r.nextInt(89) + 10;
      if (prev != 0 && r.nextInt(3) == 0) {
        value = prev;
      }
      a.add(value);
      prev = value;
    }
    a.sort(null);
    int min = a.get(0);
    int rot = r.nextInt(length);
    while (rot-- > 0)
      rotate(a);
    var nums = DataUtil.intArray(a);
    int result = findMin(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void y(int... nums) {
    int min = nums[0];
    for (var v : nums)
      min = Math.min(min, v);
    int result = findMin(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void rotate(List<Integer> a) {
    a.add(a.remove(0));
  }

  public int findMin(int[] nums) {
    minValueSoFar = null;
    return auxFindMin(nums, 0, 0, nums.length);
  }

  private int auxFindMin(int[] nums, int modIndex, int a, int b) {
    checkState(modIndex == 0);
    int diff = b - a;
    pr("auxFindMin, modIndex:", modIndex, "a:", a, "b:", b, "diff:", diff);
    if (diff < 3) {
      for (int i = a; i < b; i++) {
        get(nums, modIndex, i, "(range of < 3)");
      }
      return minValueSoFar;
    }
    int sep = diff / 3;
    int u = a;
    int v = a + sep;
    int w = b - sep;
    pr("sep:", sep, "u:", u, "v:", v, "w:", w);
    checkState(u != v && v != w && w != u);
    pr("...reading u:", u);
    int nu = get(nums, modIndex, u, "u");
    pr("...reading v:", v);
    int nv = get(nums, modIndex, v, "v");
    pr("...reading w:", w);
    int nw = get(nums, modIndex, w, "w");
    pr("...u:", u, nu, "v:", v, nv, "w:", w, nw);
    if (nu > nv) {
      pr("...nu>nv, assuming in u...v region");
      return auxFindMin(nums, modIndex, u + 1, v - 1);
    } else if (nv > nw) {
      pr("...nv>nw, assuming in v...w region");
      return auxFindMin(nums, modIndex, v + 1, w - 1);
    } else if (nw > nu) {
      pr("...nw>nu, assuming in < u or > w region");
      //var modAdj = w + 1;
      return auxFindMin(nums, modIndex, w + 1, u - 1 + nums.length);
    } else {
      pr("...nu,nv,nw all same");
      int oldMin = minValueSoFar;
      pr("...scanning u+1..v-1");
      auxFindMin(nums, modIndex, u + 1, v - 1);
      if (oldMin == minValueSoFar) {
        pr("......haven't found new min, scanning v+1...w-1");
        auxFindMin(nums, modIndex, v + 1, w - 1);
      }
      if (oldMin == minValueSoFar) {
       // var modAdj = w + 1;
        pr("......haven't found new min, scanning w+1...u-1");
        auxFindMin(nums, modIndex , w+1,  u - 1 +nums.length);
      }
      return minValueSoFar;
    }
  }

  private int get(int[] nums, int modIndex, int index, String prompt) {
    int effInd = index - modIndex;
    if (effInd < 0)
      effInd += nums.length;
    checkState(effInd >= 0 && effInd < nums.length);
    var value = nums[effInd];
    boolean upd = false;
    if (minValueSoFar == null || minValueSoFar > value) {
      upd = true;
      minValueSoFar = value;
    }
    pr("....read", prompt, " nums[", effInd, "]=", value, upd ? "*** new min" : "");
    return value;
  }

  private Integer minValueSoFar;

}
