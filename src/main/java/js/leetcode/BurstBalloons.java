package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.base.BasePrinter;

public class BurstBalloons extends LeetCode {

  public static void main(String[] args) {
    new BurstBalloons().run();
  }

  public void run() {

    x("[3,1,5,8]", 167);
    if (true)
      return;

    x("[8,2,6,8,9,8,1,4,1,5,3,0,7,7,0,4,2]", 3414);
    if (true)
      return;

    x(3, 5, 10, 11, 9, 5, 3);
    x(1, 8, 9, 7, 2);

    if (false)
      x("[54,88,26,48,9,32,0,66,4,82,66,7,28,23,73,99,44,27,16,29,52,66,30,97,41,33,60,62,45,40,19,35,97,69,19,60,2,48,81,66,45,86,51,51,47,97,5,59,54,98,79,90,70,70,62,16,96,95,97,77,21,35,10,90,14,31,21,29,40,81,33,64,91,44,81,85,77,9,67,58,77,88,69,76,89,33,36,87,85,77,94,96,37,61,23,75,15,29,21,88,91]",
          39731870);

    x("[8,2,6,8,9,8,1,4,1,5,3,0,7,7,0,4,2]");

    x(1, 2, 3, 4, 2, 1);
    x(2, 3, 4, 5, 7, 6, 2, 1);
    x(3, 5, 10, 11, 9, 5, 3);
    x(3, 5, 9, 4, 11, 13, 7, 10, 4, 2);
    x(4, 3, 4, 5, 6, 5);
    x(11, 5, 4);
    x("[11,5,4,8]");
    //
    //    int s = 126;
    //    for (int y = 3; y < 20; y++) {
    //      pr(VERT_SP, "y:", y);
    //      xSeed(y * s + 17, y, null);
    //    }
    //    x("[1,1,1,10,2,0,1,1,1]");
    //
    //    x("[10,2,0]");
    //    x("[1,3,7,7,1,7]");
    //
    //    x("[3,1,5,8]", 167);
    //    x("[2,5,10,20,10,5,2]");
    //    x("[1,5]", 10);
    //
    //    xSeed(1965, 300, 105676120);
    //
    //    x("[3,7,11,7,3,7,11,7,3]");
    //    x("[5,6,7,1,2,3,0,6,12,20]");
    //    x("[8,3,4,3,5,0,5,6,6,2,8,5,6,2,3,8,3,5,1,0,2]", 3394);
  }

  private void xSeed(int seed, int count, Integer expected) {
    var nums = new int[count];
    rand(seed);
    for (int i = 0; i < count; i++) {
      nums[i] = rand().nextInt(101);
    }
    x(nums, expected);
  }

  private void x(int... nums) {
    x(nums, null);
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

    var alg1 = new RecursionLast();

    pr(toStr(nums));
    var res = alg1.maxCoins(nums);
    pr(gameResults(nums, alg1.getSlots()));

    if (expected == null) {
      var alg2 = new RecursionMemo();
      //      db = true;
      expected = alg2.maxCoins(nums);
      pr(alg2);
    }

    verify(res, expected);
  }

  private String gameResults(int[] nums, int[] slots) {
    var sb = sb();

    var dash = repeatText("----", nums.length + 4) + "\n";
    sb.append(dash);
    var slotEmptyFlags = new BitSet(nums.length);
    var totalPopValue = 0;
    var numsColumn = 0;

    for (int turn = 0; turn < nums.length; turn++) {
      var cursor = sb.length();
      var currMove = slots[turn];
      sb.append("[ ");

      int leftValue = 1;
      int rightValue = 1;
      int leftIndex = Integer.MIN_VALUE;
      int rightIndex = Integer.MAX_VALUE;

      for (int i = 0; i < nums.length; i++) {
        if (slotEmptyFlags.get(i))
          continue;

        if (i < currMove && i > leftIndex) {
          leftIndex = i;
          leftValue = nums[i];
        } else if (i > currMove && i < rightIndex) {
          rightIndex = i;
          rightValue = nums[i];
        }

        tab(sb, cursor + 3 + i * 4);
        sb.append(i == currMove ? '*' : ' ');
        sb.append(nums[i]);
      }

      slotEmptyFlags.set(currMove);

      var c2 = cursor + 3 + nums.length * 4;
      tab(sb, c2);
      sb.append("] = ");
      numsColumn = sb.length() - cursor;
      var popValue = nums[currMove] * leftValue * rightValue;
      sb.append(fmt(popValue));
      totalPopValue += popValue;
      sb.append('\n');
    }
    sb.append(spaces(numsColumn));
    sb.append(fmt(totalPopValue));
    sb.append('\n');
    sb.append(dash);

    return sb.toString();
  }

  private String fmt(int value) {
    var s = "" + value;
    return spaces(6 - s.length()) + s;
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
      if (cursor.next.isRight()) {
        var output = new Move();
        output.slot = cursor.slot;
        output.value = cursor.value;
        return output;
      }
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
          if (output.value <= amt) {
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

  private class Bgame {
    Bgame(int[] nums) {
      first = constructNodes(nums);
      mNums = nums;
    }

    void moveSlot(int slot) {
      makeMove(nodeForSlot(slot));
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

  /**
   * A new recursive method in which we partition based on the *last* balloon to
   * be popped, and supply the external left and right balloon values to each
   * subarray
   */
  private class RecursionLast extends Alg {

    public int maxCoins(int[] nums) {
      mNums = nums;
      mMemo.clear();
      mSlots = null;
      var result = aux(0, nums.length, 1, 1);
      return result;
    }

    public int[] getSlots() {
      if (mSlots == null) {
        var x = mNums.length;
        var sl = new ArrayList<Integer>(x);
        auxFillSlots(sl, 0, x, 1, 1);
        mSlots = new int[x];
        for (int i = 0; i < x; i++)
          mSlots[i] = sl.get(x - 1 - i);
      }
      return mSlots;
    }

    private void auxFillSlots(List<Integer> dest, int start, int stop, int leftValue, int rightValue) {
      pr("auxFill, dest:", dest, "start:", start, "stop:", stop, leftValue, rightValue);
      if (stop <= start)
        return;
      int slot = start;
      if (stop > start + 1) {
        var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
        var memoValue = mMemo.get(key);
        pr("key:", key, "memoValue:", memoValue);
        checkState(memoValue != null);
        slot = (int) (memoValue >> 32);

        pr("slot:", slot, "value:", memoValue.intValue());
      }
      dest.add(slot);
      pr("...added:", slot);
      auxFillSlots(dest, start, slot, leftValue, mNums[slot]);
      auxFillSlots(dest, slot + 1, stop, mNums[slot], rightValue);
    }

    private int aux(int start, int stop, int leftValue, int rightValue) {
      if (stop <= start)
        return 0;
      var nums = mNums;
      if (stop == start + 1)
        return leftValue * nums[start] * rightValue;

      // We store a key that embeds the left and right values (log 100 = 7 bits), and the
      // start and stop indices (log 300 = 9 bits)
      var key = leftValue | (rightValue << 7) | (start << (7 + 7)) | (stop << (7 + 7 + 9));
      long memoValue = mMemo.getOrDefault(key, -1L);
      if (memoValue >= 0)
        return (int) memoValue;

      //      var output = mMemo.get(key);
      //      if (output != null)
      //        return output;

      // Consider each balloon as the *last* one to pop

      // Is there a heuristic we can employ to speed things up?
      // Skip certain values?

      var bestResult = 0;
      var bestSlot = -1;

      // The values of the left and right sides are nonstrictly increasing as the number of values
      // increases.

      for (int pivot = start; pivot < stop; pivot++) {
        var pivotValue = nums[pivot];
        // We never want a zero to be the *last* balloon popped in a set
        if (pivotValue == 0)
          continue;

        var leftSum = aux(start, pivot, leftValue, pivotValue);
        var rightSum = aux(pivot + 1, stop, pivotValue, rightValue);
        var c = leftSum + (leftValue * pivotValue * rightValue) + rightSum;
        if (c > bestResult) {
          bestResult = c;
          bestSlot = pivot;
        }
      }

      mMemo.put(key, bestResult | (((long) bestSlot) << 32));
      return bestResult;
    }

    private Map<Integer, Long> mMemo = new HashMap<>();
    private int[] mNums;
    private int[] mSlots;
  }

}
