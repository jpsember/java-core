
package js.leetcode;

import static js.base.Tools.*;

public class P84LargestRectangleInHistogram {

  public static void main(String[] args) {
    new P84LargestRectangleInHistogram().run();
  }

  private void run() {
    x(2, 1, 5, 6, 2, 3);
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

  private void dump(Column c, String prompt) {
    Column head = c;
    while (head.prev != null)
      head = head.prev;

    pr(VERT_SP, prompt);
    StringBuilder sb = new StringBuilder();

    Column w = head;
    while (w != null) {
      if (w == c)
        sb.append(">");
      else
        sb.append(" ");

      sb.append(" " + w.height);

      sb.append("|");
      for (int x = 0; x < w.width; x++) {
        sb.append(":::: ");
      }
      sb.append("|");
      w = w.next;
    }
    pr(sb.toString());
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

    dump(head, "Initial chain");

    int maxArea = 0;

    Column c = head.next;

    while (true) {
      var cNext = c.next;
      if (cNext == null)
        break;
      dump(c, "Walking chain; maxArea:" + maxArea);

      // If all we have left are the original head and tail, we're done
      if (c == head && cNext == tail)
        break;
      if (cNext.height > c.height) {
        c = cNext;
        continue;
      }

      // Next height is lower than current.
      // Calculate area of rectangle positioned at top of current as candidate,
      // then truncate it and add it to its predecessor.

      int area = c.height * c.width;
      maxArea = Math.max(area, maxArea);
      // pr("next height is lower; area of previous block:", area, "newmax:", maxArea);

      // Extend predecessor by current's width
      Column cp = c.prev;

      // Not strictly necessary, but avoid modifying original head (its height is zero so it doesn't matter what width it has)
      if (cp != head) {
        cp.width += c.width;
      }

      // delete current
      c = cp;
      join(cp, cNext);
      dump(c, "deleted current");
    }
    return maxArea;
  }

  private void join(Column a, Column b) {
    a.next = b;
    b.prev = a;
  }
}
