
package js.leetcode;

import java.util.ArrayList;
import java.util.BitSet;
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
      y("","0",100,"10");
      y("01","0",100,"");
      
//    y("000000000000000000000000000000001","0",100,"0");
//    y("01","0",100,"0");
//   
//      x(1965,500);
  }

  private void x(int seed) {
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
    int result = findMin(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void y(int... nums) {
    int min = nums[0];
    for (var v : nums)
      min = Math.min(min, v);
    mRead.clear();
    int result = findMin(nums);
    pr(nums, "result:", result);
    checkState(result == min, "expected:", min);
  }

  private void rotate(List<Integer> a) {
    a.add(a.remove(0));
  }

  public int findMin(int[] nums) {
    mNums = nums;
    inf = 0;
    mReadCount = 0;
    minValueSoFar = null;
    auxFindMin(0, nums.length);

    pr("read count:", mReadCount, ((100 * mReadCount) / mNums.length) + "%");
    return minValueSoFar;
  }

  private void auxFindMin(int a, int b) {
    checkState(inf++ < 3000);
    int diff = b - a;
    if (diff <= 0)
      return;
    pr("auxFindMin a:", a, "b:", b, "diff:", diff, "nums.length:", mNums.length);
    if (diff < 3) {
      for (int i = 0; i < diff; i++) {
        get(i + a, "(range of < 3)");
      }
      return;
    }
    int sep = diff / 3;
    int u = a;
    int v = a + sep;
    int w = b - sep;
    //pr("...sep:", sep, "u:", u, "v:", v, "w:", w);
    checkState(u != v && v != w && w != u);
    int nu = get(u, "u");
    int nv = get(v, "v");
    int nw = get(w, "w");
    //pr("...u:", u, nu, "v:", v, nv, "w:", w, nw);
    if (nu > nv) {
      pr("...nu>nv, assuming in u...v region");
      auxFindMin(u + 1, v - 1);
    } else if (nv > nw) {
      pr("...nv>nw, assuming in v...w region");
      auxFindMin(v + 1, w - 1);
    } else if (nw > nu) {
      pr("...nw>nu, assuming in < u or > w region");
      auxFindMin(w + 1, b);
    } else {
      pr("...nu,nv,nw all same");
      int oldMin = minValueSoFar;
      pr("...scanning u+1..v-1");
      auxFindMin(u + 1, v - 1);
      if (oldMin == minValueSoFar) {
        pr("......haven't found new min, scanning v+1...w-1", v + 1, w - 1);
        auxFindMin(v + 1, w - 1);
      }
      if (oldMin == minValueSoFar) {
        pr("......haven't found new min, scanning w+1...b-1", w + 1, b - 1);
        //checkState(w + 1 < u - 1 + mNums.length);
        auxFindMin(w + 1, b - 1);
      }
    }
  }

  int inf;
  private int mReadCount;

  private int get(int index, String prompt) {
    var nums = mNums;
    int effInd = index % nums.length;
    if (mRead.get(effInd)) {
      pr("*** Already read index:", index, prompt);
      halt();
    } else
      mRead.set(effInd);
    mReadCount++;

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
  private int[] mNums;
  private BitSet mRead = new BitSet();
}
