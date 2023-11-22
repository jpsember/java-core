
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

// This works, but is still 6 times slower than most of the 'best' solutions.
//
public class P15ThreeSum {

  public static void main(String[] args) {
    new P15ThreeSum().run();
  }

  private void run() {
    checkpoint("start");

    // x(0, 0, 0);
    // x(1, 0, 0, 0, 0, 0, 1, -1, -1, 1);

    x(-1, 0, 1, 2, -1, -4);
    // That is supposed to produce [[-1,-1,2],[-1,0,1]]

    if (false)

      x(34, 55, 79, 28, 46, 33, 2, 48, 31, -3, 84, 71, 52, -3, 93, 15, 21, -43, 57, -6, 86, 56, 94, 74, 83,
          -14, 28, -66, 46, -49, 62, -11, 43, 65, 77, 12, 47, 61, 26, 1, 13, 29, 55, -82, 76, 26, 15, -29, 36,
          -29, 10, -70, 69, 17, 49);
    // x(-1, 0, 1, 2, -1, -4);

    //x(-1, 0, 1, 0);

    if (false) {
      int y = 20;
      int[] nums = new int[2 * y + 3];
      int i = 3;
      for (int j = 0; j < y; j++) {
        nums[i++] = 1 + j;
        nums[i++] = -(1 + j);
      }
      x(nums);
    }

    if (false) {
      var r = new Random(1965);
      int[] nums = new int[1000];

      final int numMag = 100000;

      for (int i = 0; i < nums.length; i++)
        nums[i] = r.nextInt(numMag * 2) - numMag;
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

  private int c3;

  public List<List<Integer>> threeSum(int[] nums) {
    result.clear();
    Arrays.sort(nums);

    // Find c, the minimum value
    int c = nums[0];
    for (int x : nums)
      if (x < c)
        c = x;
    c = -c;
    c3 = c * 3;

    int[] u = new int[nums.length];
    int[] w = new int[nums.length];

    int belowC = 0;
    int equalC = 0;
    int midC = 0;
    {
      for (int x : nums) {
        var p = x + c;
        if (p < c) {
          u[belowC++] = p;
        } else if (p == c)
          equalC++;
        else if (p <= c3) {
          w[midC++] = p;
        }
      }
    }
    if (equalC >= 3) {
      addSoln(0, 0, 0);
    }

    pr("nums:",nums);
    pr("c:", c, "3c:", c3, "u:", u, "w:", w,"below:",belowC,"midC:",midC,"equal:",equalC);

    var umap = buildFreqMap(u, belowC);
    var wmap = buildFreqMap(w, midC);
    pr("umap:", umap);
    pr("wmap:", wmap);
    
    
    for (int i = 0; i < belowC; i++) {
      var uVal = u[i];
    pr("u["+i+"]: ",uVal);
    
      {
        var wVal = 2 * c - uVal;
        pr("w:",wVal);
        if (wmap.containsKey(wVal)) {
          addSoln(uVal - c, c, wVal - c);
        }
      }
      var ww = findTwoSum(w, wmap, c3 - uVal);
      if (ww != null)
        addSoln(uVal - c, ww[0] - c, ww[1] - c);
    }

    for (int i = 0; i < midC; i++) {
      var wVal = w[i];
      var uu = findTwoSum(u, umap, c3 - wVal);
      if (uu != null)
        addSoln(uu[0] - c, uu[1] - c, wVal - c);
    }

    return new ArrayList<>(result.values());
  }

  private static int[] work = new int[2];

  private int[] findTwoSum(int[] list, Map<Integer, Integer> freq, int sum) {
    int size = freq.size();
    for (int i = 0; i < size; i++) {
      int a = list[i];
      int b = sum - a;
      Integer count = freq.get(b);
      if (count != null) {
        if (a == b && count < 2)
          break;
        work[0] = a;
        work[1] = b;
        return work;
      }
    }
    return null;
  }

  private Map<Integer, Integer> buildFreqMap(int[] a, int len) {
    var res = new HashMap<Integer, Integer>(len);
    for (int i = 0; i < len; i++) {
      var x = a[i];
      Integer count = res.get(x);
      if (count == null)
        count = 0;
      res.put(x, count + 1);
    }
    return res;
  }

  private void addSoln(int a, int b, int c) {
    pr("===>",a,b,c);
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