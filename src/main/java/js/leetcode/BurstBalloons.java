package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  class Game {
    Game(int[] nums) {
      first = constructNodes(nums);
    }

    private Node first;
    private List<Node> moves = new ArrayList<Node>();
  }

  class Entry {
    Node node;
    int value;
    Entry nextMove;

    public String popSequence(int[] nums) {
      var sb = sb();
      var ent = this;
      var first = constructNodes(nums);

      var moves = new ArrayList<Node>();
      while (ent != null) {
        // sb.append("...ent slot:"+ ent.node.slot + "...");

        var iter = first.next;

        // var iter = first.next;
        var cursor = sb.length();
        while (iter.next != null) {
          //sb.append("iter.slot:"+iter.slot+")");
          tab(sb, cursor + iter.slot * 5);
          if (ent.node.slot == iter.slot)
            sb.append('*');
          sb.append(iter.value);
          iter = iter.next;
        }
        // Make move

        moves.add(ent.node);
        moves.add(ent.node.delete());

        sb.append('\n');
        ent = ent.nextMove;
      }

      for (int i = moves.size() - 2; i >= 0; i -= 2) {
        var del = moves.get(i);
        var prev = moves.get(i + 1);
        prev.insert(del);
      }

      return sb.toString();
    }
  }

  class RecursionMemo extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      var first = constructNodes(nums);
      var entry = aux(first);
      db(entry.popSequence(nums));
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

  private Node constructNodes(int[] nums) {
    Node last = new Node(1, -1);
    Node first = last;
    for (int slot = 0; slot < nums.length; slot++) {
      var n = new Node(nums[slot], slot);
      last.join(n);
      last = n;
    }
    last = last.join(new Node(1, -1));
    return first;
  }

  class BestTriple extends Alg {

    @Override
    public int maxCoins(int[] nums) {

      var first = constructNodes(nums);

      var result = 0;

      while (first.next.next != null) {
        db(first);
        Node best = null;
        int bestScore = 0;
        for (var n = first.next; n.next != null; n = n.next) {
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
