
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This is a computational geometry problem.
 * 
 * There are O(n^2) lines defined by points, so it is unlikely a solution exists
 * with running time better than that.
 * 
 * I will examine each pair of points, and determine a canonical line equation
 * for the line containing them. I will store a count of the number of points on
 * this line in a hash table.
 * 
 * There might be robustness issues due to roundoff; but I will deal with that
 * if the tests fail.
 */
public class P149MaxPointsOnALine {

  public static void main(String[] args) {
    new P149MaxPointsOnALine().run();
  }

  private void run() {
    x(4, 1, 1, 3, 2, 5, 3, 4, 1, 2, 3, 1, 4);
    x(14,
        "[[-424,-512],[-4,-47],[0,-23],[-7,-65],[7,138],[0,27],[-5,-90],[-106,-146],[-420,-158],[-7,-128],[0,16],[-6,9],[-34,26],[-9,-166],[-570,-69],[-665,-85],[560,248],[1,-17],[630,277],[1,-7],[-287,-222],[30,250],[5,5],[-475,-53],[950,187],[7,-6],[-700,-274],[3,62],[-318,-390],[7,19],[-285,-21],[-5,4],[53,37],[-5,-1],[-2,-33],[-95,11],[4,1],[8,25],[700,306],[1,24],[-2,-6],[-35,-387],[-630,-245],[-328,-260],[-350,-129],[35,299],[-380,-37],[-9,-9],[210,103],[7,-5],[-3,-52],[-51,23],[-8,-147],[-371,-451],[-1,-14],[-41,6],[-246,-184],[350,161],[-212,-268],[-140,-42],[-9,-4],[-7,5],[10,6],[-15,-191],[-7,-4],[318,342],[-8,-71],[-68,20],[6,119],[6,13],[-280,-100],[140,74],[-760,-101],[0,-24],[-70,-13],[0,2],[0,-9],[106,98]]");
  }

  private void x(int expected, String expr) {
    expr = expr.replace('[', ' ').replace(']', ' ').replace(" ", "");
    var strs = split(expr, ',');
    int[] points = new int[strs.size()];
    int i = INIT_INDEX;
    for (var s : strs) {
      i++;
      points[i] = Integer.parseInt(s);
    }
    x(expected, points);
  }

  private int slowMaxPoints(int[][] points) {
    if (points.length == 1)
      return 1;

    int maxcount = 0;
    for (int i = 0; i < points.length; i++) {
      int x1 = points[i][0];
      int y1 = points[i][1];
      for (int j = i + 1; j < points.length; j++) {
        int x2 = points[j][0];
        int y2 = points[j][1];

        double m;
        double b;
        boolean horz = true;
        double mag, perpx, perpy;
        if (Math.abs(x1 - x2) >= Math.abs(y1 - y2)) {
          m = (y2 - y1) / (double) (x2 - x1);
          b = y1 - m * x1;
          mag = Math.sqrt(m * m + 1);
          perpx = -m / mag;
          perpy = 1 / mag;

        } else {
          m = (x2 - x1) / (double) (y2 - y1);
          b = x1 - m * y1;
          horz = false;

          mag = Math.sqrt(m * m + 1);
          perpy = -m / mag;
          perpx = 1 / mag;
        }

        int kcount = 0;
        for (int k = 0; k < points.length; k++) {
          int x3 = points[k][0];
          int y3 = points[k][1];
          double dist;
          if (horz) {
            dist = (perpx * (x3 - 0) + perpy * (y3 - b));
          } else {
            dist = (perpy * (y3 - 0) + perpx * (x3 - b));
          }
          if (dist < 0.00002)
            kcount++;
        }
        maxcount = Math.max(maxcount, kcount);
      }
    }
    return maxcount;
  }

  private void x(int expected, int... points) {
    int[][] pts = new int[points.length / 2][];
    for (int i = 0; i < points.length; i += 2) {
      var y = new int[2];
      y[0] = points[i];
      y[1] = points[i + 1];
      pts[i / 2] = y;
    }
    var answer = maxPoints(pts);
    pr("points:", points, "answer:", answer);
    pr("slow answer:",slowMaxPoints(pts));
    checkState(answer == expected, "expected", expected);
  }

  public int maxPoints(int[][] points) {
    if (points.length == 1)
      return 1;
    var map = new HashMap<Long, List<Integer>>();
    List<Integer> maximalList = new ArrayList<>();
    for (int i = 0; i < points.length; i++) {
      int x1 = points[i][0];
      int y1 = points[i][1];
      for (int j = i + 1; j < points.length; j++) {
        int x2 = points[j][0];
        int y2 = points[j][1];

        double m;
        double b;
        if (Math.abs(x1 - x2) >= Math.abs(y1 - y2)) {
          m = 3.1 + (y2 - y1) / (double) (x2 - x1);
          b = y1 - m * x1;
        } else {
          m = 1.1 + (x2 - x1) / (double) (y2 - y1);
          b = x1 - m * y1;
        }
        int mKey = (int) (m * 0x8000);
        int bKey = (int) (b * 1000);
        long key = ((((long) mKey) & 0x7fffffff) << 32) | bKey;
        var ptList = map.get(key);
        if (ptList == null) {
          ptList = new ArrayList<>(2);
          map.put(key, ptList);
        }

        ptList.add(i);
        ptList.add(j);

        if (ptList.size() > maximalList.size())
          maximalList = ptList;
      }
    }
    var set = new HashSet<>(maximalList);
    return set.size();
  }
}
