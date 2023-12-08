
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
 * 
 * 
 * Got it working; notes:
 * 
 * I did a lot of fiddling around with modular arithmetic, and it was confusing
 * and unnecessary, as I would subdivide the search region into three
 * subregions, none of which crossed the n-1..0 boundary.
 * 
 * I wasted a lot of time trying to avoid searching a particular value again,
 * i.e. searching (a,...b) then (a+1, ....) or (...,b-1), but these 'creeping
 * inward' values would produce subtle problems.
 * 
 * In conclusion, if there are no duplicates, we can quickly determine which
 * of three subregions contains the minimum value (or 'rotate point'), then
 * recursively search within that subregion again.
 * 
 * If there are duplicates, then if at least one of the three 'probe' searches
 * is different than the other two, we can still eliminate two thirds of the
 * search space.
 * 
 * If all (or almost all) values are the same, then the search may still take
 * linear (or O(n log n?)) time.  Otherwise, it is O(log n).
 */
public class P154FindMinimumInRotatedSortedArrayII {

  public static void main(String[] args) {
    loadTools();
    new P154FindMinimumInRotatedSortedArrayII().run();
  }

  private void run() {
    y("", "1", 22, "01");
    y("10", "1", 100, "");

    //    y("000000000000000000000000000000001","0",100,"0");
    //    y("01","0",100,"0");
    //   
    //      x(1965,500);
  }

  /* private */ void x(int seed) {
    x(seed, 16);
  }

  private void y(String prefix, String mid, int length, String suffix) {
    StringBuilder sb = new StringBuilder(prefix);
    int j = sb.length();
    while (sb.length() < j + length)
      sb.append(mid);
    sb.setLength(j + length);
    sb.append(suffix);
    x(sb.toString());
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
    int result = findMindb(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void y(int... nums) {
    int min = nums[0];
    for (var v : nums)
      min = Math.min(min, v);

    int result = findMindb(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void rotate(List<Integer> a) {
    a.add(a.remove(0));
  }

  private int findMindb(int[] nums) {
    mNums = nums;
    mReadCount = 0;
    minValueSoFar = null;
    auxFindMin(0, nums.length);
    pr("read count:", mReadCount, ((100 * mReadCount) / mNums.length) + "%");
    return minValueSoFar;
  }

  private int mReadCount;

  // ------------------------------------------------------------------

  public int findMin(int[] nums) {
    mNums = nums;
    minValueSoFar = null;
    auxFindMin(0, nums.length);
    return minValueSoFar;
  }

  private void auxFindMin(int a, int b) {
    int diff = b - a;
    if (diff <= 0)
      return;
    if (diff < 3) {
      for (int i = 0; i < diff; i++)
        get(i + a, "(range of < 3)");
      return;
    }
    int sep = diff / 3;
    int u = a;
    int v = a + sep;
    int w = b - sep;
    int nu = get(u, "u");
    int nv = get(v, "v");
    int nw = get(w, "w");
    if (nu > nv) {
      auxFindMin(u, v);
    } else if (nv > nw) {
      auxFindMin(v, w);
    } else if (nw > nu) {
      auxFindMin(w, b);
    } else {
      int oldMin = minValueSoFar;
      auxFindMin(u, v);
      if (oldMin == minValueSoFar)
        auxFindMin(v, w);
      if (oldMin == minValueSoFar)
        auxFindMin(w, b);
    }
  }

  private int get(int index, String prompt) {
    var nums = mNums;
    int effInd = index % nums.length;
    var value = nums[effInd];
    if (minValueSoFar == null || minValueSoFar > value)
      minValueSoFar = value;
    return value;
  }

  private Integer minValueSoFar;
  private int[] mNums;

}
