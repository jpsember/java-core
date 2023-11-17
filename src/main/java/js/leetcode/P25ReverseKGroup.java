//25. Reverse Nodes in K-Group

// "You may not alter the values in the list's nodes, only nodes themselves may be changed."
//
// If this is to mean that the instance fields of a node are not to be modified, that makes the problem tougher...
//

package js.leetcode;

import static js.base.Tools.*;

import js.base.BasePrinter;
import js.json.JSList;

public class P25ReverseKGroup {

  public static void main(String[] args) {
    new P25ReverseKGroup().run();
  }

  private void run() {
    int n = 24;
    int k = 7;

    var a = buildNodesN(n);
    pr(reverseKGroup(a, k));
  }

  private static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
      this(val, null);
    }

    ListNode(int val, ListNode next) {
      this.val = val;
      this.next = next;
    }

    @Override
    public String toString() {
      JSList lst = list();
      ListNode nx = this;

      while (nx != null) {
        lst.add(nx.val);
        nx = nx.next;
      }
      return lst.toString();
    }
  }

  private static ListNode buildNodesN(int length) {
    int[] v = new int[length];
    for (int i = 0; i < length; i++)
      v[i] = i + 1;
    return buildNodes(v);
  }

  private static ListNode buildNodes(int... values) {
    ListNode head = null;
    ListNode tail = null;
    for (int x : values) {
      if (head == null) {
        head = new ListNode(x);
        tail = head;
      } else {
        tail.next = new ListNode(x);
        tail = tail.next;
      }
    }
    return head;
  }

  public ListNode reverseKGroup(ListNode head, int k) {
    ListNode resultHead = null;
    ListNode resultTail = null;

    while (head != null) {
      // Try to construct a reversed k group
      var chain = extractReversedChain(head, k);

      if (chain.length < k) {
        // Reverse the group back onto the end
        chain = extractReversedChain(chain.head, chain.length);
      }

      if (resultHead == null) {
        resultHead = chain.head;
      } else {
        resultTail.next = chain.head;
      }
      resultTail = chain.tail;
      head = chain.sourceHead;
    }

    return resultHead;
  }

  static class ReversedChain {
    int length;
    ListNode head;
    ListNode tail;
    ListNode sourceHead;

    @Override
    public String toString() {
      return BasePrinter.toString("ReversedChain len", length, " head", head, "tail", tail, "sourceHead",
          sourceHead);
    }

  }

  // Extract and reverse a chain from a list. Returns a node whose value is the length of the chain, and
  // whose next node is the first node in the reversed chain
  //
  private ReversedChain extractReversedChain(ListNode head, int chainSize) {
    var r = new ReversedChain();

    while (r.length < chainSize && head != null) {
      r.head = new ListNode(head.val, r.head);
      if (r.tail == null)
        r.tail = r.head;
      head = head.next;
      r.length++;
    }
    r.sourceHead = head;
    return r;
  }

}
