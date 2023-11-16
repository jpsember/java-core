//23. Merge K sorted lists

// Using a divide-and-conquer to merge k/2 lists with k/2 lists, with the merge 2 lists as a subroutine

package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

public class P23MergeKSortedLists {

  public static void main(String[] args) {
    new P23MergeKSortedLists().run();
  }

  private void run() {
    var a = buildNodes(1, 4, 5);
    var b = buildNodes(1, 3, 4);
    var c = buildNodes(2, 6);
    ListNode[] lists = {}; //{ a, b, c };
    pr(mergeKLists(lists));
  }

  private static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
      this.val = val;
    }

    ListNode() {
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

  public ListNode mergeKLists(ListNode[] lists) {
    if (lists.length == 0)
      return new ListNode();
    return auxMerge(lists, 0, lists.length);
  }

  private ListNode auxMerge(ListNode[] lists, int i0, int i1) {
    int size = i1 - i0;
    switch (size) {
    case 0:
      return null;
    case 1:
      return lists[i0];
    case 2:
      return mergeTwoLists(lists[i0], lists[i0 + 1]);
    default: {
      int div = (i0 + i1) / 2;
      return mergeTwoLists(auxMerge(lists, i0, div), auxMerge(lists, div, i1));
    }
    }
  }

  private ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    ListNode head = null;
    ListNode tail = null;
    while (list1 != null || list2 != null) {
      ListNode newTail;
      if (list1 == null || (list2 != null && list2.val < list1.val)) {
        newTail = list2;
        list2 = list2.next;
      } else {
        newTail = list1;
        list1 = list1.next;
      }
      if (head == null) {
        head = newTail;
        tail = head;
      } else {
        tail.next = newTail;
        tail = tail.next;
      }
    }
    return head;
  }
}
