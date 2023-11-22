
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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

    if (true) {
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
    var result = sorted(threeSum(nums));
    pr(CR, result, CR, "count:", result.size(), "steps:", steps);
    if (!result.equals(verified)) {
      halt(CR, "*** Expected this instead:", CR, verified, CR, "count:", verified.size());
    }
    pr(CR);
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
    unique.clear();

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

    return new ArrayList<>(result);
  }

  final boolean shortCircuit = true;
  
  public List<List<Integer>> threeSum(int[] nums) {

    result.clear();
    unique.clear();
    steps = 0;

    Arrays.sort(nums);

    // pr("sorted:", nums);
    int a = 0;
    int b = nums.length - 1;

    while (a < b) {
      int va = nums[a];
      int vb = nums[b];
      if ((va ^ vb) >= 0)
        break;

      pr(VERT_SP, "[a " + a + "]:", va, "[b " + b + "]:", vb);

      for (var a1 = a; a1 + 1 < b; a1++) {
        steps++;

        int va1 = nums[a1];
        int vc = 0 - (va1 + vb);
        pr(" [a1 " + a1 + "]:", va1, "vc:", vc);

        int k = Arrays.binarySearch(nums, a1 + 1, b, vc);

        if (k >= 0) {

          // This is causing things to fail
          if (vc < va1 || vc > vb) {
            halt("found soln at k:", k, "for vc:", vc, "va1:", va1, "vb:", vb);
          }

          addSoln(va1, vb, vc);
        } else if (shortCircuit) {
          // Short circuit: 
          // If the insertion position for this missing value is to the left, we won't find any more results 
          // stepping further to the right.
          if (-k - 1 <= a1)
            break;
        }
      }

      for (var b1 = b - 1; b1 - 1 > a; b1--) {
        steps++;
        int vb1 = nums[b1];
        int vc = 0 - (va + vb1);
        pr(" [b1 " + b1 + "]:", vb1, "vc:", vc);

        // This is causing things to fail
        //                if (vc < va || vc > vb1)
        //                break;
        int k = Arrays.binarySearch(nums, a + 1, b1, vc);
        pr("k:", k);
        if (k >= 0) {
          // This is causing things to fail
          if (vc < va || vc > vb1) {
            halt("found soln at k:", k, "for vc:", vc, "va:", va, "vb:", vb1);
          }
          addSoln(va, vb1, vc);
        }  else if (shortCircuit) {
          // Short circuit: 
          // If the insertion position for this missing value is to the right, we won't find any more results 
          // stepping further to the left.
          if (-k - 1 >= b1)
            break;
        }
      }
      a++;
      b--;
    }

    // Add 0,0,0 if there are at least three zeros
    {
      int k = Arrays.binarySearch(nums, 0);
      pr("search for zero:", k);
      if (k >= 0) {
        while (k > 0 && nums[k - 1] == 0)
          k--;
        if (k + 2 < nums.length && nums[k + 1] == 0 && nums[k + 2] == 0) {
          addSoln(0, 0, 0);
        }
      }
    }

    return result;
  }

  private void addSoln(int a, int b, int c) {
    final long minNum = -100000;

    long key = (a - minNum) + ((b - minNum) << 17) + ((c - minNum) << (17 * 2));
    if (!unique.add(key))
      return;

    List<Integer> soln = new ArrayList<>();

    soln.add(a);
    soln.add(b);
    soln.add(c);
    result.add(soln);
  }

  private List<List<Integer>> result = new ArrayList<>();
  private Set<Long> unique = new HashSet<>();
  private int steps;
}