package js.leetcode;

// Use breadth-first search to find minimum moves to reach target.
//
// This seems to be the most common solution, but it can be time-consuming on some inputs.
// Maybe a dynamic programming approach is best?  But if there are like 5000 steps possible,
// that's a huge number of actions, even for a dynamic programming approach.

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

// 45. Jump Game II

public class P45JumpGameII {

  public static void main(String[] args) {
    new P45JumpGameII().run();
  }

  private void run() {
    x(2, 3, 0, 1, 4);
  }

  private void x(int... nums) {
    pr(nums);
    var answer = jump(nums);
    pr("answer:", answer);
  }

  public int jump(int[] nums) {
    if (nums.length == 1) return 0;
    List<Integer> stack = new ArrayList<>();
    List<Integer> stack2 = new ArrayList<>();
    boolean[] visited = new boolean[nums.length];
    int jumpCount = 0;
    stack.add(0);
    
    while (true) {
      jumpCount++;
      stack2.clear();
      for (int square : stack) {

        if (square == nums.length - 1)
          break;

        for (int jumpDistance = 1; jumpDistance <= nums[square]; jumpDistance++) {
          int target = square + jumpDistance;
          if (target == nums.length - 1)
            return jumpCount;
          if (target >= nums.length)
            continue;
          //pr("...jump by", jumpDistance, "gets to", target);

          if (visited[target]) {
            //pr("...already visited");
            continue;
          }
          visited[target] = true;
          stack2.add(target);
        }
      }
      var tmp = stack2;
      stack2 = stack;
      stack = tmp;
    }
  }

}
