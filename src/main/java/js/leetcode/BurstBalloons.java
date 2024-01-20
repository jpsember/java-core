package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.base.BasePrinter;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {

    //    {
    //      int[] nums = { 3, 1, 5, 8 };
    //      var g = new Bgame(nums);
    //      g.moveSlot(2);
    //      g.moveSlot(1);
    //      pr(g);
    //      halt();
    //
    //    }
    xo("[2,8,9,7,3]", 9, 7, 8, 2, 3);

    //    x(5,8,10,8,5,8,10,8,5);
    //    halt();
    x(1, 1, 1, 2, 3, 4, 5, 6, 17, 6);
    halt();
    x(5, 6, 7, 8, 9, 10, 11, 12, 11, 10, 8, 6, 4);
    halt();
    halt();
    x(1, 8, 9, 7, 2);
    halt();
    //    x(4, 3, 4, 2);

    x(1, 2, 3, 4, 2, 1);
    halt();
    x(2, 3, 4, 5, 7, 6, 2, 1);
    //x(1, 2, 1, 4);
    x(3, 5, 10, 11, 9, 5, 3);
    halt();
    x(3, 5, 9, 4, 11, 13, 7, 10, 4, 2);
    halt();
    x(4, 3, 4, 5, 6, 5);
    x(11, 5, 4);

    x("[11,5,4,8]");

    int s = 126;
    for (int y = 3; y < 20; y++) {
      pr(VERT_SP, "y:", y);
      xSeed(y * s + 17, y, null);
    }
    x("[1,1,1,10,2,0,1,1,1]");

    x("[10,2,0]");
    x("[1,3,7,7,1,7]");

    x("[3,1,5,8]", 167);
    x("[2,5,10,20,10,5,2]");
    x("[1,5]", 10);

    xSeed(1965, 300, 78683782);

    //    x(1965, 300, null);
    //    x("[3,7,11,7,3,7,11,7,3]");
    //    x("[5,6,7,1,2,3,0,6,12,20]");
    //    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]", 3394);

  }

  private void xSeed(int seed, int count, Integer expected) {
    var nums = new int[count];
    rand(seed);
    for (int i = 0; i < count; i++) {
      nums[i] = rand().nextInt(20);
    }
    if (false && count > 3) {
      nums[0] = 1;
      nums[1] = 1;
      nums[count - 1] = 1;
      nums[count - 2] = 1;
    }

    x(nums, expected);
  }

  private void x(int... nums) {
    x(nums, null);
  }

  private void x(String a) {
    x(a, null);
  }

  private void xo(String a, int... sequence) {
    var nums = extractNums(a);

    var game = new Bgame(nums);

    //    var sb = sb();
    var total = 0;
    for (var n : sequence) {
      game.moveNumber(n);
      //      var cursor = sb.length();
      //
      //      var node = game.nodeForValue(n);
      //
      //      // Iterate over all nodes in the current game state
      //      var nz = game.first.next;
      //      while (nz.next != null) {
      //        tab(sb, cursor + nz.slot * 4);
      //        sb.append(nz.slot == node.slot ? '*' : ' ');
      //        sb.append(nz.value);
      //        nz = nz.next;
      //      }
      //
      //      total += node.popVal();
      //      game.makeMove(node);
      //
      //      sb.append('\n');
    }
    //    sb.append("total: ").append(total);
    pr(game);

    db = false;
    var alg2 = new RecursionMemo();
    var expected = alg2.maxCoins(nums);
    pr(alg2);
    verify(total, expected);
  }

  private void x(String a, Integer expected) {
    var nums = extractNums(a);
    x(nums, expected);
  }

  private void x(int[] nums, Integer expected) {
    db = nums.length < 12;

    Alg alg1 = new BestProfit();

    boolean rec = false;
    if (rec)
      alg1 = new RecursionMemo();

    pr(toStr(nums));
    var res = alg1.maxCoins(nums);
    pr(INDENT, res);

    if (rec)
      expected = res;
    if (expected == null) {
      db = false;
      var alg2 = new RecursionMemo();
      expected = alg2.maxCoins(nums);
      pr(alg2);
    }

    verify(res, expected);
  }

  private abstract class Alg {

    public abstract int maxCoins(int[] nums);

  }

  // ------------------------------------------------------------------

  private class RecursionMemo extends Alg {

    @Override
    public int maxCoins(int[] nums) {
      var first = constructNodes(nums);
      var value = aux(first);
      var entry = aux(first);
      mFirstMove = entry;
      mNums = nums;
      db("recursion:", DASHES, INDENT, entry.popSequence(nums), CR, DASHES);
      return value.value;
    }

    @Override
    public String toString() {
      if (mFirstMove != null) {
        return BasePrinter.toString("recursion:", DASHES, INDENT, mFirstMove.popSequence(mNums), CR, DASHES);
      }
      return "RecursionMemo";
    }

    private Move mFirstMove;
    private int[] mNums;

    private Move aux(Node node) {
      var cursor = node.next;
      if (cursor.isRight())
        return null;

      var key = node.memoKey();
      var output = mMemo.get(key);
      if (output == null) {
        pushIndent();
        db("aux", node, "{");

        output = new Move();

        // Try popping each possible balloon

        while (!cursor.isRight()) {
          var amt = cursor.popVal();
          var save = cursor.delete();
          var recAnswer = aux(node);
          if (recAnswer != null) {
            amt += recAnswer.value;
          }
          if (output.value < amt) {
            output.value = amt;
            output.slot = cursor.slot;
            output.nextMove = recAnswer;
          }
          // Reinsert the deleted node
          save.insert(cursor);
          cursor = cursor.next;
        }
        db(output.value, "}");
        popIndent();
        db("storing key:", key, "=>", output);
        mMemo.put(key, output);
      }
      return output;
    }

    private Map<Object, Move> mMemo = new HashMap<>();
  }

  /**
   * A node in a linked list of balloons
   */
  private class Node {
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

    public Object memoKey() {
      var sb = new StringBuilder();
      //   var ls = list();
      var c = this;
      if (c.prev == null)
        c = c.next;
      while (c.next != null) {
        sb.append(c.slot);
        sb.append(' ');
        //        ls.add(c.slot);
        c = c.next;
      }
      return sb.toString();
    }

    public int popVal() {
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

    public boolean isLeft() {
      return prev == null;
    }

    public boolean isRight() {
      return next == null;
    }
  }

  /**
   * A linked sequence of moves
   */
  private class Move {
    int slot; // balloon to be popped
    int value; // value of all moves in this list
    Move nextMove; // next move in list

    public String popSequence(int[] nums) {
      var ent = this;
      var game = new Bgame(nums);
      while (ent != null) {
        game.moveSlot(ent.slot);
        ent = ent.nextMove;
      }
      return game.toString();
    }

    @Override
    public String toString() {
      var sb = sb();
      var x = this;
      while (x != null) {
        sb.append("<#").append(x.slot).append(": ");
        sb.append(x.value);
        sb.append("> ");
        x = x.nextMove;
      }
      return sb.toString();
    }

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

  /**
   * Fails with [11,5,4,8]
   */
  private class BestProfit extends Alg {

    @Override
    public int maxCoins(int[] nums) {

      var first = constructNodes(nums);

      var result = 0;

      Move movesHead = null;
      Move movesTail = null;

      while (first.next.next != null) {
        Node best = null;
        int bestScore = 0;
        for (var n = first.next; !n.isRight(); n = n.next) {
          int a, b, c, d, e;
          {
            var prev = n.prev;
            var next = n.next;
            a = prev.isLeft() ? 0 : prev.prev.value;
            b = prev.value;
            c = n.value;
            d = next.value;
            e = next.isRight() ? 0 : next.next.value;
          }
          var score = b * c * d + a * b * (d - c) + d * e * (b - c);
          if (best == null || bestScore < score) {
            best = n;
            bestScore = score;
          }
        }

        var n = best;
        var popVal = n.popVal();
        db(first, INDENT, "popping:", n.prev.value, " * [", n.value, "] *", n.next.value, " = ", popVal,
            " result now:", result + popVal);
        result += popVal;

        var ent = new Move();
        ent.slot = best.slot;
        ent.value = result;
        if (movesTail == null) {
          movesHead = ent;
          movesTail = ent;
        } else {
          movesTail.nextMove = ent;
          movesTail = movesTail.nextMove;
        }
        n.delete();
      }
      if (db) {
        db(movesHead.popSequence(nums));
      }
      return result;
    }

  }

  private class Bgame {
    Bgame(int[] nums) {
      first = constructNodes(nums);
      mNums = nums;
    }

    void moveSlot(int slot) {
      makeMove(nodeForSlot(slot));
    }

    void moveNumber(int value) {
      makeMove(nodeForValue(value));
    }

    void makeMove(Node popNode) {
      var v = popNode.popVal();
      moveValues.add(v);
      gameValue += v;
      moves.add(popNode);
      movesAux.add(popNode.delete());
      if (false)
        unmove();
    }

    void unmove() {
      int i = moves.size() - 1;
      var restored = moves.get(i);
      var prev = movesAux.get(i);
      prev.insert(restored);
      gameValue -= moveValues.get(i);
      moves.remove(i);
      movesAux.remove(i);
      moveValues.remove(i);
    }

    int moveCount() {
      return moves.size();
    }

    private int[] mNums;
    private Node first;
    private int gameValue;
    private List<Node> moves = new ArrayList<>();
    private List<Node> movesAux = new ArrayList<>();
    private List<Integer> moveValues = new ArrayList<>();

    public int value() {
      return gameValue;
    }

    public Node nodeForSlot(int slot) {
      var x = first;
      while (x != null) {
        if (x.slot == slot)
          return x;
        x = x.next;
      }
      throw badState("no node found for slot:", slot);
    }

    public Node nodeForValue(int value) {
      var x = first;
      while (x != null) {
        if (x.value == value)
          return x;
        x = x.next;
      }
      throw badState("no node found with value:", value);
    }

    @Override
    public String toString() {
      var sb = sb();
      var gm = new Bgame(mNums);
      for (int moveNum = 0; moveNum < moveCount(); moveNum++) {
        var currMove = moves.get(moveNum);
        sb.append("[ ");
        var cursor = sb.length();

        // Iterate over all nodes in the current game state
        var n = gm.first.next;
        while (n.next != null) {
          tab(sb, cursor + n.slot * 4);
          sb.append(n.slot == currMove.slot ? '*' : ' ');
          sb.append(n.value);
          n = n.next;
        }
        // Make move
        var nd = gm.nodeForSlot(currMove.slot);
        var val = nd.popVal();
        gm.makeMove(nd);
        var c2 = cursor + mNums.length * 4 + 3;
        tab(sb, c2);
        sb.append("] + " + val);
        tab(sb, c2 + 8);
        sb.append("= " + gm.value());
        sb.append('\n');
      }
      return sb.toString();
    }

  }

}
