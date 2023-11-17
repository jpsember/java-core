
package js.leetcode;

// 42. Trapping Rain Water

import static js.base.Tools.*;

public class P42TrappingRainWater {

  public static void main(String[] args) {
    new P42TrappingRainWater().run();
  }

  private void run() {
//    x(0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1);
    x(4, 2, 0, 3, 2, 5);
  }

  private void x(int... height) {
    pr(height);
    var answer = trap(height);
    pr(height, "answer:", answer);
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

  public int trap(int[] height) {
    int z = 0;
    Column head = null;
    {
      Column tail = null;

      for (int h : height) {
        if (tail != null && h == tail.height) {
          tail.width++;
          continue;
        }
        Column c = new Column();
        c.height = h;
        c.width = 1;

        if (tail == null) {
          head = c;
          tail = c;
        } else {
          join(tail, c);
        }
        tail = c;
      }
      tail = null;
    }

    dump(head, "Initial chain");

    int poolVolume = 0;

    Column c = head;

    while (c.next != null) {
      dump(c, "Walking chain; volume:" + poolVolume);

      checkState(z++ < 100);
      if (c.next.height < c.height) {
        c = c.next;
        continue;
      }

      // We have found a NE movement.  
      // If there's nothing behind us, delete it and move forward.
      if (c.prev == null) {
        c = c.next;
        c.prev = null;
        head = c;
        continue;
      }

      int poolHeight = Math.min(c.prev.height, c.next.height) - c.height;
      checkState(poolHeight > 0);

      pr("heights:",c.prev.height,c.height,c.next.height);
      pr("curr width:",c.width);
      pr("volume:",poolHeight * c.width);
      
      poolVolume += poolHeight * c.width;

      // Merge the current column with the lower neighbor
      boolean mergeToLeft = (c.prev.height <= c.next.height);
      if (mergeToLeft) {
        c.prev.width += c.width;
        join(c.prev, c.next);
        c = c.prev;

        // If both sides were the same height, merge to right as well.
        if (c.height == c.next.height) {
          c.width += c.next.width;
          join(c, c.next.next);
        }
      } else {
        c.next.width += c.width;
        join(c.prev, c.next);
        c = c.next;
      }
    }
    return poolVolume;
  }

  private void join(Column a, Column b) {
    a.next = b;
    if (b != null) {
      b.prev = a;
    }
  }
}
