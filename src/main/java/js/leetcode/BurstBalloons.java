package js.leetcode;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.Map;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {
    x("[2,5,10,20,10,5,2]");
//    x("[1,5]", 10);
//    x("[3,1,5,8]", 167);
//    x("[5,6,7,1,2,3,0,6,12,20]");
//    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]", 3394);
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

    Alg alg1 = new RecursionMemo();

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
      // pr("...deleting node", slot, "(prev:", prev.slot, ")");
      var ret = prev;
      prev.join(next);
      prev = null;
      next = null;
      return ret;
    }

    public Node insert(Node newNext) {
      // pr("...inserting node", newNext.slot, "after", slot, "...");
      var oldNext = next;
      join(newNext);
      newNext.join(oldNext);
      return newNext;
    }
  }

  class Entry {
    Node node;
    int value;
    Entry nextMove;

    public String popSequence() {
      var sb = sb();
      var ent = this;
      while (ent != null) {
        sb.append(ent.node.slot).append(".").append(ent.node.value).append("  ");
        ent = ent.nextMove;
      }
      return sb.toString();
    }
  }

  class RecursionMemo extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      var nodes = constructNodes(nums);
      var entry = aux(nodes[0]);
      db(entry.popSequence());
      return entry.value;
    }

    private Entry aux(Node node) {
      if (node.next.next == null)
        return null;

      var key = node.toString();
      var output = mMemo.get(key);
      if (output == null) {
        pushIndent();
        db("aux", node, "{");

        output = new Entry();

        // Try popping each possible balloon

        var cursor = node.next;
        while (cursor.next != null) {
          var amt = cursor.popVal();
          var save = cursor.delete();
          var recAnswer = aux(node);
          if (recAnswer != null)
            amt += recAnswer.value;
          if (output.value < amt) {
            output.value = amt;
            output.node = cursor;
            output.nextMove = recAnswer;
          }
          // Reinsert the deleted node
          save.insert(cursor);
          cursor = cursor.next;
        }
        db(output.value, "}");
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
