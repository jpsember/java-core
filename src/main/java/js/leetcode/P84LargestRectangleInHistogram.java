
package js.leetcode;

import static js.base.Tools.*;

public class P84LargestRectangleInHistogram {

  public static void main(String[] args) {
    new P84LargestRectangleInHistogram().run();
  }

  private void run() {
    x(9, 0);
  }

  private void x(int... heights) {
    pr(heights);
    var answer = largestRectangleArea(heights);
    pr(heights, "answer:", answer);
  }

  private static class Column {
    int height;
    int width;
    Column prev;
    Column next;
  }

  public int largestRectangleArea(int[] heights) {

    // Have some zero-height sentinel columns as the head and tail
    Column head = new Column();
    Column tail = new Column();

    {
      Column current = head;
      for (int h : heights) {
        if (h == current.height) {
          if (current != head)
            current.width++;
          continue;
        }
        Column c = new Column();
        c.height = h;
        c.width = 1;

        join(current, c);
        current = c;
      }
      join(current, tail);
    }

    int maxArea = 0;

    Column c = head;

    while (true) {
      var cNext = c.next;
      if (cNext == null)
        break;

      // If all we have left are the original head and tail, we're done
      if (c == head && cNext == tail)
        break;

      if (cNext.height > c.height) {
        c = cNext;
        continue;
      }

      // If current is head, next is also zero; delete the next
      if (c == head) {
        join(c, cNext.next);
        continue;
      }

      // Next height is lower than current.
      // Calculate area of rectangle positioned at top of current as candidate,
      // then truncate it and add it to its predecessor.

      int area = c.height * c.width;
      maxArea = Math.max(area, maxArea);

      // Extend predecessor by current's width
      Column cp = c.prev;

      // Not strictly necessary, but avoid modifying original head (its height is zero so it doesn't matter what width it has)
      if (cp != head) {
        cp.width += c.width;
      }

      // delete current
      c = cp;
      join(cp, cNext);
    }
    return maxArea;
  }

  private void join(Column a, Column b) {
    a.next = b;
    b.prev = a;
  }
}
