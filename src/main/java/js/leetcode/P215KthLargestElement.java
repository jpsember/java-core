package js.leetcode;

import static js.base.Tools.*;

public class P215KthLargestElement extends LeetCode {

  public static void main(String[] args) {
    new P215KthLargestElement().run();
  }

  public void run() {
    x("[2,1]", 1, 2);
    x("[-1,2,0]", 1, 2);
    x("[2,1]", 2, 1);

    x("[3,2,1,5,6,4]", 2, 5);
    x("[3,2,3,1,2,4,5,5,6]", 4, 4);
  }

  private void x(String numsStr, int k, int exp) {
    var nums = extractNums(numsStr);
    var result = findKthLargest(nums, k);
    pr("nums:", nums, "k:", k, "result:", result);
    verify(result, exp);
  }

  public int findKthLargest(int[] nums, int k) {
    // Construct an auxilliary buffer. We will be copying a subset of the original array to this buffer,
    // then switching to it for recursive calls.
    var auxBuff = new int[nums.length];

    return aux(nums, k, 0, nums.length, auxBuff);
  }

  private int aux(int[] nums, int k, int min, int max, int[] auxBuff) {
    int range = max - min;
    int pivot = nums[(min + max) / 2];
    int loInd = 0;
    int hiInd = range;

    for (int i = min; i < max; i++) {
      var v = nums[i];
      if (v < pivot)
        auxBuff[loInd++] = v;
      else if (v > pivot)
        auxBuff[--hiInd] = v;
    }

    var hiCount = range - hiInd;
    var loCount = loInd;
    var pivotCount = range - loCount - hiCount;

    if (k <= hiCount)
      return aux(auxBuff, k, hiInd, range, nums);
    k -= hiCount;
    if (k <= pivotCount)
      return pivot;
    k -= pivotCount;
    return aux(auxBuff, k, 0, loInd, nums);
  }
}
