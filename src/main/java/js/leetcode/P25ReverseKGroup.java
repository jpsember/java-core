//25. Reverse Nodes in K-Group

// "You may not alter the values in the list's nodes, only nodes themselves may be changed."
//
// If this is to mean that the instance fields of a node are not to be modified, that makes the problem tougher...
//

package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

public class P25ReverseKGroup {

  public static void main(String[] args) {
    new P25ReverseKGroup().run();
  }

  private void run() {
    var a = buildNodes(1, 2, 3, 4, 5);
    pr(reverseKGroup(a, 3));
  }

  private static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
      this(val, null);
    }

    ListNode() {
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

    ListNode targetHandle = new ListNode();
    ListNode targetTail = targetHandle;
    int[] buffer = new int[k];
    int bufferSize = 0;

    while (true) {

      bufferSize = 0;
      for (int i = 0; i < k; i++) {
        if (head == null) {
          break;
        }
        buffer[bufferSize++] = head.val;
        head = head.next;
      }

      for (int j = 0; j < bufferSize; j++) {
        int j2 = j;
        if (bufferSize == k) {
          j2 = k - 1 - j;
        }
        ListNode newTail = new ListNode(buffer[j2] );
        targetTail.next = newTail;
        targetTail = newTail;
      }
      if (bufferSize < k)
        break;
    }
    return targetHandle.next;
  }
}
