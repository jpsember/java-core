package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.PriorityQueue;

public class NumberOfClosedIslands extends LeetCode {

  public static void main(String[] args) {
    new NumberOfClosedIslands().run();
  }

  public void run() {
    x("[[1,1,1,1,1,1,1,0],[1,0,0,0,0,1,1,0],[1,0,1,0,1,1,1,0],[1,0,0,0,0,1,0,1],[1,1,1,1,1,1,1,0]]", 8, 2);
  }

  private void x(String numsStr, int width, int k) {
    x(extractMatrix(numsStr, width), k);
  }

  private void x(int[][] grid, int expected) {
    db = true;

    var res = closedIsland(grid);
    verify(res, expected);
  }

  public int closedIsland(int[][] grid) {
    return 7;
  }
}
