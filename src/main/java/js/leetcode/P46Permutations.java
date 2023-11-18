package js.leetcode;

//
// This seems to be the most common solution, but it can be time-consuming on some inputs.
// Maybe a dynamic programming approach is best?  But if there are like 5000 steps possible,
// that's a huge number of actions, even for a dynamic programming approach.

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.json.JSList;

// 46. Permutations

public class P46Permutations {

  public static void main(String[] args) {
    new P46Permutations().run();
  }

  private void run() {
    x(5);
  }

  private static void swap(byte[] a, int i, int j) {
    byte tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
  }

  private void x(int n) {
    int[] nums = new int[n];
    for (int i = 1; i <= n; i++)
      nums[i - 1] = i;
    var result = permute(nums);
    for (var x : result) {
      pr(str(x));
    }
  }

  //  private byte[] workBuffer;
  private List<List<Integer>> output;

  private static String str(byte[] arr) {
    return str(arr, 0, arr.length);
  }

  private static String str(List<Integer> lst) {
    return JSList.withUnsafeList(lst).toString();
  }

  private static String str(byte[] arr, int start, int count) {
    return JSList.with(Arrays.copyOfRange(arr, start, start + count)).toString();
  }

  public List<List<Integer>> permute(int[] nums) {

    int n = nums.length;

    // The values fit into bytes
    byte[] bnums = new byte[n];
    for (int i = 0; i < n; i++)
      bnums[i] = (byte) nums[i];

    pr("permute [n]:", n, str(bnums));

    // Construct a work buffer large enough to hold |permute(n-1)|
    var workBuffer = allocBufferForPermute(n);
    var auxBuffer = allocBufferForPermute(n);

    buffers = new ArrayList<>();
    for (int i = 1; i < n; i++) {
      buffers.add(allocBufferForPermute(i));
    }

    int numResults = auxPermute(bnums, 0, n, workBuffer, auxBuffer);

    // Construct output array large enough to hold |permute(n)|
    output = new ArrayList<>(numResults);
    int srcCursor = 0;
    for (int i = 0; i < numResults; i++) {
      var x = new ArrayList<Integer>(n);
      for (int j = 0; j < n; j++) {
        x.add((int) (workBuffer[srcCursor]));
        srcCursor++;
      }
      output.add(x);
    }

    return output;
  }

  private static int factorial(int val) {
    if (val == 0)
      return 1;
    return val * factorial(val - 1);
  }

  private int auxPermute(byte[] sourceBuffer, int sourceBufferOffset, int n, byte[] destBuffer,
      byte[] destBuffer2) {
    //    pr(VERT_SP);
    //    pr("auxPermute, n=" + n, "source:", str(sourceBuffer, sourceBufferOffset, n));
    if (n == 1) {
      destBuffer[0] = sourceBuffer[sourceBufferOffset];
      return 1;
    }

    int suffixLength = n - 1;
    int suffixCount = -1; // This gets set to something positive below

    int destCursor = 0;

    // swap the first element with each element (including itself), then recursively permute the 2nd...n elements
    byte[] auxBuffer2 = auxBuffer(suffixLength);
    //    int auxid = lastid;
    for (int slot = 0; slot < n; slot++) {
      //      pr(VERT_SP, "slot:" + slot, " for auxPermute n=" + n);
      swap(sourceBuffer, sourceBufferOffset, sourceBufferOffset + slot);

      //      pr("pivot", slot, "after swap pivot, source:", str(sourceBuffer, sourceBufferOffset, n));

      //new byte[destBuffer.length];

      suffixCount = auxPermute(sourceBuffer, sourceBufferOffset + 1, suffixLength, destBuffer2, auxBuffer2);
      //      pr("auxPermute for n=" + suffixLength, "produced result count", suffixCount);
      //
      //      pr("destBuffer2:", str(destBuffer2));

      // For each suffix in the destination buffer, stitch results into destination buffer
      byte prefixValue = sourceBuffer[sourceBufferOffset];
      int sourceCursor = 0;

      for (int i = 0; i < suffixCount; i++) {
        destBuffer[destCursor] = prefixValue;
        for (int j = 0; j < suffixLength; j++) {
          destBuffer[destCursor + j + 1] = destBuffer2[sourceCursor + j];
        }
        sourceCursor += suffixLength;
        destCursor += n;
        //        pr("=========== stitched prefix value:", prefixValue, "to produce:",
        //            str(destBuffer, destCursor - n, n));
      }

      // reverse the suffix to undo the above permutation
      //reverse(sourceBuffer, sourceBufferOffset + 1, n - 1);
      // unswap the first element
      swap(sourceBuffer, sourceBufferOffset, sourceBufferOffset + slot);
      //      pr("after unswap pivot, source:", str(sourceBuffer, sourceBufferOffset, n));
    }
    // freeBuffer(auxid);
    return suffixCount * n;
  }

  private byte[] getWorkBuffer(int forResultsOfPermuteN) {
    int id = uniqueBufferId++;
    lastid = id;
    int n = forResultsOfPermuteN;
    int cap = factorial(n) * n;
    pr("alloc buffer", id, " with capacity:", cap, "count", workBufferMap.size());
    var result = new byte[cap];
    workBufferMap.put(id, result);
    return result;
  }

  private void freeBuffer(int id) {
    pr("free buffer", id);
    var value = workBufferMap.remove(id);
    checkState(value != null);
  }

  int uniqueBufferId = 50;
  int lastid;
  private Map<Integer, byte[]> workBufferMap = new HashMap<>();

  private byte[] auxBuffer(int n) {
    return buffers.get(n - 1);
  }

  private byte[] allocBufferForPermute(int n) {
    return new byte[factorial(n + 1)];
  }

  private ArrayList<byte[]> buffers;

}
