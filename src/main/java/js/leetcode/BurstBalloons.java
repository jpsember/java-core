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
    x("[1,3,7,7,1,7]");

    x("[3,1,5,8]", 167);
    x("[2,5,10,20,10,5,2]");
    x("[1,5]", 10);

    //    x("[3,7,11,7,3,7,11,7,3]");
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

    Alg alg1 = new BestProfit();

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
      pr("constructed nodes with slot count:", nums.length);
    }

    void makeMove(Node popNode) {
      moves.add(popNode);
      moves.add(popNode.delete());
    }

    void unmove() {
      int i = moves.size() - 2;
      var restored = moves.get(i);
      var prev = moves.get(i + 1);
      prev.insert(restored);
      moves.remove(i + 1);
      moves.remove(i);
    }

    int moveCount() {
      return moves.size() / 2;
    }

    private Node first;
    private List<Node> moves = new ArrayList<Node>();

    public Node nodeForSlot(int slot) {
      var x = first;
      while (x != null) {
        if (x.slot == slot)
          return x;
        x = x.next;
      }
      throw badState("no node found for slot:", slot);
    }
  }

  class Entry {
    Node node;
    int value;
    Entry nextMove2;

    public void setNextMove(Entry entry) {
      checkState(entry.node.slot != node.slot, "duplicate slot!");
      nextMove2 = entry;
    }

    public Entry getNextMove() {
      return nextMove2;
    }

    public String popSequence(int[] nums) {
      var sb = sb();
      var ent = this;

      var game = new Game(nums);
      pr("building pop sequence for Entry");

      while (ent != null) {
        pr("...current entry value:", ent.value, "node slot, val:", ent.node.slot, ent.node.value);
        var cursor = sb.length();

        // Iterate over all nodes in the current game state
        var n = game.first.next;
        while (n.next != null) {
          tab(sb, cursor + n.slot * 4);
          sb.append(ent.node.slot == n.slot ? '*' : ' ');
          sb.append(n.value);
          n = n.next;
        }

        // Make move
        pr("...attempting to make move with slot:", ent.node.slot);
        game.makeMove(game.nodeForSlot(ent.node.slot));
        sb.append('\n');
        ent = ent.getNextMove();
        pr("...moved to next move in list");
      }

      while (game.moveCount() != 0)
        game.unmove();

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
            if (recAnswer != null)
              output.setNextMove(recAnswer);
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

      Entry movesHead = null;
      Entry movesTail = null;

      while (first.next.next != null) {
        db(first);
        Node best = null;
        int bestScore = 0;
        for (var n = first.next; n.next != null; n = n.next) {
          var score = n.popVal();
          //  var score = n.prev.value * n.prev.value * n.value * n.next.value * n.next.value;
          db("...pop score", n.value, score);
          if (best == null || bestScore < score) {
            best = n;
            bestScore = score;
          }
        }

        var n = best;
        var popVal = n.popVal();
        db("popping:", n.prev.value, " * [", n.value, "] *", n.next.value, " = ", popVal, " result now:",
            result + popVal);
        result += popVal;

        var ent = new Entry();
        ent.node = best;
        ent.value = result;
        if (movesTail == null) {
          movesHead = ent;
          movesTail = ent;
        } else {
          movesTail.setNextMove(ent);
          movesTail = movesTail.getNextMove();
        }
        n.delete();
      }
      pr(movesHead.popSequence(nums));
      return result;
    }

  }

  class BestProfit extends Alg {

    @Override
    public int maxCoins(int[] nums) {

      var first = constructNodes(nums);

      var result = 0;

      Entry movesHead = null;
      Entry movesTail = null;

      while (first.next.next != null) {
        db(first);
        Node best = null;
        int bestScore = 0;
        for (var n = first.next; n.next != null; n = n.next) {

          int a, b, c, d, e;
          {
            var prev = n.prev;
            var next = n.next;
            a = 0;
            if (prev.prev != null)
              a = prev.prev.value;
            b = prev.value;
            c = n.value;
            d = next.value;
            e = 0;
            if (next.next != null)
              e = next.next.value;
          }

          var score = b * c * d + a * b * (d - c) + d * e * (b - c);
          db("...pop score", n.value, score);
          if (best == null || bestScore < score) {
            best = n;
            bestScore = score;
          }
        }

        var n = best;
        var popVal = n.popVal();
        db("popping:", n.prev.value, " * [", n.value, "] *", n.next.value, " = ", popVal, " result now:",
            result + popVal);
        result += popVal;

        var ent = new Entry();
        ent.node = best;
        ent.value = result;
        if (movesTail == null) {
          movesHead = ent;
          movesTail = ent;
        } else {
          movesTail.setNextMove(ent);
          movesTail = movesTail.getNextMove();
        }
        n.delete();
      }
      pr(movesHead.popSequence(nums));
      return result;
    }

  }
}
