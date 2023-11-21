
package js.leetcode;

import static js.base.Tools.*;

public class P84LargestRectangleInHistogram {

  public static void main(String[] args) {
    new P84LargestRectangleInHistogram().run();
  }

  private void run() {
    x(3, 2, 1, 2);
    x(10, 2, 1, 5, 6, 2, 3);
    x(4, 2, 4);
    x(9, 9, 0);
    x(9, 5, 4, 3, 2, 1);
    x(9, 1, 2, 3, 4, 5);
    x(9, 1, 2, 3, 4, 5, 1, 2, 3);
  }

  private void x(int expectedArea, int... heights) {
    pr(heights);
    var answer = largestRectangleArea(heights);
    pr(heights, "answer:", answer);
    if (answer != expectedArea)
      halt("expected area was", expectedArea);
  }

  private void dump(Column c, String prompt) {
    Column head = c;
    while (head.prev != null)
      head = head.prev;

    pr(prompt);
    StringBuilder sb = new StringBuilder();

    Column w = head;
    while (w != null) {
      if (w == c)
        sb.append(">");
      else
        sb.append(" ");

      sb.append(w.height);

      sb.append("|");
      for (int x = 0; x < w.width; x++) {
        sb.append(":::: ");
      }
      sb.append("|");
      w = w.next;
    }
    pr(sb.toString());
  }

  static int uniqueId = 50;

  private static class Column {
    Column(int height) {
      this.height = height;
      this.width = 1;
    }

    int height;
    int width;
    Column prev;
    Column next;
    int id = uniqueId++;

    @Override
    public String toString() {
      return "#" + id + " ht:" + height + " wd:" + width;
    }

  }

  public int largestRectangleArea(int[] heights) {

    // Wrap heights in zero-height columns

    Column head = new Column(0);
    {
      Column current = head;
      for (int h : heights) {
        Column c = new Column(h);
        c.height = h;

        join(current, c);
        current = c;
      }
      join(current, new Column(0));
    }

    int maxArea = 0;

    Column c = head; //.next;

    dump(c, "starting iteration");

    while (true) {
      var cNext = c.next;
      if (cNext == null)
        break;

      //      // If all we have left are the original head and tail, we're done
      //      if (c == head && cNext == tail)
      //        break;

      if (cNext.height > c.height) {
        c = cNext;
        dump(c, "next is higher, moved to next");
        continue;
      } else if (cNext.height == c.height) {
        c.width += cNext.width;
        join(c, cNext.next);
        dump(c, "next is same height, merged with it");
      } else {
        int area = c.height * c.width;
        maxArea = Math.max(area, maxArea);

        int leftHeight = 0;
        if (c.prev != null)
          leftHeight = c.prev.height;
        int rightHeight = cNext.height;
        c.height = Math.max(leftHeight, rightHeight);
        dump(c, "next is lower; trimmed current; added candidate area:" + area);
        // Back up, if we can, in case current has dropped at or below its previous
        if (c.prev != null) {
          c = c.prev;
          dump(c, "moved to previous");
        }
      }
    }
    dump(c, "done; max area: " + maxArea);
    return maxArea;
  }

  private void join(Column a, Column b) {
    a.next = b;
    if (b != null)
      b.prev = a;
  }
}
