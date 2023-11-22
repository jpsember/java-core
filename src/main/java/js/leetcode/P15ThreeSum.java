
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

// A new algorithm that partitions the numbers into regions such that 
// a solution must be x numbers from one region and y numbers from another;
// it uses 2Sum as a subroutine
//
public class P15ThreeSum {

  public static void main(String[] args) {
    new P15ThreeSum().run();
  }

  private void rand(int seed, int count) {
    var r = new Random(seed);
    int[] nums = new int[count];

    final int numMag = 1000;

    for (int i = 0; i < nums.length; i++)
      nums[i] = r.nextInt(numMag * 2) - numMag;
    x(nums);
  }

  private void run() {
    checkpoint("start");

    x(-12, -4, 8);

    x(-647, -566, -499, -278, -258, -120, -64, -44, -26, -4, 18, 140, 322, 327, 405, 502, 742, 889, 928, 996);

    x(-8, -6, 3, 8, 10);
    x(-747, -289, 709, 747, 840);
    rand(340, 5);

    for (int s = 1; s < 800; s++) {
      pr(VERT_SP, "seed:", s);
      rand(s + 4000, 20);
    }

    //  rand(1965,1000);

    // x(0, 0, 0);
    // x(1, 0, 0, 0, 0, 0, 1, -1, -1, 1);

    // x(-1, 0, 1, 2, -1, -4);

    x(34, 55, 79, 28, 46, 33, 2, 48, 31, -3, 84, 71, 52, -3, 93, 15, 21, -43, 57, -6, 86, 56, 94, 74, 83, -14,
        28, -66, 46, -49, 62, -11, 43, 65, 77, 12, 47, 61, 26, 1, 13, 29, 55, -82, 76, 26, 15, -29, 36, -29,
        10, -70, 69, 17, 49);
    // x(-1, 0, 1, 2, -1, -4);

    //x(-1, 0, 1, 0);

    {
      int y = 20;
      int[] nums = new int[2 * y + 3];
      int i = 3;
      for (int j = 0; j < y; j++) {
        nums[i++] = 1 + j;
        nums[i++] = -(1 + j);
      }
      x(nums);
    }

    checkpoint("stop");
  }

  private void x(int... nums) {
    var verified = sorted(slow(nums));
    pr(CR);

    var result = sorted(threeSum(nums));
    /**/pr(CR, result, CR, "count:", result.size());
    if (!result.equals(verified)) {
      halt(CR, "*** Expected this instead:", CR, verified, CR, "count:", verified.size());
    }
    /**/pr(CR);
  }

  private List<List<Integer>> sorted(List<List<Integer>> nums) {
    Map<String, List<Integer>> mp = new TreeMap<>();
    for (var x : nums) {
      x.sort(null);
      var key = x.get(0) + " " + x.get(1) + " " + x.get(2);
      checkState(mp.put(key, x) == null);
    }
    return new ArrayList<>(mp.values());
  }

  private List<List<Integer>> slow(int[] nums) {
    result.clear();
    Arrays.sort(nums);
    int n = nums.length;
    for (int a = 0; a < n; a++) {
      int va = nums[a];
      if (va > 0)
        break;
      for (int b = a + 1; b < n; b++) {
        int vb = nums[b];
        int sum = va + vb;
        if (sum > 0)
          break;
        for (int c = b + 1; c < n; c++) {
          int vc = nums[c];
          if (sum + vc == 0) {
            addSoln(va, vb, vc);
          }
        }
      }
    }

    return new ArrayList<>(result.values());
  }

  public List<List<Integer>> threeSum(int[] nums) {
    result.clear();
    Arrays.sort(nums);

    // Find c, the minimum value
    int c = nums[0];
    for (int x : nums)
      if (x < c)
        c = x;
    c = -c;
    var c3 = c * 3;

    int[] u = new int[nums.length];
    int[] w = new int[nums.length];

    int belowC = 0;
    int equalC = 0;
    int midC = 0;

    for (int x : nums) {
      var p = x + c;
      if (p < c) {
        u[belowC++] = x;
      } else if (p == c)
        equalC++;
      else if (p <= c3) {
        w[midC++] = x;
      }
    }
    u = Arrays.copyOfRange(u, 0, belowC);
    w = Arrays.copyOfRange(w, 0, midC);

    if (equalC >= 3)
      addSoln(0, 0, 0);

    var umap = buildFreqMap(u);
    var wmap = buildFreqMap(w);

    for (var uVal : u) {
      if (equalC > 0) {
        var wVal = -uVal;
        if (wmap.containsKey(wVal))
          addSoln(uVal, 0, wVal);
      }
      findTwoSums(w, wmap, -uVal);
    }

    for (var wVal : w)
      findTwoSums(u, umap, -wVal);

    return new ArrayList<>(result.values());
  }

  private void findTwoSums(int[] list, Map<Integer, Integer> freq, int sum) {
    // We want to avoid choosing the pairs (a,b) AND (b,a)
    Integer prevA = null;

    for (var a : list) {
      int b = sum - a;
      if (prevA != null && b == prevA)
        break;
      Integer count = freq.get(b);
      if (count != null) {
        if (a == b && count < 2)
          break;
        addSoln(a, b, -sum);
        prevA = a;
      }
    }
  }

  private Map<Integer, Integer> buildFreqMap(int[] a) {
    var res = new HashMap<Integer, Integer>(a.length);
    for (int x : a) {
      Integer count = res.get(x);
      if (count == null)
        count = 0;
      res.put(x, count + 1);
    }
    return res;
  }

  private void addSoln(int a, int b, int c) {
    final long minNum = -100000;
    long key = (a - minNum) + ((b - minNum) << 17) + ((c - minNum) << (17 * 2));
    List<Integer> soln = new ArrayList<>(3);
    soln.add(a);
    soln.add(b);
    soln.add(c);
    result.put(key, soln);
  }

  private Map<Long, List<Integer>> result = new HashMap<>();

}