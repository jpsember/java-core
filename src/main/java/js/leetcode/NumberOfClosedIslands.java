package js.leetcode;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.PriorityQueue;

public class NumberOfClosedIslands extends LeetCode {

  public static void main(String[] args) {
    new NumberOfClosedIslands().run();
  }

  public void run() {
    x("[[1,1,1],[1,0,1],[1,1,1]]", 3, 1);

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

  static int uniqueId = 200;

  /**
   * This point class also acts as a queue.
   */
  private class Pt {
    Pt(int x, int y) {
      this.x = x;
      this.y = y;
      id = uniqueId++;
    }

    int read() {
      return mGrid[y][x];
    }

    /**
     * Push a point to the back of the queue; assumes 'this' is the current
     * back. Returns the new back.
     */
    Pt pushBack(int x, int y) {
      next = new Pt(x, y);
      return next;
    }

    /**
     * Pop point from the front of the queue. Returns the point as the new
     * front.
     */
    Pt popFront() {
      return next;
    }

    Pt next;
    int x, y;
    int id;

    @Override
    public String toString() {
      return "(#" + id + " " + x + " " + y + ")";
    }

    public boolean withinGrid() {
      return !(x < 0 || x >= width || y < 0 || y >= height);
    }

  }

  private String dumpQueue(Pt front, Pt back) {
    checkArgument(front != null);
    checkArgument(back != null);
    var ls = list();
    while (front != back) {
      checkState(front.next != null);
      var next = front.popFront();
      checkState(next != null);
      checkState(next.id > front.id);
      front = next;
      ls.add(front.id);
    }
    return "Queue: " + ls.prettyPrint();
  }

  // ------------------------------------------------------------------

  private static final int WATER = 1;
  private static final int LAND = 0;

  public int closedIsland(int[][] grid) {
    mGrid = grid;
    width = grid[0].length;
    height = grid.length;

    // Our queue will have a sentinel 'handle' as the front node, which is assumed to not
    // hold data.  So an empty queue is one with a non-null front which equals its back.

    var front = new Pt(0, 0);
    var back = front;

    // 'fill in' any land that touches the grid boundary.

    for (int y = 0; y < height; y++) {
      back = back.pushBack(0, y);
      back = back.pushBack(width - 1, y);
    }
    for (int x = 1; x < width - 1; x++) {
      back = back.pushBack(x, 0);
      back = back.pushBack(x, height - 1);
    }

    pr("queue:", INDENT, dumpQueue(front, back));

    while (front != back) {
      pr("queueBack:", back);
      pr("queueFront:", front, "next:", front.next);
      pr("queue:", INDENT, dumpQueue(front, back));

      front = front.popFront();
      pr("popped queueFront:", front);
      var pt = front;
      if (pt.withinGrid() && pt.read() == LAND) {
        paintLandAsWater(pt);
      }
    }

    // Scan for any remaining land, fill in when found, and increment island count
    var islandCount = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (mGrid[y][x] == LAND) {
          var pt = new Pt(x, y);
          islandCount++;
          paintLandAsWater(pt);
        }
      }
    }
    return islandCount;
  }

  private void paintLandAsWater(Pt pt) {
    checkInf(100);

    // Construct a new queue from a copy of this point
    var front = new Pt(0, 0);
    var back = front;
    back = back.pushBack(pt.x, pt.y);

    while (front != back) {
      front = front.next;
      if (!front.withinGrid())
        continue;
      var x = front.x;
      var y = front.y;
      if (front.read() != LAND)
        continue;
      mGrid[y][x] = WATER;
      back = back.pushBack(x - 1, y);
      back = back.pushBack(x + 1, y);
      back = back.pushBack(x, y - 1);
      back = back.pushBack(x, y + 1);
    }
  }

  private int[][] mGrid;
  private int width, height;

}
