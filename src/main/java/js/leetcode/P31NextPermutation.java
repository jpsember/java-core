
package js.leetcode;

// 31. Next Permutation
//

import static js.base.Tools.*;
import static js.data.DataUtil.*;

import java.util.Arrays;

import js.json.JSList;

public class P31NextPermutation {

  public static void main(String[] args) {
    new P31NextPermutation().run();
  }

  private void run() {
         x(1, 5, 1);
        
  }

  private void x(int... nums) {
    int[] n = new int[nums.length];
    for (int i = 0; i < nums.length; i++)
      n[i] = nums[i];
    pr(VERT_SP, "before permute:", INDENT, JSList.with(n));
    nextPermutation(n);
    pr("after:", INDENT, JSList.with(n));
  }

  public void nextPermutation(int[] nums) {

    if (nums.length == 1)
      return;

    for (int i = nums.length - 2; i >= 0; i--) {

      int jBest = 0;

       
      for (int j = i + 1; j < nums.length; j++) {
        //pr("i:"+i,"j:"+j,"ni:"+nums[i],"nj:"+nums[j],"jbest:"+jBest);

        // right digit must be greater than left to be a candidate
        if (nums[i] >= nums[j])
          continue;

        
        if (jBest != 0 && nums[jBest] <  nums[j])
          continue;

        jBest = j;
        //pr("...setting jbest to",jBest);
      }
      if (jBest != 0) {
        //pr("swapping",i,jBest);
        swap(nums, i, jBest);

        // Sort elements following the swap target into lowest possible order
        Arrays.sort(nums, i + 1, nums.length);
        return;
      }
    }

    // No candidates were found, so put array into lowest possible order (by sorting)
    Arrays.sort(nums);
  }

  private static void swap(int[] a, int i, int j) {
    int tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
  }
}
