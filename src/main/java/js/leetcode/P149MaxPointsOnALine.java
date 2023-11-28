
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 
 * Second attempt:
 * 
 * I'm using integer math only by calculating, for each pair of points, the line
 * equation Ax + By + C = 0, then normalizing by:
 * 
 * negating all three if A < 0 (or A=0 && B < 0);
 * 
 * dividing A,B, and C by the gcd of the three numbers.
 * 
 */
public class P149MaxPointsOnALine {

  public static void main(String[] args) {

    new P149MaxPointsOnALine().run();
  }

  private void run() {

    x(5, "[[0,0],[4,5],[7,8],[8,9],[5,6],[3,4],[1,1]]");

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
    checkState(answer == expected, "expected", expected);
  }

  public int maxPoints(int[][] points) {
    if (points.length == 1)
      return 1;

    var pointsOnLinesMap = new HashMap<String, Set<Integer>>();

    int longestPointList = 0;

    var sb = new StringBuilder();

    for (int i = 0; i < points.length; i++) {
      int x1 = points[i][0];
      int y1 = points[i][1];
      for (int j = i + 1; j < points.length; j++) {
        int x2 = points[j][0];
        int y2 = points[j][1];

        var xd = x1 - x2;
        var yd = y1 - y2;

        var A = yd;
        var B = -xd;
        var C = -(A * x1 + B*y1);
pr("A:",A,"B:",B,"C:",C);

        // Find GCD of A,B,C
        var g = gcd(gcd(A, B), C);
        A /= g;
        B /= g;
        C /= g;

        if (A < 0 || (A == 0 && B < 0)) {
          A = -A;
          B = -B;
          C = -C;
        }

        sb.setLength(0);
        sb.append(A);
        sb.append(' ');
        sb.append(B);
        sb.append(' ');
        sb.append(C);

        var key = sb.toString();

        var pointsList = pointsOnLinesMap.get(key);
        if (pointsList == null) {
          pr("new key:", key,"for points",x1,y1,x2,y2);
          pointsList = new HashSet<>();
          pointsOnLinesMap.put(key, pointsList);
        } else {
        pr("additional points for key:",key, x1,y1,x2,y2);
        }

        pointsList.add(i);
        pointsList.add(j);

        longestPointList = Math.max(longestPointList, pointsList.size());
      }
    }
    return longestPointList;
  }

  private static int gcd(int a, int b) {
    a = Math.abs(a);
    b = Math.abs(b);
    if (a < b) {
      var tmp = a;
      a = b;
      b = tmp;
    }
    checkState(a != 0);
    while (b != 0) {
      if (a == b)
        return a;
      var c = a % b;
      a = b;
      b = c;
    }
    return a;
  }

}
