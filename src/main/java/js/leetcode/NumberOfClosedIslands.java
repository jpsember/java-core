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

  private class Pt {
    Pt(int x, int y) {
      this.x = x;
      this.y = y;
      id = uniqueId++;
    }

    int read() {
      pr("reading grid:", this);
      return gr[y][x];
    }

    Pt pushBack(int x, int y) {
      return pushBack(new Pt(x, y));
    }

    Pt pushBack(Pt pt) {
      pr("...pushing:", pt);
      next = pt;
      return next;
    }

    Pt popFront() {
      checkState(next != null);
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

  private static final int WATER = 1;
  private static final int LAND = 0;

  private int[][] gr;
  private int width, height;

  public int closedIsland(int[][] grid) {
    gr = grid;
    width = grid[0].length;
    height = grid.length;

    var queueFront = new Pt(0, 0);
    var queueBack = queueFront;

    // 'fill in' any land that touches the grid boundary.

    for (int y = 0; y < height; y++) {
      queueBack = queueBack.pushBack(0, y);
      queueBack = queueBack.pushBack(width - 1, y);
    }
    for (int x = 1; x < width - 1; x++) {
      queueBack = queueBack.pushBack(x, 0);
      queueBack = queueBack.pushBack(x, height - 1);
    }

    pr("queue:", INDENT, dumpQueue(queueFront, queueBack));

    while (queueFront != queueBack) {
      pr("queueBack:", queueBack);
      pr("queueFront:", queueFront, "next:", queueFront.next);
      pr("queue:", INDENT, dumpQueue(queueFront, queueBack));

      queueFront = queueFront.popFront();
      pr("popped queueFront:", queueFront);
      var pt = queueFront;
      if (pt.withinGrid() && pt.read() == LAND) {
        paintLandAsWater(pt);
      }
    }

    // Scan for any remaining land, fill in when found, and increment island count
    var islandCount = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (gr[y][x] == LAND) {
          var pt = new Pt(x, y);
          islandCount++;
          paintLandAsWater(pt);
        }
      }
    }
    return islandCount;
  }

  private String dumpQueue(Pt front, Pt back) {
    checkArgument(front != null);
    checkArgument(back != null);
    var ls = list();
    while (front != back) {
      checkState(front.next != null);
      //      if (front.next == null) {
      //        ls.add("<< no next!!!!");
      //        break;
      //      }
      var next = front.popFront();
      checkState(next != null);
      checkState(next.id > front.id);
      front = next;
      ls.add(front.id);
    }
    return "Queue: " + ls.prettyPrint();
  }

  private void paintLandAsWater(Pt pt) {
    checkInf(100);

    // Construct a new queue from a copy of this point
    var qf = new Pt(0, 0);
    var qb = qf;
    qb = qb.pushBack(pt.x, pt.y);

    while (qf != qb) {
      qf = qf.next;
      var p = qf;
      pr("painting, pt:", p);
      if (!p.withinGrid())
        continue;
      var x = p.x;
      var y = p.y;
      if (p.read() != LAND)
        continue;
      gr[y][x] = WATER;
      pr("...painted as water:", p);
      qb = qb.pushBack(x - 1, y);
      qb = qb.pushBack(x + 1, y);
      qb = qb.pushBack(x, y - 1);
      qb = qb.pushBack(x, y + 1);
    }
  }

}
