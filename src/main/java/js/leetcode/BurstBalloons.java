package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {
    x("[3,1,5,8]", 167);
    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]", 3394);
    x("[1,5]", 10);
    x("[5,6,7,1,2,3,0,6,12,20]");

  }

  private void x(String a) {
    x(a, null);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);
    x(nums, expected);
  }

  private void x(int[] nums, Integer expected) {
    db = nums.length < 12;

    Alg alg1 = new BestTriple();

    pr(toStr(nums));
    var res = alg1.maxCoins(nums);
    pr(INDENT, res);

    if (expected == null) {
      db = false;
      expected = new RecursionMemo().maxCoins(nums);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxCoins(int[] nums);

  }

  // ------------------------------------------------------------------

  class Node {
    int slot;
    int value;
    Node prev;
    Node next;

    Node(int value, int slot) {
      this.value = value;
      this.slot = slot;
    }

    public Node join(Node next) {
      this.next = next;
      next.prev = this;
      return next;
    }

    public String toString() {
      var ls = list();
      var c = this;
      if (c.prev == null)
        c = c.next;
      while (c.next != null) {
        ls.add(c.value);
        c = c.next;
      }
      return ls.toString();
    }

    public int popVal() {
      //      var left = 1;
      //      var right = 1;
      //      if (prev != null) left = prev.value;
      //      if (next != null) right = next.value;
      return prev.value * value * next.value;
    }

    public Node delete() {
      var ret = prev;
      prev.join(next);
      prev = null;
      next = null;
      return ret;
    }

    public Node insert(Node newNext) {
      var oldNext = next;
      join(newNext);
      newNext.join(oldNext);
      return newNext;
    }

    //    public String toString(Node stop) {
    //      var ls = list();
    //      var c = this;
    //      while (c != stop) {
    //        ls.add(c.value);
    //        c = c.next;
    //      }
    //      return ls.toString();
    //    }
  }

  class Recursion extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      return aux(nums, 0, nums.length);
    }

    private int aux(int[] nums, int start, int stop) {
      if (start == stop)
        return 0;

      pushIndent();
      db("aux", toStr(nums, start, stop));

      int bestAmt = -1;
      for (int i = start; i < stop; i++) {
        var amt = nums[i];
        db("candidate", i, amt);
        if (i > start) {
          db("mult left:", nums[i - 1], "*", amt);
          amt *= nums[i - 1];
        }
        if (i + 1 < stop) {
          db("mult right: * ", nums[i + 1]);
          amt *= nums[i + 1];
        }
        db("...pop amount", amt);

        var work = new int[stop - start - 1];
        int w = 0;
        for (int j = start; j < i; j++)
          work[w++] = nums[j];
        for (int j = i + 1; j < stop; j++)
          work[w++] = nums[j];

        amt += aux(work, 0, work.length);
        bestAmt = Math.max(amt, bestAmt);
      }
      db(INDENT, bestAmt);
      popIndent();
      return bestAmt;
    }

  }

  class Entry {
    Node node;
    int value;
    // Entry nextMove;
  }

  class RecursionMemo extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      var nodes = constructNodes(nums);
      var entry = aux(nodes[0]);
      return entry.value;
    }

    private StringBuilder sb = new StringBuilder();

    private Entry aux(Node node) {
      if (node.next.next == null)
        return null;

      sb.setLength(0);
      var c = node.next;
      while (c.next != null) {
        sb.append(c.value);
        sb.append(' ');
        c = c.next;
      }
      var key = sb.toString();
      var output = mMemo.get(key);
      if (output == null) {
        pushIndent();
        db("aux", node);

        output = new Entry();

        // Try popping each possible balloon

        var cursor = node.next;
        while (cursor.next != null) {
          var amt = cursor.popVal();

          var save = cursor.delete();
          //          //          
          //          //        for (int i = start; i < stop; i++) {
          //          //          var amt = nums[i];
          //          //          if (i > start) {
          //          //            amt *= nums[i - 1];
          //          //          }
          //          //          if (i + 1 < stop) {
          //          //            amt *= nums[i + 1];
          //          //          }
          //
          //          var work = new int[stop - start - 1];
          //          int w = 0;
          //          for (int j = start; j < i; j++)
          //            work[w++] = nums[j];
          //          for (int j = i + 1; j < stop; j++)
          //            work[w++] = nums[j];

          var recAnswer = aux(node);
          amt += recAnswer.value;
          if (output.value < amt) {
            output.value = amt;
            output.node = cursor;
          }

          // Reinsert the deleted node
          save.insert(cursor);
        }
        db(INDENT, output.value);
        popIndent();
        mMemo.put(key, output);
      }
      return output;
    }

    private Map<String, Entry> mMemo = new HashMap<>();

  }

  private Node[] constructNodes(int[] nums) {
    Node last = new Node(1, -1);
    Node first = last;
    for (int slot = 0; slot < nums.length; slot++) {
      var n = new Node(nums[slot], slot);
      last.join(n);
      last = n;
    }
    last = last.join(new Node(1, -1));
    var result = new Node[2];
    result[0] = first;
    result[1] = last;
    return result;
  }

  class BestTriple extends Alg {

    @Override
    public int maxCoins(int[] nums) {

      var nodes = constructNodes(nums);
      var first = nodes[0];
      var last = nodes[1];

      var result = 0;

      while (first.next != last) {
        db(first);
        Node best = null;
        int bestScore = 0;
        for (var n = first.next; n != last; n = n.next) {
          var score = n.prev.value * n.prev.value * n.value * n.next.value * n.next.value;
          db("...pop score", n.value, score);
          if (best == null || bestScore < score) {
            best = n;
            bestScore = score;
          }
        }

        var n = best;
        var popVal = n.prev.value * n.value * n.next.value;
        db("popping:", n.prev.value, " * [", n.value, "] *", n.next.value, " = ", popVal, " result now:",
            result + popVal);
        result += popVal;
        n.prev.join(n.next);
      }

      return result;
    }

  }
}
