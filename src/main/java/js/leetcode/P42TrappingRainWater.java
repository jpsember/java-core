
package js.leetcode;

// 42. Trapping Rain Water

import static js.base.Tools.*;

import js.base.Tools;

public class P42TrappingRainWater {

  public static void main(String[] args) {
    new P42TrappingRainWater().run();
  }

  private void run() {
    x(0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1);
  }

  private void x(int... height) {
    pr(height);
    var answer = trap(height);
    pr(height, "answer:", answer);
  }

  public int trap(int[] height) {

  }


}
