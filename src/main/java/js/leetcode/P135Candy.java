
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;

import js.json.JSList;

/**
 * First stab is sort by rating, and assign lowest-rated children candy as
 * necessary; as each child is assigned, look at its neighbors and ensure candy
 * count is high enough.
 * 
 * We will never end up assigning an insufficient candy to a child since we
 * never assign a lower-rated child candy after a higher-rated one.
 * 
 * Ok, that works, but I am in the 10th percentile of speed. There must be a
 * more clever algorithm.
 * 
 * Came up with a linear algorithm that looks for strictly decreasing rating
 * sequences to the right of the current child to determine the lower bound to
 * that side.
 * 
 * Did some reading, and there is a more elegant solution that does a full forward
 * scan, then a full backward scan.  (Not quicker asymptotically).
 */
public class P135Candy {

  public static void main(String[] args) {
    new P135Candy().run();
  }

  private void run() {
    x(1, 3, 2, 2, 1);
    // x(1, 0, 2);
    // x(1, 2, 2);
  }

  private void x(int... ratings) {
    pr("ratings:", ratings);
    int expected = slowCandy(ratings);
    int result = candy(ratings);
    pr(VERT_SP, "n:", ratings.length, "result:", result);
    checkState(result == expected, "expected:", expected);
  }

  private int slowCandy(int[] ratings) {
    var prevCandy = 0;
    int sum = 0;
    int cursor = 0;

    while (cursor < ratings.length) {
      var rating = ratings[cursor];
      int candy = 1;

      if (cursor > 0 && ratings[cursor - 1] < rating) {
        candy = prevCandy + 1;
      }

      int j = 0;
      while (cursor + j + 1 < ratings.length && ratings[cursor + j + 1] < ratings[cursor + j])
        j++;
      if (j != 0) {
        sum += (j * (j + 1)) / 2;
        candy = Math.max(candy, j + 1);
        cursor += 1 + j;
        prevCandy = 1;
      } else {
        cursor++;
        prevCandy = candy;
      }
      sum += candy;
    }

    return sum;
  }

  public int candy(int[] ratings) {
    var prevCandy = 0;
    int sum = 0;
    int cursor = 0;

    while (cursor < ratings.length) {
      var rating = ratings[cursor];
      //pr("cursor:", cursor, "sum:", sum, "prevCandy:", prevCandy, "rating:", rating);

      int candy = 1;

      if (cursor > 0 && ratings[cursor - 1] < rating) {
        candy = prevCandy + 1;
        //pr("...required candy", candy, "to be higher than previous");
      }

      // Scan ahead to determine the length of the maximal
      // *strictly decreasing* ratings ahead of this one.
      // Each child in this chain (of length j) can receive
      // candy amounts (j, j-1, j-2, ..., 1).

      int j = 0;
      while (cursor + j + 1 < ratings.length && ratings[cursor + j + 1] < ratings[cursor + j])
        j++;
      //pr("...forward decreasing ratings has length j", j);
      if (j != 0) {
        sum += (j * (j + 1)) / 2;
        if (candy <= j)
          candy = j + 1;
        cursor += 1 + j;
        prevCandy = 1;
      } else {
        cursor++;
        prevCandy = candy;
      }
      sum += candy;
    }

    return sum;
  }
}
