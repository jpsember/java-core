//23. Merge K sorted lists

// Using a divide-and-conquer to merge k/2 lists with k/2 lists, with the merge 2 lists as a subroutine

// These linked list problems can get pretty hairy... if a list is empty, how is that represented?  A null
// element?  I often add 'handle' items at the start to deal with this case.
//
package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

public class P24SwapNodesInPairs {

  public static void main(String[] args) {
    new P24SwapNodesInPairs().run();
  }

  private void run() {
    var a = buildNodes(1, 2, 3, 4);
    pr(swapPairs(a));
  }

  private static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
      this(val, null);
    }

    //    ListNode() {
    //    }

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

  public ListNode swapPairs(ListNode head) {
    ListNode targetHandle = new ListNode(0, null);

    ListNode targetTail = targetHandle;
    ListNode sourceA = head;

    while (sourceA != null) {

      ListNode sourceB = sourceA.next;
      // If there's no second element, store the tail element by itself
      if (sourceB == null) {
        targetTail.next = sourceA;
        break;
      }
      ListNode newSourceA = sourceB.next;

      ListNode targetB = sourceA;
      targetB.next = null;
      ListNode targetA = new ListNode(sourceB.val, targetB);
      targetTail.next = targetA;

      targetTail = targetB;
      sourceA = newSourceA;
    }
    
    // Strip the handle before returning
    return targetHandle.next;
  }
}
