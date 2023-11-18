package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.json.JSList;

// 46. Permutations
//
// Runtime beats 94.61%; memory 66.19%;
// 
// But there is a much better solution that doesn't use auxillary buffers.  
// I implemented it in P47.
//

public class P46Permutations {

  public static void main(String[] args) {
    new P46Permutations().run();
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

  public List<List<Integer>> permute(int[] nums) {

    int n = nums.length;

    // The values fit into bytes
    byte[] bnums = new byte[n];
    for (int i = 0; i < n; i++)
      bnums[i] = (byte) nums[i];

    // Construct a work buffer large enough to hold |permute(n-1)|
    var destBuffer = allocBufferForPermute(n);

    buffers = new ArrayList<>();
    for (int i = 1; i < n; i++) {
      buffers.add(allocBufferForPermute(i));
    }

    int numResults = auxPermute(bnums, 0, n, destBuffer, allocBufferForPermute(n));

    // Construct output array large enough to hold |permute(n)|

    List<List<Integer>> output = new ArrayList<>(numResults);
    int srcCursor = 0;
    for (int i = 0; i < numResults; i++) {
      var x = new ArrayList<Integer>(n);
      for (int j = 0; j < n; j++) {
        x.add((int) destBuffer[srcCursor++]);
      }
      output.add(x);
    }
    return output;
  }

  private int auxPermute(byte[] sourceBuffer, int sourceBufferOffset, int n, byte[] destBuffer,
      byte[] auxBuffer) {

    if (n == 1) {
      destBuffer[0] = sourceBuffer[sourceBufferOffset];
      return 1;
    }

    int suffixLength = n - 1;
    int suffixCount = -1; // This gets set to something positive below
    int destCursor = 0;

    // swap the first element with each element (including itself), then recursively permute the 2nd...n elements

    byte[] auxBuffer2 = buffers.get(suffixLength - 1);

    for (int slot = 0; slot < n; slot++) {
      int s0 = sourceBufferOffset;
      int s1 = sourceBufferOffset + slot;
      byte s0Val = sourceBuffer[s0];
      byte s1Val = sourceBuffer[s1];
      sourceBuffer[s0] = s1Val;
      sourceBuffer[s1] = s0Val;

      suffixCount = auxPermute(sourceBuffer, sourceBufferOffset + 1, suffixLength, auxBuffer, auxBuffer2);

      // For each suffix in the destination buffer, stitch results into destination buffer
      byte prefixValue = sourceBuffer[sourceBufferOffset];
      int sourceCursor = 0;

      for (int i = 0; i < suffixCount; i++) {
        destBuffer[destCursor] = prefixValue;
        for (int j = 0; j < suffixLength; j++) {
          destBuffer[destCursor + j + 1] = auxBuffer[sourceCursor + j];
        }
        sourceCursor += suffixLength;
        destCursor += n;
      }

      // undo the swap we did earlier
      sourceBuffer[s0] = s0Val;
      sourceBuffer[s1] = s1Val;
    }
    return suffixCount * n;
  }

  private byte[] allocBufferForPermute(int n) {
    // Construct room for n! arrays, each of length n
    int f = 1;
    for (int i = 1; i <= n; i++)
      f *= i;
    return new byte[f * n];
  }

  private ArrayList<byte[]> buffers;

}
